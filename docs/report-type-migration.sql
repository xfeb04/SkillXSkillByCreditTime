-- MySQL 8.0. Stop the app and back up the database before running manually.
-- Renames the column without changing the existing report values.
USE skillxskillbycredittime;
DELIMITER $$
DROP PROCEDURE IF EXISTS rename_report_type$$
CREATE PROCEDURE rename_report_type()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
               AND TABLE_NAME='report' AND COLUMN_NAME='banParty') THEN
        IF EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE()
                   AND TABLE_NAME='report' AND COLUMN_NAME='reportType') THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Both banParty and reportType exist. Reconcile data before renaming.';
        END IF;
        ALTER TABLE report RENAME COLUMN banParty TO reportType;
    END IF;
END$$
CALL rename_report_type()$$
DROP PROCEDURE rename_report_type$$
DELIMITER ;
SELECT report_id, reportType FROM report ORDER BY report_id;
