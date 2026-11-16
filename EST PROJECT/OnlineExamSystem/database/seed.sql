USE online_exam_db;

-- 1. Users
INSERT INTO Users (name, email, password_hash, role) VALUES
  ('Test Teacher', 'teacher@test.com', SHA2('pass123', 256), 'teacher'),
  ('Test Student', 'student@test.com', SHA2('pass123', 256), 'student')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Subjects
INSERT INTO Subjects (subject_name, subject_code) VALUES
  ('DBMS', 'CS301'),
  ('Java Programming', 'CS201'),
  ('Operating Systems', 'CS302')
ON DUPLICATE KEY UPDATE subject_code=VALUES(subject_code);

-- 3. Exam
INSERT INTO Exams (title, description, subject_id, duration_minutes, difficulty, created_by, pass_mark, total_marks) VALUES
  (
    'DBMS Fundamentals',
    'A comprehensive test on Database Management Systems concepts',
    (SELECT subject_id FROM Subjects WHERE subject_name = 'DBMS'),
    60,
    'Medium',
    (SELECT user_id FROM Users WHERE email = 'teacher@test.com'),
    40,
    5
  );

-- 4. Questions
INSERT INTO Questions (exam_id, question_text, marks, question_order) VALUES
  ((SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1), 'What does DBMS stand for?', 1, 1),
  ((SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1), 'Which of the following is a DDL command?', 1, 2),
  ((SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1), 'What is a primary key?', 1, 3),
  ((SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1), 'Which normal form eliminates partial dependencies?', 1, 4),
  ((SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1), 'What does SQL stand for?', 1, 5);

-- 5. Options
-- Options for Q1: "What does DBMS stand for?"
INSERT INTO Options (question_id, option_text, is_correct) VALUES
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 1), 'Database Management System', TRUE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 1), 'Data Base Management System', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 1), 'Database Managed System', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 1), 'Data Management Base System', FALSE);

-- Options for Q2: "Which of the following is a DDL command?"
INSERT INTO Options (question_id, option_text, is_correct) VALUES
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 2), 'SELECT', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 2), 'INSERT', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 2), 'CREATE', TRUE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 2), 'UPDATE', FALSE);

-- Options for Q3: "What is a primary key?"
INSERT INTO Options (question_id, option_text, is_correct) VALUES
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 3), 'A column that uniquely identifies each row', TRUE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 3), 'A key used for encryption', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 3), 'A foreign reference column', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 3), 'A key shared between tables', FALSE);

-- Options for Q4: "Which normal form eliminates partial dependencies?"
INSERT INTO Options (question_id, option_text, is_correct) VALUES
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 4), '1NF', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 4), '2NF', TRUE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 4), '3NF', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 4), 'BCNF', FALSE);

-- Options for Q5: "What does SQL stand for?"
INSERT INTO Options (question_id, option_text, is_correct) VALUES
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 5), 'Structured Query Language', TRUE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 5), 'Simple Query Language', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 5), 'Standard Query Logic', FALSE),
  ((SELECT question_id FROM Questions WHERE exam_id = (SELECT exam_id FROM Exams WHERE title = 'DBMS Fundamentals' LIMIT 1) AND question_order = 5), 'Sequential Query Language', FALSE);
