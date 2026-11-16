-- ============================================================
-- Online Exam System Database Schema
-- Database : online_exam_db
-- Charset  : utf8mb4 / utf8mb4_unicode_ci
-- Engine   : InnoDB
-- Normal Form: 3NF
-- ============================================================

CREATE DATABASE IF NOT EXISTS online_exam_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE online_exam_db;

-- ------------------------------------------------------------
-- 1. Users
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Users (
    user_id       INT           NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    role          ENUM('student','teacher') NOT NULL DEFAULT 'student',
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 2. Subjects
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Subjects (
    subject_id   INT          NOT NULL AUTO_INCREMENT,
    subject_name VARCHAR(100) NOT NULL,
    subject_code VARCHAR(20)  DEFAULT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (subject_id),
    UNIQUE KEY uq_subjects_name (subject_name),
    UNIQUE KEY uq_subjects_code (subject_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 3. Exams
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Exams (
    exam_id          INT          NOT NULL AUTO_INCREMENT,
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    subject_id       INT          NOT NULL,
    duration_minutes INT          NOT NULL CHECK (duration_minutes > 0),
    difficulty       ENUM('Easy', 'Medium', 'Hard') NOT NULL DEFAULT 'Medium',
    created_by       INT          NOT NULL,
    pass_mark        INT          NOT NULL DEFAULT 40 CHECK (pass_mark BETWEEN 0 AND 100),
    total_marks      INT          NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exam_id),
    INDEX idx_exams_created_by (created_by),
    INDEX idx_exams_subject_id (subject_id),
    CONSTRAINT fk_exams_created_by
        FOREIGN KEY (created_by) REFERENCES Users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_exams_subject_id
        FOREIGN KEY (subject_id) REFERENCES Subjects (subject_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 4. Questions
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Questions (
    question_id    INT  NOT NULL AUTO_INCREMENT,
    exam_id        INT  NOT NULL,
    question_text  TEXT NOT NULL,
    marks          INT  NOT NULL DEFAULT 1 CHECK (marks > 0),
    question_order INT  NOT NULL,
    PRIMARY KEY (question_id),
    INDEX idx_questions_exam_id (exam_id),
    CONSTRAINT fk_questions_exam_id
        FOREIGN KEY (exam_id) REFERENCES Exams (exam_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 5. Options
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Options (
    option_id   INT          NOT NULL AUTO_INCREMENT,
    question_id INT          NOT NULL,
    option_text VARCHAR(500) NOT NULL,
    is_correct  BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (option_id),
    INDEX idx_options_question_id (question_id),
    CONSTRAINT fk_options_question_id
        FOREIGN KEY (question_id) REFERENCES Questions (question_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 6. Student_Responses
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Student_Responses (
    response_id        INT       NOT NULL AUTO_INCREMENT,
    student_id         INT       NOT NULL,
    exam_id            INT       NOT NULL,
    question_id        INT       NOT NULL,
    selected_option_id INT       DEFAULT NULL,
    submitted_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (response_id),
    UNIQUE KEY uq_response (student_id, exam_id, question_id),
    INDEX idx_sr_student_id         (student_id),
    INDEX idx_sr_exam_id            (exam_id),
    INDEX idx_sr_question_id        (question_id),
    INDEX idx_sr_selected_option_id (selected_option_id),
    CONSTRAINT fk_sr_student_id
        FOREIGN KEY (student_id) REFERENCES Users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_sr_exam_id
        FOREIGN KEY (exam_id) REFERENCES Exams (exam_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_sr_question_id
        FOREIGN KEY (question_id) REFERENCES Questions (question_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_sr_selected_option_id
        FOREIGN KEY (selected_option_id) REFERENCES Options (option_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 7. Results
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Results (
    result_id    INT           NOT NULL AUTO_INCREMENT,
    student_id   INT           NOT NULL,
    exam_id      INT           NOT NULL,
    score        DECIMAL(5,2)  NOT NULL DEFAULT 0.00,
    total_marks  INT           NOT NULL,
    passed       BOOLEAN       NOT NULL DEFAULT FALSE,
    attempt_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (result_id),
    UNIQUE KEY uq_result (student_id, exam_id),
    INDEX idx_results_student_id (student_id),
    INDEX idx_results_exam_id    (exam_id),
    CONSTRAINT fk_results_student_id
        FOREIGN KEY (student_id) REFERENCES Users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_results_exam_id
        FOREIGN KEY (exam_id) REFERENCES Exams (exam_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 8. Audit_Logs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Audit_Logs (
    log_id      INT          NOT NULL AUTO_INCREMENT,
    table_name  VARCHAR(100) NOT NULL,
    action_type ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    record_id   INT          NOT NULL,
    changed_by  INT          DEFAULT NULL,
    old_value   TEXT,
    new_value   TEXT,
    changed_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    INDEX idx_audit_changed_by (changed_by),
    CONSTRAINT fk_audit_changed_by
        FOREIGN KEY (changed_by) REFERENCES Users (user_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 9. Cheating_Logs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS Cheating_Logs (
    cheat_id   INT          NOT NULL AUTO_INCREMENT,
    student_id INT          NOT NULL,
    exam_id    INT          NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details    TEXT,
    PRIMARY KEY (cheat_id),
    INDEX idx_cheat_student_id (student_id),
    INDEX idx_cheat_exam_id    (exam_id),
    CONSTRAINT fk_cheat_student_id
        FOREIGN KEY (student_id) REFERENCES Users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cheat_exam_id
        FOREIGN KEY (exam_id) REFERENCES Exams (exam_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
