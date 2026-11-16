USE online_exam_db;

-- 1. Create Subjects table if it does not exist
CREATE TABLE IF NOT EXISTS Subjects (
    subject_id   INT          NOT NULL AUTO_INCREMENT,
    subject_name VARCHAR(100) NOT NULL,
    subject_code VARCHAR(20)  DEFAULT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (subject_id),
    UNIQUE KEY uq_subjects_name (subject_name),
    UNIQUE KEY uq_subjects_code (subject_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Seed 3 default subjects (DBMS, Java Programming, Operating Systems)
INSERT INTO Subjects (subject_name, subject_code)
VALUES 
    ('DBMS', 'CS301'),
    ('Java Programming', 'CS201'),
    ('Operating Systems', 'CS302')
ON DUPLICATE KEY UPDATE 
    subject_code = VALUES(subject_code);

-- 3. Alter Exams table to add subject_id and difficulty if not present
SET @dbname = DATABASE();
SET @tablename = 'Exams';

-- Add subject_id column
SET @columnname = 'subject_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  'ALTER TABLE Exams ADD COLUMN subject_id INT DEFAULT NULL AFTER description'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add difficulty column
SET @columnname = 'difficulty';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  "ALTER TABLE Exams ADD COLUMN difficulty ENUM('Easy', 'Medium', 'Hard') NOT NULL DEFAULT 'Medium' AFTER duration_minutes"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add index for subject_id if not present
SET @indexname = 'idx_exams_subject_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND INDEX_NAME = @indexname
  ) > 0,
  'SELECT 1',
  'ALTER TABLE Exams ADD INDEX idx_exams_subject_id (subject_id)'
));
PREPARE addIdxIfNotExists FROM @preparedStatement;
EXECUTE addIdxIfNotExists;
DEALLOCATE PREPARE addIdxIfNotExists;

-- Add foreign key constraint for subject_id if not present
SET @constraintname = 'fk_exams_subject_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND CONSTRAINT_NAME = @constraintname
  ) > 0,
  'SELECT 1',
  'ALTER TABLE Exams ADD CONSTRAINT fk_exams_subject_id FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id) ON DELETE CASCADE'
));
PREPARE addFkIfNotExists FROM @preparedStatement;
EXECUTE addFkIfNotExists;
DEALLOCATE PREPARE addFkIfNotExists;

-- 4. Update existing exams: link 'DBMS Fundamentals' (and any unlinked exams) to DBMS subject with 'Medium' difficulty
UPDATE Exams 
SET 
    subject_id = (SELECT subject_id FROM Subjects WHERE subject_name = 'DBMS' LIMIT 1),
    difficulty = 'Medium'
WHERE subject_id IS NULL;

