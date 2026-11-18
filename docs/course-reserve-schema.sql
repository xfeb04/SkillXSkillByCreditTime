-- Run against the application's database only if Hibernate ddl-auto=update
-- has not made register_course_id nullable. Existing registration links are preserved.
ALTER TABLE credit_transaction MODIFY COLUMN register_course_id INT NULL;
-- Hibernate adds reserved_course_id and its unique foreign key from the entity mapping.
-- Existing course reserves are not reconstructed automatically: old balances may
-- include multiple courses or legacy seed transactions. For a course still in
-- created status, saving its edit form records its current reserve without
-- debiting the unchanged reserve a second time.
