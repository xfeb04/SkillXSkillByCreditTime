-- MySQL 8.0: stop the application and back up the database before running manually.
-- Rename the review date column while preserving all existing values.
USE skillxskillbycredittime;
DELIMITER $$
DROP PROCEDURE IF EXISTS rename_review_date$$
CREATE PROCEDURE rename_review_date()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='register_course' AND COLUMN_NAME='previewDate') THEN
        IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='register_course' AND COLUMN_NAME='reviewDate') THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Both previewDate and reviewDate exist. Reconcile data before renaming.';
        END IF;
        ALTER TABLE register_course RENAME COLUMN previewDate TO reviewDate;
    END IF;
END$$
CALL rename_review_date()$$
DROP PROCEDURE rename_review_date$$
DELIMITER ;
SELECT register_course_id, reviewDate FROM register_course ORDER BY register_course_id;
