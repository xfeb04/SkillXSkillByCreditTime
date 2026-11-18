-- MySQL 8.0. Stop the application, back up the database, then run manually in Workbench.
-- Supports the original schema and the earlier Member/Student/Teacher migration.
-- Never run automatically at application startup. Keeps account IDs and credit balances.
USE skillxskillbycredittime;
DELIMITER $$

DROP PROCEDURE IF EXISTS replace_member_role_fk$$
CREATE PROCEDURE replace_member_role_fk(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_target VARCHAR(64))
BEGIN
    DECLARE finished INT DEFAULT 0;
    DECLARE old_fk VARCHAR(64);
    DECLARE existing_fk INT DEFAULT 0;
    DECLARE fk_cursor CURSOR FOR
        SELECT DISTINCT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=p_table AND COLUMN_NAME=p_column
          AND REFERENCED_TABLE_NAME IS NOT NULL
          AND (p_target IS NULL OR REFERENCED_TABLE_NAME<>p_target OR REFERENCED_COLUMN_NAME<>'member_id');
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished=1;
    OPEN fk_cursor;
    fk_loop: LOOP
        FETCH fk_cursor INTO old_fk;
        IF finished=1 THEN LEAVE fk_loop; END IF;
        SET @role_sql=CONCAT('ALTER TABLE `',REPLACE(p_table,'`','``'),
            '` DROP FOREIGN KEY `',REPLACE(old_fk,'`','``'),'`');
        PREPARE role_statement FROM @role_sql;
        EXECUTE role_statement;
        DEALLOCATE PREPARE role_statement;
    END LOOP;
    CLOSE fk_cursor;
    IF p_target IS NOT NULL THEN
        SELECT COUNT(*) INTO existing_fk FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=p_table AND COLUMN_NAME=p_column
          AND REFERENCED_TABLE_NAME=p_target AND REFERENCED_COLUMN_NAME='member_id';
        IF existing_fk=0 THEN
            SET @role_sql=CONCAT('ALTER TABLE `',REPLACE(p_table,'`','``'),
                '` ADD CONSTRAINT `fk_role_',p_table,'_',p_column,'` FOREIGN KEY (`',
                REPLACE(p_column,'`','``'),'`) REFERENCES `',REPLACE(p_target,'`','``'),'` (`member_id`)');
            PREPARE role_statement FROM @role_sql;
            EXECUTE role_statement;
            DEALLOCATE PREPARE role_statement;
        END IF;
    END IF;
END$$

DROP PROCEDURE IF EXISTS migrate_member_roles$$
CREATE PROCEDURE migrate_member_roles()
BEGIN
    DECLARE old_columns INT DEFAULT 0;
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
               AND TABLE_NAME='report' AND COLUMN_NAME='banParty') THEN
        IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
                   AND TABLE_NAME='report' AND COLUMN_NAME='reportType') THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Both banParty and reportType exist. Reconcile data before renaming.';
        END IF;
        ALTER TABLE report RENAME COLUMN banParty TO reportType;
    END IF;

    SELECT COUNT(*) INTO old_columns FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='report'
      AND COLUMN_NAME IN ('reporter_member_id','reported_member_id');
    IF old_columns=1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Incomplete legacy report columns. Restore or repair before migration.';
    END IF;
    IF EXISTS (SELECT 1 FROM teacher t LEFT JOIN members m ON m.member_id=t.member_id WHERE m.member_id IS NULL)
       OR EXISTS (SELECT 1 FROM register_course r LEFT JOIN members m ON m.member_id=r.member_id WHERE m.member_id IS NULL)
       OR EXISTS (SELECT 1 FROM report WHERE reportType IS NULL OR reportType NOT IN ('ผู้เรียน','ผู้สอน')) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Invalid member references or report reportType. Fix before migration.';
    END IF;
    IF old_columns=2 THEN
        IF EXISTS (SELECT 1 FROM report r LEFT JOIN members m ON m.member_id=r.reporter_member_id WHERE m.member_id IS NULL)
           OR EXISTS (SELECT 1 FROM report r LEFT JOIN members m ON m.member_id=r.reported_member_id WHERE m.member_id IS NULL) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Orphan report account references found.';
        END IF;
    END IF;

    CREATE TABLE IF NOT EXISTS student (
        member_id INT NOT NULL,
        PRIMARY KEY (member_id),
        CONSTRAINT fk_student_member FOREIGN KEY (member_id) REFERENCES members(member_id)
    ) ENGINE=InnoDB;
    INSERT INTO student (member_id)
    SELECT m.member_id FROM members m LEFT JOIN student s ON s.member_id=m.member_id WHERE s.member_id IS NULL;
    INSERT INTO teacher (member_id,bio,avgRatingScore)
    SELECT m.member_id,NULL,0.00 FROM members m LEFT JOIN teacher t ON t.member_id=m.member_id WHERE t.member_id IS NULL;
    CALL replace_member_role_fk('student','member_id','members');
    CALL replace_member_role_fk('teacher','member_id','members');
    CALL replace_member_role_fk('register_course','member_id','student');

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
                   AND TABLE_NAME='report' AND COLUMN_NAME='teacher_member_id') THEN
        ALTER TABLE report ADD COLUMN teacher_member_id INT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
                   AND TABLE_NAME='report' AND COLUMN_NAME='student_member_id') THEN
        ALTER TABLE report ADD COLUMN student_member_id INT NULL;
    END IF;
    IF old_columns=2 THEN
        -- reportType identifies who was reported, not who filed the report.
        IF EXISTS (SELECT 1 FROM report WHERE
            (teacher_member_id IS NOT NULL AND teacher_member_id <>
                CASE WHEN reportType='ผู้เรียน' THEN reporter_member_id ELSE reported_member_id END)
            OR (student_member_id IS NOT NULL AND student_member_id <>
                CASE WHEN reportType='ผู้เรียน' THEN reported_member_id ELSE reporter_member_id END)) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Existing report role columns disagree with legacy identities.';
        END IF;
        UPDATE report SET
            teacher_member_id=CASE WHEN reportType='ผู้เรียน' THEN reporter_member_id ELSE reported_member_id END,
            student_member_id=CASE WHEN reportType='ผู้เรียน' THEN reported_member_id ELSE reporter_member_id END;
    END IF;
    -- Validate identities against the actual course owner and enrolled student before retiring old columns.
    IF EXISTS (SELECT 1 FROM report r
        LEFT JOIN register_course rc ON rc.register_course_id=r.register_course_id
        LEFT JOIN course c ON c.course_id=rc.course_id
        LEFT JOIN teacher t ON t.member_id=r.teacher_member_id
        LEFT JOIN student s ON s.member_id=r.student_member_id
        WHERE t.member_id IS NULL OR s.member_id IS NULL OR rc.register_course_id IS NULL OR c.course_id IS NULL
           OR r.teacher_member_id<>c.teacher_member_id OR r.student_member_id<>rc.member_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Report roles disagree with registration/course. Fix data before continuing.';
    END IF;
    ALTER TABLE report MODIFY COLUMN teacher_member_id INT NOT NULL, MODIFY COLUMN student_member_id INT NOT NULL;
    CALL replace_member_role_fk('report','teacher_member_id','teacher');
    CALL replace_member_role_fk('report','student_member_id','student');
    IF old_columns=2 THEN
        CALL replace_member_role_fk('report','reporter_member_id',NULL);
        CALL replace_member_role_fk('report','reported_member_id',NULL);
        ALTER TABLE report DROP COLUMN reporter_member_id, DROP COLUMN reported_member_id;
    END IF;
END$$

CALL migrate_member_roles()$$
DROP PROCEDURE migrate_member_roles$$
DROP PROCEDURE replace_member_role_fk$$
DELIMITER ;

SELECT (SELECT COUNT(*) FROM members) AS members_count,
       (SELECT COUNT(*) FROM student) AS student_count,
       (SELECT COUNT(*) FROM teacher) AS teacher_count;
SELECT member_id,username,creditBalance,creditFrozen FROM members ORDER BY member_id;
SELECT report_id,reportType,teacher_member_id,student_member_id,register_course_id FROM report ORDER BY report_id;
SELECT TABLE_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA=DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL
  AND TABLE_NAME IN ('teacher','student','register_course','report')
ORDER BY TABLE_NAME,COLUMN_NAME;
