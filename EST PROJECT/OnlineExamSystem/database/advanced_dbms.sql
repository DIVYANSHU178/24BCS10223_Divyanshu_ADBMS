USE online_exam_db;

-- ============================================================
-- (A) TRIGGER: after_response_insert
-- ============================================================
DELIMITER $$

DROP TRIGGER IF EXISTS after_response_insert$$
CREATE TRIGGER after_response_insert
AFTER INSERT ON Student_Responses
FOR EACH ROW
BEGIN
    DECLARE v_is_correct     TINYINT(1) DEFAULT 0;
    DECLARE v_marks          DECIMAL(5,2) DEFAULT 0;
    DECLARE v_total_marks    DECIMAL(10,2) DEFAULT 0;
    DECLARE v_pass_mark      DECIMAL(5,2) DEFAULT 0;
    DECLARE v_score          DECIMAL(10,2) DEFAULT 0;
    DECLARE v_passed         TINYINT(1) DEFAULT 0;

    -- Determine if the selected option is correct and get question marks
    IF NEW.selected_option_id IS NOT NULL THEN
        SELECT o.is_correct, q.marks
        INTO   v_is_correct, v_marks
        FROM   Options o
        JOIN   Questions q ON q.question_id = NEW.question_id
        WHERE  o.option_id = NEW.selected_option_id
          AND  o.question_id = NEW.question_id;
    ELSE
        -- Skipped: still need marks for reference but no score change
        SELECT marks INTO v_marks
        FROM   Questions
        WHERE  question_id = NEW.question_id;
    END IF;

    -- Get exam totals
    SELECT total_marks, pass_mark
    INTO   v_total_marks, v_pass_mark
    FROM   Exams
    WHERE  exam_id = NEW.exam_id;

    -- Recalculate accumulated score for this student + exam
    SELECT COALESCE(
               SUM(
                   CASE
                       WHEN sr.selected_option_id IS NULL THEN 0
                       WHEN o.is_correct = 1             THEN q.marks
                       ELSE                                   -(0.25)
                   END
               ), 0)
    INTO v_score
    FROM  Student_Responses sr
    JOIN  Questions q  ON q.question_id  = sr.question_id
    LEFT JOIN Options o
           ON o.option_id   = sr.selected_option_id
          AND o.question_id = sr.question_id
    WHERE sr.student_id = NEW.student_id
      AND sr.exam_id    = NEW.exam_id;

    -- Determine pass/fail
    SET v_passed = IF(v_score >= (v_pass_mark / 100.0) * v_total_marks, 1, 0);

    -- Upsert into Results
    INSERT INTO Results (student_id, exam_id, score, total_marks, passed)
    VALUES (NEW.student_id, NEW.exam_id, v_score, v_total_marks, v_passed)
    ON DUPLICATE KEY UPDATE
        score       = v_score,
        total_marks = v_total_marks,
        passed      = v_passed;
END$$

-- Also recalculate on response update (e.g. ON DUPLICATE KEY UPDATE)
DROP TRIGGER IF EXISTS after_response_update$$
CREATE TRIGGER after_response_update
AFTER UPDATE ON Student_Responses
FOR EACH ROW
BEGIN
    DECLARE v_total_marks    DECIMAL(10,2) DEFAULT 0;
    DECLARE v_pass_mark      DECIMAL(5,2) DEFAULT 0;
    DECLARE v_score          DECIMAL(10,2) DEFAULT 0;
    DECLARE v_passed         TINYINT(1) DEFAULT 0;

    -- Get exam totals
    SELECT total_marks, pass_mark
    INTO   v_total_marks, v_pass_mark
    FROM   Exams
    WHERE  exam_id = NEW.exam_id;

    -- Recalculate accumulated score for this student + exam
    SELECT COALESCE(
               SUM(
                   CASE
                       WHEN sr.selected_option_id IS NULL THEN 0
                       WHEN o.is_correct = 1             THEN q.marks
                       ELSE                                   -(0.25)
                   END
               ), 0)
    INTO v_score
    FROM  Student_Responses sr
    JOIN  Questions q  ON q.question_id  = sr.question_id
    LEFT JOIN Options o
           ON o.option_id   = sr.selected_option_id
          AND o.question_id = sr.question_id
    WHERE sr.student_id = NEW.student_id
      AND sr.exam_id    = NEW.exam_id;

    -- Determine pass/fail
    SET v_passed = IF(v_score >= (v_pass_mark / 100.0) * v_total_marks, 1, 0);

    -- Upsert into Results
    INSERT INTO Results (student_id, exam_id, score, total_marks, passed)
    VALUES (NEW.student_id, NEW.exam_id, v_score, v_total_marks, v_passed)
    ON DUPLICATE KEY UPDATE
        score       = v_score,
        total_marks = v_total_marks,
        passed      = v_passed;
END$$

DELIMITER ;

-- ============================================================
-- (B) TRIGGER: after_question_update
-- ============================================================
DELIMITER $$

DROP TRIGGER IF EXISTS after_question_update$$
CREATE TRIGGER after_question_update
AFTER UPDATE ON Questions
FOR EACH ROW
BEGIN
    INSERT INTO Audit_Logs (table_name, action_type, record_id, old_value, new_value, changed_by)
    VALUES (
        'Questions',
        'UPDATE',
        NEW.question_id,
        CONCAT('text:', OLD.question_text, ',marks:', OLD.marks),
        CONCAT('text:', NEW.question_text, ',marks:', NEW.marks),
        NULL
    );
END$$

DELIMITER ;

-- ============================================================
-- (C) STORED PROCEDURE: GetExamAnalytics
-- ============================================================
DELIMITER $$

DROP PROCEDURE IF EXISTS GetExamAnalytics$$
CREATE PROCEDURE GetExamAnalytics(IN p_exam_id INT)
BEGIN
    SELECT
        COUNT(DISTINCT student_id)                                AS total_attempts,
        AVG(score)                                                AS avg_score,
        MAX(score)                                                AS max_score,
        SUM(CASE WHEN passed = TRUE THEN 1 ELSE 0 END)           AS pass_count
    FROM Results
    WHERE exam_id = p_exam_id;
END$$

DELIMITER ;

-- ============================================================
-- (D) VIEW: Student_Leaderboard
-- ============================================================
CREATE OR REPLACE VIEW Student_Leaderboard AS
SELECT
    u.user_id,
    u.name,
    u.email,
    e.title                                                        AS exam_title,
    r.score,
    r.total_marks,
    r.passed,
    RANK() OVER (PARTITION BY r.exam_id ORDER BY r.score DESC)    AS rank_position
FROM  Results r
JOIN  Users u ON r.student_id = u.user_id
JOIN  Exams e ON r.exam_id    = e.exam_id;
