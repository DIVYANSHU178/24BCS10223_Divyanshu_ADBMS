package dao;

import config.DBConnection;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class AnalyticsDAO {

    public JSONObject getAnalytics(int examId) {
        JSONObject result = new JSONObject();
        result.put("exam_id", examId);
        String sql = "{CALL GetExamAnalytics(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, examId);
            cs.execute();
            ResultSet rs = cs.getResultSet();
            if (rs != null && rs.next()) {
                int totalAttempts = rs.getInt("total_attempts");
                double avgScore = Math.round(rs.getDouble("avg_score") * 100.0) / 100.0;
                double maxScore = rs.getDouble("max_score");
                int passCount = rs.getInt("pass_count");
                result.put("total_attempts", totalAttempts);
                result.put("avg_score", avgScore);
                result.put("max_score", maxScore);
                result.put("pass_count", passCount);
            } else {
                result.put("total_attempts", 0);
                result.put("avg_score", 0.0);
                result.put("max_score", 0.0);
                result.put("pass_count", 0);
            }
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("total_attempts", 0);
            result.put("avg_score", 0.0);
            result.put("max_score", 0.0);
            result.put("pass_count", 0);
        }
        return result;
    }

    public JSONArray getLeaderboard(int examId) {
        JSONArray leaderboard = new JSONArray();
        String sql = "SELECT u.user_id, u.name, u.email, e.title AS exam_title, " +
                     "r.score, r.total_marks, r.passed, " +
                     "RANK() OVER (ORDER BY r.score DESC) AS rank_position " +
                     "FROM Results r " +
                     "JOIN Users u ON r.student_id = u.user_id " +
                     "JOIN Exams e ON r.exam_id = e.exam_id " +
                     "WHERE r.exam_id = ? " +
                     "ORDER BY rank_position ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject entry = new JSONObject();
                    entry.put("user_id", rs.getInt("user_id"));
                    entry.put("name", rs.getString("name"));
                    entry.put("email", rs.getString("email"));
                    entry.put("exam_title", rs.getString("exam_title"));
                    entry.put("score", rs.getDouble("score"));
                    entry.put("total_marks", rs.getDouble("total_marks"));
                    entry.put("passed", rs.getBoolean("passed"));
                    entry.put("rank_position", rs.getInt("rank_position"));
                    leaderboard.put(entry);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }

    public JSONArray getLeaderboard() {
        JSONArray leaderboard = new JSONArray();
        String sql = "SELECT u.user_id, u.name, sl.exam_title, sl.score, sl.total_marks, " +
                     "sl.passed, sl.rank_position " +
                     "FROM Student_Leaderboard sl " +
                     "JOIN Users u ON sl.user_id = u.user_id " +
                     "ORDER BY sl.exam_title, sl.rank_position ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("user_id", rs.getInt("user_id"));
                entry.put("name", rs.getString("name"));
                entry.put("exam_title", rs.getString("exam_title"));
                entry.put("score", rs.getDouble("score"));
                entry.put("total_marks", rs.getDouble("total_marks"));
                entry.put("passed", rs.getBoolean("passed"));
                entry.put("rank_position", rs.getInt("rank_position"));
                leaderboard.put(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }
}
