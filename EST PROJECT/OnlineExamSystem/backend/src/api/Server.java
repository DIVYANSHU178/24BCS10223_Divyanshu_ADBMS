package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;
import dao.ExamDAO;
import dao.AnalyticsDAO;

public class Server {

    private static File frontendDirectory;

    public static void main(String[] args) throws IOException {
        initFrontendDirectory();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // API Endpoints
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/exams", new ExamsHandler());
        server.createContext("/api/questions", new QuestionsHandler());
        server.createContext("/api/submit", new SubmitHandler());
        server.createContext("/api/create-exam", new CreateExamHandler());
        server.createContext("/api/log-cheating", new CheatingHandler());
        server.createContext("/api/analytics", new AnalyticsHandler());
        server.createContext("/api/leaderboard", new LeaderboardHandler());
        server.createContext("/api/subjects", new SubjectsHandler());
        server.createContext("/api/review", new ReviewHandler());

        // Static Web Hosting Handler (Root '/')
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(12));
        server.start();
        System.out.println("=================================================");
        System.out.println("  Online Examination System - 3-Tier Enterprise  ");
        System.out.println("  Server started on: http://localhost:8080       ");
        System.out.println("  Serving frontend from: " + frontendDirectory.getAbsolutePath());
        System.out.println("=================================================");
    }

    private static void initFrontendDirectory() {
        File[] candidates = new File[] {
            new File("frontend"),
            new File("../frontend"),
            new File("C:/Users/karan/Desktop/OnlineExamSystem/frontend")
        };
        for (File dir : candidates) {
            if (dir.exists() && dir.isDirectory()) {
                frontendDirectory = dir;
                return;
            }
        }
        frontendDirectory = new File("frontend");
    }

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, Origin");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");
    }

    private static void sendJsonResponse(HttpExchange exchange, int code, String body) throws IOException {
        setCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // -------------------------------------------------------------------------
    // StaticFileHandler: Serves frontend/ files with MIME types, default /login.html
    // -------------------------------------------------------------------------
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                setCorsHeaders(exchange);
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                String path = exchange.getRequestURI().getPath();
                if (path == null || path.equals("/") || path.trim().isEmpty()) {
                    path = "/login.html";
                }

                // Sanitize path to prevent directory traversal
                String cleanPath = path.replace("\\", "/");
                while (cleanPath.startsWith("/")) {
                    cleanPath = cleanPath.substring(1);
                }

                File targetFile = new File(frontendDirectory, cleanPath);

                // Canonical path security check
                String canonicalBase = frontendDirectory.getCanonicalPath();
                String canonicalTarget = targetFile.getCanonicalPath();

                if (!canonicalTarget.startsWith(canonicalBase) || !targetFile.exists() || targetFile.isDirectory()) {
                    String notFoundMsg = "<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p>The requested file was not found.</p><p><a href='/login.html'>Go to Login</a></p></body></html>";
                    byte[] notFoundBytes = notFoundMsg.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(404, notFoundBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(notFoundBytes);
                    }
                    return;
                }

                String mimeType = getMimeType(targetFile.getName());
                exchange.getResponseHeaders().set("Content-Type", mimeType);
                exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");

                byte[] fileBytes = Files.readAllBytes(targetFile.toPath());
                exchange.sendResponseHeaders(200, fileBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileBytes);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    exchange.sendResponseHeaders(500, -1);
                } catch (Exception ignored) {}
            }
        }

        private String getMimeType(String fileName) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".gif")) return "image/gif";
            if (lower.endsWith(".ico")) return "image/x-icon";
            if (lower.endsWith(".woff2")) return "font/woff2";
            if (lower.endsWith(".woff")) return "font/woff";
            if (lower.endsWith(".ttf")) return "font/ttf";
            return "application/octet-stream";
        }
    }

    // -------------------------------------------------------------------------
    // LoginHandler
    // -------------------------------------------------------------------------
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String body = readBody(exchange);
                JSONObject req = new JSONObject(body);
                String email = req.getString("email");
                String password = req.getString("password");

                JSONObject user = new ExamDAO().authenticateUser(email, password);
                if (user != null) {
                    JSONObject resp = new JSONObject();
                    resp.put("success", true);
                    resp.put("user", user);
                    sendJsonResponse(exchange, 200, resp.toString());
                } else {
                    JSONObject resp = new JSONObject();
                    resp.put("success", false);
                    resp.put("error", "Invalid email or password.");
                    sendJsonResponse(exchange, 401, resp.toString());
                }
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("error", e.getMessage());
                sendJsonResponse(exchange, 500, err.toString());
            }
        }
    }

    // -------------------------------------------------------------------------
    // ExamsHandler: Returns exams with subject_id, subject_name, difficulty
    // -------------------------------------------------------------------------
    static class ExamsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                JSONArray exams = new ExamDAO().getExams();
                JSONObject resp = new JSONObject();
                resp.put("success", true);
                resp.put("exams", exams);
                sendJsonResponse(exchange, 200, resp.toString());
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("error", e.getMessage());
                sendJsonResponse(exchange, 500, err.toString());
            }
        }
    }

    // -------------------------------------------------------------------------
    // SubjectsHandler: GET /api/subjects, POST /api/subjects
    // -------------------------------------------------------------------------
    static class SubjectsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String method = exchange.getRequestMethod().toUpperCase();
            if ("GET".equals(method)) {
                try {
                    JSONArray subjects = new ExamDAO().getSubjects();
                    JSONObject resp = new JSONObject();
                    resp.put("success", true);
                    resp.put("subjects", subjects);
                    sendJsonResponse(exchange, 200, resp.toString());
                } catch (Exception e) {
                    JSONObject err = new JSONObject();
                    err.put("success", false);
                    err.put("error", e.getMessage());
                    sendJsonResponse(exchange, 500, err.toString());
                }
            } else if ("POST".equals(method)) {
                try {
                    String body = readBody(exchange);
                    JSONObject req = new JSONObject(body);
                    String name = req.getString("subject_name");
                    String code = req.optString("subject_code", null);
                    JSONObject result = new ExamDAO().addSubject(name, code);
                    int statusCode = result.optBoolean("success", false) ? 200 : 400;
                    sendJsonResponse(exchange, statusCode, result.toString());
                } catch (Exception e) {
                    JSONObject err = new JSONObject();
                    err.put("success", false);
                    err.put("error", e.getMessage());
                    sendJsonResponse(exchange, 500, err.toString());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // -------------------------------------------------------------------------
    // QuestionsHandler
    // -------------------------------------------------------------------------
    static class QuestionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                int examId = -1;
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2 && "exam_id".equals(kv[0])) {
                            examId = Integer.parseInt(kv[1]);
                        }
                    }
                }
                JSONArray questions = new ExamDAO().getQuestions(examId);
                JSONObject resp = new JSONObject();
                resp.put("success", true);
                resp.put("questions", questions);
                sendJsonResponse(exchange, 200, resp.toString());
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("error", e.getMessage());
                sendJsonResponse(exchange, 500, err.toString());
            }
        }
    }

    // -------------------------------------------------------------------------
    // SubmitHandler
    // -------------------------------------------------------------------------
    static class SubmitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                setCorsHeaders(exchange);
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                String body = readBody(exchange);
                if (body == null || body.trim().isEmpty()) {
                    JSONObject err = new JSONObject();
                    err.put("success", false);
                    err.put("error", "Request payload cannot be empty.");
                    sendJsonResponse(exchange, 400, err.toString());
                    return;
                }

                JSONObject req = new JSONObject(body);
                int studentId = req.optInt("student_id", -1);
                if (studentId == -1) {
                    try {
                        studentId = Integer.parseInt(req.optString("student_id", "-1"));
                    } catch (Exception ignored) {}
                }

                int examId = req.optInt("exam_id", -1);
                if (examId == -1) {
                    try {
                        examId = Integer.parseInt(req.optString("exam_id", "-1"));
                    } catch (Exception ignored) {}
                }

                if (studentId == -1 || examId == -1) {
                    JSONObject err = new JSONObject();
                    err.put("success", false);
                    err.put("error", "Both student_id and exam_id are required.");
                    sendJsonResponse(exchange, 400, err.toString());
                    return;
                }

                JSONArray responses = req.optJSONArray("responses");
                if (responses == null) {
                    responses = new JSONArray();
                }

                JSONObject result = new ExamDAO().submitExam(studentId, examId, responses);
                sendJsonResponse(exchange, 200, result.toString());
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    JSONObject err = new JSONObject();
                    err.put("success", false);
                    err.put("error", e.getMessage() != null ? e.getMessage() : "Internal Server Error");
                    sendJsonResponse(exchange, 500, err.toString());
                } catch (Exception ignored) {}
            }
        }
    }

    // -------------------------------------------------------------------------
    // CreateExamHandler: Saves subject_id, difficulty, and multiple questions
    // -------------------------------------------------------------------------
    static class CreateExamHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String body = readBody(exchange);
                JSONObject req = new JSONObject(body);
                JSONObject result = new ExamDAO().createExam(req);
                int statusCode = result.optBoolean("success", false) ? 200 : 400;
                sendJsonResponse(exchange, statusCode, result.toString());
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("error", e.getMessage());
                sendJsonResponse(exchange, 500, err.toString());
            }
        }
    }

    // -------------------------------------------------------------------------
    // CheatingHandler
    // -------------------------------------------------------------------------
    static class CheatingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String body = readBody(exchange);
                JSONObject req = new JSONObject(body);
                int studentId = req.getInt("student_id");
                int examId = req.getInt("exam_id");
                String eventType = req.getString("event_type");
                String details = req.optString("details", "");

                JSONObject result = new ExamDAO().logCheating(studentId, examId, eventType, details);
                sendJsonResponse(exchange, 200, result.toString());
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("error", e.getMessage());
                sendJsonResponse(exchange, 500, err.toString());
            }
        }
    }

    // -------------------------------------------------------------------------
    // AnalyticsHandler
    // -------------------------------------------------------------------------
    static class AnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                int examId = -1;
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2 && "exam_id".equals(kv[0])) {
                            examId = Integer.parseInt(kv[1]);
                        }
                    }
                }
                JSONObject analytics = new AnalyticsDAO().getAnalytics(examId);
                JSONObject resp = new JSONObject();
                resp.put("success", true);
                resp.put("analytics", analytics);
                sendJsonResponse(exchange, 200, resp.toString());
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("error", e.getMessage());
                sendJsonResponse(exchange, 500, err.toString());
            }
        }
    }

    // -------------------------------------------------------------------------
    // LeaderboardHandler
    // -------------------------------------------------------------------------
    static class LeaderboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                Integer examId = null;
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2 && "exam_id".equals(kv[0])) {
                            examId = Integer.parseInt(kv[1]);
                        }
                    }
                }

                JSONArray leaderboard;
                if (examId != null) {
                    leaderboard = new AnalyticsDAO().getLeaderboard(examId);
                } else {
                    leaderboard = new AnalyticsDAO().getLeaderboard();
                }

                JSONObject resp = new JSONObject();
                resp.put("success", true);
                resp.put("leaderboard", leaderboard);
                sendJsonResponse(exchange, 200, resp.toString());
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("error", e.getMessage());
                sendJsonResponse(exchange, 500, err.toString());
            }
        }
    }

    // -------------------------------------------------------------------------
    // ReviewHandler: GET /api/review?student_id=X&exam_id=Y
    // -------------------------------------------------------------------------
    static class ReviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                int studentId = -1;
                int examId = -1;
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2) {
                            if ("student_id".equals(kv[0])) {
                                studentId = Integer.parseInt(kv[1]);
                            } else if ("exam_id".equals(kv[0])) {
                                examId = Integer.parseInt(kv[1]);
                            }
                        }
                    }
                }
                if (studentId == -1 || examId == -1) {
                    JSONObject err = new JSONObject();
                    err.put("success", false);
                    err.put("error", "Both student_id and exam_id parameters are required.");
                    sendJsonResponse(exchange, 400, err.toString());
                    return;
                }

                JSONObject review = new ExamDAO().getExamReview(studentId, examId);
                int statusCode = review.optBoolean("success", false) ? 200 : 403;
                sendJsonResponse(exchange, statusCode, review.toString());
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("error", e.getMessage());
                sendJsonResponse(exchange, 500, err.toString());
            }
        }
    }
}
