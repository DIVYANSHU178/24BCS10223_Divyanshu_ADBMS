# Online Examination System (3-Tier DBMS & JDBC Architecture)

A full-stack, strictly separated **3-Tier Online Examination System** built with **MySQL 8.0**, **Core Java (Raw JDBC + Built-in HttpServer)**, and **Vanilla HTML/CSS/JavaScript**. Designed specifically to showcase core Database Management System (DBMS) principles—including 3NF normalization, active database triggers, stored procedures, window functions, and manual ACID transaction management—without relying on heavy ORM frameworks.

---

## 🏗️ System Architecture (Strict 3-Tier Separation)

```text
OnlineExamSystem/
├── database/                  # Tier 1: Data Layer (Pure MySQL 8.0)
│   ├── schema.sql             # 3NF normalized schema with PK, FK, UNIQUE & CHECK constraints
│   ├── advanced_dbms.sql      # Negative-marking trigger, audit trigger, SP & RANK() view
│   ├── upgrade_subjects.sql   # Multi-subject categorization & exam difficulty levels
│   └── seed.sql               # Initial teacher, student & sample DBMS exam data
├── backend/                   # Tier 2: Application & JDBC Bridge (Core Java 17+)
│   ├── lib/
│   │   ├── mysql-connector-j-26.10.0.jar
│   │   └── json-20240303.jar
│   ├── src/
│   │   ├── config/DBConnection.java
│   │   ├── dao/ExamDAO.java
│   │   ├── dao/AnalyticsDAO.java
│   │   └── api/Server.java
│   └── out/                   # Compiled .class bytecode
├── frontend/                  # Tier 3: Presentation Layer (Zero SQL/DB access)
│   ├── login.html             # Role-based authentication portal
│   ├── student-exam.html      # Interactive exam UI, SVG timer, keyboard shortcuts & proctoring
│   ├── teacher-dashboard.html # Subject/exam builder & stored procedure analytics
│   └── leaderboard.html       # Live rankings powered by SQL RANK() view
└── Launch-Website.bat         # One-click Windows launcher
```

---

## 👥 Team Members & DBMS Responsibilities

| Team Member | Role | Owned Modules & Files | Core DBMS Concepts |
| :--- | :--- | :--- | :--- |
| **Jaskaran** *(Team Lead)* | JDBC Connectivity & ACID Transaction Engineer | `DBConnection.java`, `ExamDAO.java`, `Server.java`, `login.html`, `student-exam.html` | Type-4 JDBC, `PreparedStatement` (SQL Injection prevention), `setAutoCommit(false)` / `commit()` / `rollback()`, `ON DUPLICATE KEY UPDATE` |
| **Arjun** | Conceptual Modeling & Referential Integrity Lead | `schema.sql` (`Users`, `Exams`), `upgrade_subjects.sql` (`Subjects`), `seed.sql` | ER Modeling, `1:N` Cardinalities, Primary & Foreign Keys, `ON DELETE CASCADE` |
| **Shubham** | Relational Normalization & Constraints Engineer | `schema.sql` (`Questions`, `Options`, `Cheating_Logs`), `teacher-dashboard.html` | Functional Dependencies, `1NF → 2NF → 3NF` Decomposition, `CHECK`, `ENUM` & `UNIQUE` constraints |
| **Divyanshu** | Active Database & Triggers Developer | `advanced_dbms.sql` (Triggers), `Student_Responses`, `Results`, `Audit_Logs` | `AFTER INSERT` & `AFTER UPDATE` Triggers, Automated Negative Marking (`-0.25`), `OLD`/`NEW` Audit Trails |
| **Pahul** | Analytical SQL, Views & Stored Procedures Lead | `advanced_dbms.sql` (SP & View), `AnalyticsDAO.java`, `leaderboard.html` | Parameterized Stored Procedures (`CallableStatement`), Views, `RANK() OVER (PARTITION BY ...)` |

---

## ✨ Key DBMS & Application Features

### 1. Database-Level Features (`MySQL 8.0`)
* **3rd Normal Form (3NF) Schema:** Decomposes multi-choice options into `Options` (1NF) and subject metadata into `Subjects` (3NF) across 9 relational tables.
* **Automated Grading Trigger (`after_response_insert`):** Automatically evaluates answers upon insertion into `Student_Responses`, awarding `+marks` for correct answers and deducting `-0.25` negative marking for incorrect ones inside `Results`.
* **Audit Trail Trigger (`after_question_update`):** Captures `OLD` and `NEW` values in `Audit_Logs` whenever an exam question is modified.
* **Stored Procedure (`GetExamAnalytics`):** Computes total attempts, average score, highest score, and pass count in a single database call.
* **Leaderboard View (`Student_Leaderboard`):** Uses SQL window functions (`RANK() OVER (PARTITION BY exam_id ORDER BY score DESC)`) while hiding sensitive credentials (`password_hash`).

### 2. Backend & Security Features (`Pure Java JDBC`)
* **ACID Transaction Control:** Exam submissions and multi-question exam creations run with `conn.setAutoCommit(false)` and full `conn.rollback()` on failure.
* **SQL Injection & Answer-Leak Protection:** All queries use `PreparedStatement` with `?` placeholders, passwords are verified using MySQL `SHA2(?, 256)`, and `is_correct` is excluded from student question projections.
* **Concurrent Thread Pool:** Built on `com.sun.net.httpserver.HttpServer` with a fixed 10-thread executor paired with MySQL InnoDB row-level locking.

### 3. Interactive Frontend Features
* **Subject & Difficulty Filtering:** Filter exams by subject (`DBMS`, `Java`, `OS`) and difficulty (`Easy`, `Medium`, `Hard`).
* **Interactive Exam Interface:** Circular SVG countdown timer with auto-submit, question navigator palette (`Answered`, `Marked for Review`, `Unvisited`), keyboard shortcuts (`1–4` / `A–D`, `Left/Right` arrows), and post-exam score gauge.
* **Anti-Cheating Proctoring:** Detects tab switching (`visibilitychange`) and window blur events, logging violations directly into MySQL's `Cheating_Logs` table.

---

## 🚀 Setup & Installation Guide

### Prerequisites
* **Java Development Kit (JDK) 17 or 21** (`javac` added to system PATH)
* **MySQL Server 8.0+** & **MySQL Workbench**
* Required JARs placed in `backend/lib/`:
  * `mysql-connector-j-26.10.0.jar`
  * `json-20240303.jar`

### Step 1: Initialize the Database
1. Open **MySQL Workbench** and connect to your local MySQL instance (`localhost:3306`).
2. Execute the SQL scripts in the following exact order:
   1. `database/schema.sql`
   2. `database/advanced_dbms.sql`
   3. `database/upgrade_subjects.sql`
   4. `database/seed.sql`
3. Verify your database credentials in `backend/src/config/DBConnection.java` (default user: `root`, password: `root`).

### Step 2: Compile the Java Backend
Open a terminal in the root `OnlineExamSystem/` folder and run:

```bash
# Windows
javac -cp "backend/lib/mysql-connector-j-26.10.0.jar;backend/lib/json-20240303.jar" -d backend/out backend/src/config/DBConnection.java backend/src/dao/ExamDAO.java backend/src/dao/AnalyticsDAO.java backend/src/api/Server.java

# Mac / Linux
javac -cp "backend/lib/mysql-connector-j-26.10.0.jar:backend/lib/json-20240303.jar" -d backend/out backend/src/config/DBConnection.java backend/src/dao/ExamDAO.java backend/src/dao/AnalyticsDAO.java backend/src/api/Server.java
```

### Step 3: Launch the Website
* **Windows (One-Click):** Double-click **`Launch-Website.bat`** to start MySQL, boot the Java server on port `8080`, and open `http://localhost:8080` automatically.
* **Manual Start:**
  ```bash
  java -cp "backend/out;backend/lib/mysql-connector-j-26.10.0.jar;backend/lib/json-20240303.jar" api.Server
  ```
  Then open **`http://localhost:8080`** in your browser.

---

## 🔑 Default Test Credentials

| Role | Email | Password |
| :--- | :--- | :--- |
| **Teacher** | `teacher@test.com` | `pass123` |
| **Student** | `student@test.com` | `pass123` |

---

## 📡 REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/login` | Authenticates user credentials using MySQL `SHA2` |
| `GET` | `/api/subjects` | Lists all available subjects |
| `POST` | `/api/subjects` | Adds a new subject (Teacher) |
| `GET` | `/api/exams` | Fetches all exams with subject & difficulty metadata |
| `GET` | `/api/questions?exam_id=` | Fetches exam questions & options (omitting `is_correct`) |
| `POST` | `/api/create-exam` | Atomically creates an exam, questions & options |
| `POST` | `/api/submit` | Commits student responses & returns trigger-calculated score |
| `POST` | `/api/log-cheating` | Logs browser tab-switch violations into `Cheating_Logs` |
| `GET` | `/api/analytics?exam_id=` | Executes `CALL GetExamAnalytics(?)` stored procedure |
| `GET` | `/api/leaderboard` | Queries the `Student_Leaderboard` SQL `RANK()` view |
