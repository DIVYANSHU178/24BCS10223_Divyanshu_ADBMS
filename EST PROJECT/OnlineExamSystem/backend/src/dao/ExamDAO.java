package dao;

import config.DBConnection;
import java.sql.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExamDAO {

    public JSONObject authenticateUser(String email, String password) {
        String sql = "SELECT user_id, name, email, role FROM Users WHERE email=? AND password_hash=SHA2(?,256)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject obj = new JSONObject();
                    obj.put("user_id", rs.getInt("user_id"));
                    obj.put("name", rs.getString("name"));
                    obj.put("email", rs.getString("email"));
                    obj.put("role", rs.getString("role"));
                    return obj;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray getSubjects() {
        String sql = "SELECT subject_id, subject_name, subject_code, created_at FROM Subjects ORDER BY subject_name ASC";
        JSONArray result = new JSONArray();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JSONObject sub = new JSONObject();
                sub.put("subject_id", rs.getInt("subject_id"));
                sub.put("subject_name", rs.getString("subject_name"));
                sub.put("subject_code", rs.getString("subject_code") != null ? rs.getString("subject_code") : "");
                result.put(sub);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject addSubject(String subjectName, String subjectCode) {
        String sql = "INSERT INTO Subjects(subject_name, subject_code) VALUES(?,?)";
        JSONObject result = new JSONObject();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, subjectName.trim());
            if (subjectCode != null && !subjectCode.trim().isEmpty()) {
                ps.setString(2, subjectCode.trim());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }
            ps.executeUpdate();
            int subjectId = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    subjectId = keys.getInt(1);
                }
            }
            result.put("success", true);
            result.put("subject_id", subjectId);
            result.put("subject_name", subjectName.trim());
            result.put("subject_code", subjectCode != null ? subjectCode.trim() : "");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public JSONArray getExams() {
        String sql = "SELECT e.exam_id, e.title, e.description, e.duration_minutes, e.pass_mark, " +
                "e.total_marks, e.difficulty, e.subject_id, s.subject_name, s.subject_code, u.name as teacher_name " +
                "FROM Exams e " +
                "JOIN Users u ON e.created_by = u.user_id " +
                "LEFT JOIN Subjects s ON e.subject_id = s.subject_id " +
                "ORDER BY e.created_at DESC";
        JSONArray result = new JSONArray();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JSONObject exam = new JSONObject();
                exam.put("exam_id", rs.getInt("exam_id"));
                exam.put("title", rs.getString("title"));
                exam.put("description", rs.getString("description"));
                exam.put("duration_minutes", rs.getInt("duration_minutes"));
                exam.put("pass_mark", rs.getInt("pass_mark"));
                exam.put("total_marks", rs.getInt("total_marks"));
                exam.put("difficulty", rs.getString("difficulty") != null ? rs.getString("difficulty") : "Medium");
                exam.put("subject_id", rs.getInt("subject_id"));
                exam.put("subject_name",
                        rs.getString("subject_name") != null ? rs.getString("subject_name") : "General");
                exam.put("subject_code", rs.getString("subject_code") != null ? rs.getString("subject_code") : "");
                exam.put("teacher_name", rs.getString("teacher_name"));
                result.put(exam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONArray getQuestions(int examId) {
        String qSql = "SELECT q.question_id, q.question_text, q.marks, q.question_order " +
                "FROM Questions q WHERE q.exam_id=? ORDER BY q.question_order";
        String optSql = "SELECT option_id, option_text FROM Options WHERE question_id=? ORDER BY option_id ASC";
        JSONArray result = new JSONArray();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement qPs = conn.prepareStatement(qSql)) {
            qPs.setInt(1, examId);
            try (ResultSet qRs = qPs.executeQuery()) {
                while (qRs.next()) {
                    JSONObject question = new JSONObject();
                    int questionId = qRs.getInt("question_id");
                    question.put("question_id", questionId);
                    question.put("question_text", qRs.getString("question_text"));
                    question.put("marks", qRs.getInt("marks"));
                    question.put("question_order", qRs.getInt("question_order"));

                    JSONArray options = new JSONArray();
                    try (PreparedStatement optPs = conn.prepareStatement(optSql)) {
                        optPs.setInt(1, questionId);
                        try (ResultSet optRs = optPs.executeQuery()) {
                            while (optRs.next()) {
                                JSONObject option = new JSONObject();
                                option.put("option_id", optRs.getInt("option_id"));
                                option.put("option_text", optRs.getString("option_text"));
                                options.put(option);
                            }
                        }
                    }
                    question.put("options", options);
                    result.put(question);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject submitExam(int studentId, int examId, JSONArray responses) {
        String insertSql = "INSERT INTO Student_Responses(student_id, exam_id, question_id, selected_option_id) " +
                "VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE selected_option_id=VALUES(selected_option_id)";
        String resultSql = "SELECT score, total_marks, passed FROM Results WHERE student_id=? AND exam_id=?";
        JSONObject result = new JSONObject();
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (int i = 0; i < responses.length(); i++) {
                        JSONObject response = responses.getJSONObject(i);
                        ps.setInt(1, studentId);
                        ps.setInt(2, examId);
                        ps.setInt(3, response.getInt("question_id"));
                        if (response.has("selected_option_id") && !response.isNull("selected_option_id")) {
                            ps.setInt(4, response.getInt("selected_option_id"));
                        } else {
                            ps.setNull(4, java.sql.Types.INTEGER);
                        }
                        ps.executeUpdate();
                    }
                }

                // Explicitly update/insert Results row to guarantee score calculation
                // even when ON DUPLICATE KEY UPDATE occurs on existing Student_Responses
                String recalcSql = 
                    "INSERT INTO Results (student_id, exam_id, score, total_marks, passed) " +
                    "SELECT ?, ?, " +
                    "COALESCE(SUM(CASE WHEN sr.selected_option_id IS NULL THEN 0 WHEN o.is_correct = 1 THEN q.marks ELSE -0.25 END), 0) AS score, " +
                    "e.total_marks, " +
                    "(COALESCE(SUM(CASE WHEN sr.selected_option_id IS NULL THEN 0 WHEN o.is_correct = 1 THEN q.marks ELSE -0.25 END), 0) >= (e.pass_mark / 100.0) * e.total_marks) AS passed " +
                    "FROM Exams e " +
                    "LEFT JOIN Questions q ON q.exam_id = e.exam_id " +
                    "LEFT JOIN Student_Responses sr ON sr.question_id = q.question_id AND sr.student_id = ? AND sr.exam_id = ? " +
                    "LEFT JOIN Options o ON o.option_id = sr.selected_option_id AND o.question_id = q.question_id " +
                    "WHERE e.exam_id = ? " +
                    "GROUP BY e.exam_id, e.total_marks, e.pass_mark " +
                    "ON DUPLICATE KEY UPDATE " +
                    "score = VALUES(score), total_marks = VALUES(total_marks), passed = VALUES(passed)";

                try (PreparedStatement psRecalc = conn.prepareStatement(recalcSql)) {
                    psRecalc.setInt(1, studentId);
                    psRecalc.setInt(2, examId);
                    psRecalc.setInt(3, studentId);
                    psRecalc.setInt(4, examId);
                    psRecalc.setInt(5, examId);
                    psRecalc.executeUpdate();
                }

                conn.commit();

                try (PreparedStatement ps2 = conn.prepareStatement(resultSql)) {
                    ps2.setInt(1, studentId);
                    ps2.setInt(2, examId);
                    try (ResultSet rs = ps2.executeQuery()) {
                        if (rs.next()) {
                            result.put("success", true);
                            result.put("score", rs.getDouble("score"));
                            result.put("total_marks", rs.getInt("total_marks"));
                            result.put("passed", rs.getBoolean("passed"));
                        } else {
                            result.put("success", true);
                            result.put("score", 0.0);
                            result.put("total_marks", 0);
                            result.put("passed", false);
                        }
                    }
                }
            } catch (Exception ex) {
                conn.rollback();
                result.put("success", false);
                result.put("error", ex.getMessage());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public JSONObject createExam(JSONObject examData) {
        String examSql = "INSERT INTO Exams(title, description, subject_id, duration_minutes, difficulty, created_by, pass_mark, total_marks) "
                +
                "VALUES(?,?,?,?,?,?,?,?)";
        String questionSql = "INSERT INTO Questions(exam_id, question_text, marks, question_order) VALUES(?,?,?,?)";
        String optionSql = "INSERT INTO Options(question_id, option_text, is_correct) VALUES(?,?,?)";
        JSONObject result = new JSONObject();
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int examId;
                try (PreparedStatement ps = conn.prepareStatement(examSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, examData.getString("title"));
                    ps.setString(2, examData.optString("description", ""));
                    if (examData.has("subject_id") && !examData.isNull("subject_id")) {
                        ps.setInt(3, examData.getInt("subject_id"));
                    } else {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    }
                    ps.setInt(4, examData.getInt("duration_minutes"));
                    ps.setString(5, examData.optString("difficulty", "Medium"));
                    ps.setInt(6, examData.getInt("created_by"));
                    ps.setInt(7, examData.getInt("pass_mark"));
                    ps.setInt(8, examData.getInt("total_marks"));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            examId = keys.getInt(1);
                        } else {
                            throw new SQLException("Failed to obtain generated exam ID.");
                        }
                    }
                }

                JSONArray questions = examData.getJSONArray("questions");
                for (int i = 0; i < questions.length(); i++) {
                    JSONObject question = questions.getJSONObject(i);
                    int questionId;
                    try (PreparedStatement qPs = conn.prepareStatement(questionSql, Statement.RETURN_GENERATED_KEYS)) {
                        qPs.setInt(1, examId);
                        qPs.setString(2, question.getString("question_text"));
                        qPs.setInt(3, question.optInt("marks", 1));
                        qPs.setInt(4, question.optInt("question_order", i + 1));
                        qPs.executeUpdate();
                        try (ResultSet qKeys = qPs.getGeneratedKeys()) {
                            if (qKeys.next()) {
                                questionId = qKeys.getInt(1);
                            } else {
                                throw new SQLException("Failed to obtain generated question ID.");
                            }
                        }
                    }

                    JSONArray options = question.getJSONArray("options");
                    for (int j = 0; j < options.length(); j++) {
                        JSONObject option = options.getJSONObject(j);
                        try (PreparedStatement optPs = conn.prepareStatement(optionSql)) {
                            optPs.setInt(1, questionId);
                            optPs.setString(2, option.getString("option_text"));
                            optPs.setBoolean(3, option.optBoolean("is_correct", false));
                            optPs.executeUpdate();
                        }
                    }
                }

                conn.commit();
                result.put("success", true);
                result.put("exam_id", examId);
            } catch (Exception ex) {
                conn.rollback();
                result.put("success", false);
                result.put("error", ex.getMessage());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public JSONObject logCheating(int studentId, int examId, String eventType, String details) {
        String sql = "INSERT INTO Cheating_Logs(student_id, exam_id, event_type, details) VALUES(?,?,?,?)";
        JSONObject result = new JSONObject();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            ps.setString(3, eventType);
            ps.setString(4, details != null ? details : "");
            ps.executeUpdate();
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    public JSONObject getExamReview(int studentId, int examId) {
        JSONObject result = new JSONObject();
        String resultSql = "SELECT score, total_marks, passed FROM Results WHERE student_id=? AND exam_id=?";
        String qSql = "SELECT q.question_id, q.question_text, q.marks, q.question_order " +
                      "FROM Questions q WHERE q.exam_id=? ORDER BY q.question_order ASC";
        String optSql = "SELECT option_id, option_text, is_correct FROM Options WHERE question_id=? ORDER BY option_id ASC";
        String respSql = "SELECT selected_option_id FROM Student_Responses WHERE student_id=? AND exam_id=? AND question_id=?";

        try (Connection conn = DBConnection.getConnection()) {
            // Check if student submitted this exam
            try (PreparedStatement resPs = conn.prepareStatement(resultSql)) {
                resPs.setInt(1, studentId);
                resPs.setInt(2, examId);
                try (ResultSet resRs = resPs.executeQuery()) {
                    if (resRs.next()) {
                        result.put("score", resRs.getDouble("score"));
                        result.put("total_marks", resRs.getInt("total_marks"));
                        result.put("passed", resRs.getBoolean("passed"));
                    } else {
                        result.put("success", false);
                        result.put("error", "Exam has not been submitted yet.");
                        return result;
                    }
                }
            }

            JSONArray questions = new JSONArray();
            try (PreparedStatement qPs = conn.prepareStatement(qSql)) {
                qPs.setInt(1, examId);
                try (ResultSet qRs = qPs.executeQuery()) {
                    while (qRs.next()) {
                        JSONObject qObj = new JSONObject();
                        int questionId = qRs.getInt("question_id");
                        int marks = qRs.getInt("marks");
                        qObj.put("question_id", questionId);
                        qObj.put("question_text", qRs.getString("question_text"));
                        qObj.put("marks", marks);
                        qObj.put("question_order", qRs.getInt("question_order"));

                        // Get student's response for this question
                        Integer selectedOptionId = null;
                        try (PreparedStatement rPs = conn.prepareStatement(respSql)) {
                            rPs.setInt(1, studentId);
                            rPs.setInt(2, examId);
                            rPs.setInt(3, questionId);
                            try (ResultSet rRs = rPs.executeQuery()) {
                                if (rRs.next()) {
                                    int selId = rRs.getInt("selected_option_id");
                                    if (!rRs.wasNull()) {
                                        selectedOptionId = selId;
                                    }
                                }
                            }
                        }

                        if (selectedOptionId != null) {
                            qObj.put("selected_option_id", selectedOptionId);
                        } else {
                            qObj.put("selected_option_id", JSONObject.NULL);
                        }

                        // Fetch options and determine correctness
                        JSONArray optionsArr = new JSONArray();
                        boolean isCorrect = false;
                        int correctOptionId = -1;

                        try (PreparedStatement optPs = conn.prepareStatement(optSql)) {
                            optPs.setInt(1, questionId);
                            try (ResultSet optRs = optPs.executeQuery()) {
                                while (optRs.next()) {
                                    JSONObject optObj = new JSONObject();
                                    int optId = optRs.getInt("option_id");
                                    boolean correct = optRs.getBoolean("is_correct");
                                    optObj.put("option_id", optId);
                                    optObj.put("option_text", optRs.getString("option_text"));
                                    optObj.put("is_correct", correct);
                                    optionsArr.put(optObj);

                                    if (correct) {
                                        correctOptionId = optId;
                                    }
                                    if (selectedOptionId != null && selectedOptionId == optId && correct) {
                                        isCorrect = true;
                                    }
                                }
                            }
                        }

                        qObj.put("options", optionsArr);
                        qObj.put("correct_option_id", correctOptionId);

                        if (selectedOptionId == null) {
                            qObj.put("status", "SKIPPED");
                            qObj.put("earned_marks", 0.0);
                        } else if (isCorrect) {
                            qObj.put("status", "CORRECT");
                            qObj.put("earned_marks", (double) marks);
                        } else {
                            qObj.put("status", "INCORRECT");
                            qObj.put("earned_marks", -0.25);
                        }

                        questions.put(qObj);
                    }
                }
            }

            result.put("success", true);
            result.put("questions", questions);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
