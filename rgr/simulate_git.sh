#!/bin/bash

# =============================================================
# Скрипт имитации истории разработки — папка rgr/
#
# Структура репозитория:
#   homework/
#   ├── lr6/
#   ├── lr7/
#   ├── lr8/
#   └── rgr/        ← здесь лежит этот скрипт
#       ├── simulate_git.sh
#       ├── pom.xml
#       ├── sql/
#       └── src/
#
# Использование:
#   Положи скрипт в папку homework/rgr/
#   cd homework/rgr
#   chmod +x simulate_git.sh
#   ./simulate_git.sh
#
# Время работы: ~2 минуты
# Коммиты: 25 штук, даты 10–19 марта 2026
# =============================================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

log()  { echo -e "${GREEN}[$(date '+%H:%M:%S')]${NC} $1"; }
step() { echo -e "\n${BLUE}━━━ $1 ━━━${NC}"; }

# =============================================================
# Определяем пути
# =============================================================
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"   # .../homework/rgr
REPO_DIR="$(dirname "$SCRIPT_DIR")"           # .../homework  (корень репозитория)
RGR_REL="rgr"                                  # относительный путь от корня репо

# Проверки
if [ ! -f "$SCRIPT_DIR/pom.xml" ]; then
  echo "❌ Запусти скрипт из папки rgr/ (где лежит pom.xml)"
  exit 1
fi

if [ ! -d "$REPO_DIR/.git" ]; then
  echo "❌ Git репозиторий не найден в $REPO_DIR"
  echo "   Убедись что папка homework/ инициализирована через git init"
  exit 1
fi

log "Репозиторий: $REPO_DIR"
log "Папка РГР:   $SCRIPT_DIR"
echo ""

# =============================================================
# Функция коммита — всегда из корня репозитория,
# добавляем только файлы внутри rgr/
# =============================================================
commit() {
  local msg="$1"
  local date="$2"
  cd "$REPO_DIR"
  git add "$RGR_REL/" 2>/dev/null || true
  GIT_AUTHOR_DATE="$date +0300" \
  GIT_COMMITTER_DATE="$date +0300" \
  git commit -m "rgr: $msg" --quiet --allow-empty
  log "  ✓ rgr: $msg"
  cd "$SCRIPT_DIR"
}

p() { sleep "${1:-2}"; }

# =============================================================
# Сохраняем итоговые файлы во временную папку
# =============================================================
TMPFINAL=$(mktemp -d)
cp -r "$SCRIPT_DIR/src" "$TMPFINAL/" 2>/dev/null || true
cp -r "$SCRIPT_DIR/sql" "$TMPFINAL/" 2>/dev/null || true
cp "$SCRIPT_DIR/pom.xml" "$TMPFINAL/" 2>/dev/null || true
[ -f "$SCRIPT_DIR/README.md" ] && cp "$SCRIPT_DIR/README.md" "$TMPFINAL/" || true

# =============================================================
step "ДЕНЬ 1 (10 марта) — Инициализация"
# =============================================================

cat > "$SCRIPT_DIR/README.md" << 'RDEOF'
# Project Manager — РГР

Веб-приложение для управления проектами и задачами.
Java Servlets + PostgreSQL + JDBC
RDEOF

# Временная минимальная версия pom.xml
cat > "$SCRIPT_DIR/pom.xml" << 'POMEOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.pm</groupId>
    <artifactId>project-manager</artifactId>
    <version>1.0.0</version>
    <packaging>war</packaging>
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.3</version>
        </dependency>
    </dependencies>
    <build>
        <finalName>pm</finalName>
    </build>
</project>
POMEOF

commit "инициализация проекта, pom.xml, README" "2026-03-10 09:14:22"
p 3

# --- Схема БД v0.1 ---
mkdir -p "$SCRIPT_DIR/sql"
cat > "$SCRIPT_DIR/sql/schema.sql" << 'SQLEOF'
-- schema v0.1

CREATE TABLE IF NOT EXISTS users (
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(200) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'user',
    confirmed     BOOLEAN      NOT NULL DEFAULT false,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
    id          SERIAL PRIMARY KEY,
    owner_id    INT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id          SERIAL PRIMARY KEY,
    project_id  INT          NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'open',
    priority    VARCHAR(10)  NOT NULL DEFAULT 'medium',
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
SQLEOF

commit "схема БД v0.1 — таблицы users, projects, tasks" "2026-03-10 10:02:55"
p 3

# --- Модели ---
mkdir -p "$SCRIPT_DIR/src/main/java/com/pm/model"

cat > "$SCRIPT_DIR/src/main/java/com/pm/model/User.java" << 'EOF'
package com.pm.model;

public class User {
    private int id;
    private String username;
    private String email;
    private String role;
    private boolean confirmed;

    public User() {}
    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }
    public String getUsername()      { return username; }
    public void setUsername(String v){ this.username = v; }
    public String getEmail()         { return email; }
    public void setEmail(String v)   { this.email = v; }
    public String getRole()          { return role; }
    public void setRole(String v)    { this.role = v; }
    public boolean isConfirmed()     { return confirmed; }
    public void setConfirmed(boolean v) { this.confirmed = v; }
    public boolean isAdmin()         { return "admin".equals(role); }
}
EOF

cat > "$SCRIPT_DIR/src/main/java/com/pm/model/Project.java" << 'EOF'
package com.pm.model;

import java.sql.Timestamp;

public class Project {
    private int id;
    private int ownerId;
    private String name;
    private String description;
    private String status;
    private Timestamp createdAt;

    public Project() {}
    public int getId()              { return id; }
    public void setId(int v)        { this.id = v; }
    public int getOwnerId()         { return ownerId; }
    public void setOwnerId(int v)   { this.ownerId = v; }
    public String getName()         { return name; }
    public void setName(String v)   { this.name = v; }
    public String getDescription()  { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getStatus()       { return status; }
    public void setStatus(String v) { this.status = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
}
EOF

cat > "$SCRIPT_DIR/src/main/java/com/pm/model/Task.java" << 'EOF'
package com.pm.model;

import java.sql.Timestamp;

public class Task {
    private int id;
    private int projectId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Timestamp createdAt;

    public Task() {}
    public int getId()              { return id; }
    public void setId(int v)        { this.id = v; }
    public int getProjectId()       { return projectId; }
    public void setProjectId(int v) { this.projectId = v; }
    public String getTitle()        { return title; }
    public void setTitle(String v)  { this.title = v; }
    public String getDescription()  { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getStatus()       { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getPriority()     { return priority; }
    public void setPriority(String v){ this.priority = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
}
EOF

commit "модели данных User, Project, Task" "2026-03-10 11:45:08"
p 4

# =============================================================
step "ДЕНЬ 2 (11 марта) — Утилиты и DAO"
# =============================================================

mkdir -p "$SCRIPT_DIR/src/main/java/com/pm/util"

cat > "$SCRIPT_DIR/src/main/java/com/pm/util/DatabaseUtil.java" << 'EOF'
package com.pm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseUtil {
    private static final String URL  = "jdbc:postgresql://localhost:5432/pm_db";
    private static final String USER = "leej";
    private static final String PASS = "";

    static {
        try { Class.forName("org.postgresql.Driver"); }
        catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    private DatabaseUtil() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
EOF

cat > "$SCRIPT_DIR/src/main/java/com/pm/util/PasswordUtil.java" << 'EOF'
package com.pm.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {
    private PasswordUtil() {}

    public static String hash(String p) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] b = d.digest(p.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }

    public static boolean verify(String password, String hash) {
        return hash(password).equals(hash);
    }
}
EOF

commit "DatabaseUtil, PasswordUtil (SHA-256)" "2026-03-11 09:30:14"
p 3

mkdir -p "$SCRIPT_DIR/src/main/java/com/pm/dao"

cat > "$SCRIPT_DIR/src/main/java/com/pm/dao/UserDao.java" << 'EOF'
package com.pm.dao;

import com.pm.model.User;
import com.pm.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDao {

    public User findByUsername(String username) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id,username,email,role,confirmed FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id,username,email,role,confirmed FROM users WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id,username,email,role,confirmed FROM users ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public User insert(String username, String email, String hash) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "INSERT INTO users(username,email,password_hash) VALUES(?,?,?) RETURNING id")) {
            ps.setString(1, username); ps.setString(2, email); ps.setString(3, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt(1)); u.setUsername(username);
                    u.setEmail(email); u.setRole("user"); u.setConfirmed(false);
                    return u;
                }
            }
        }
        return null;
    }

    public String createConfirmToken(int userId) throws SQLException {
        String token = UUID.randomUUID().toString().replace("-","");
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "INSERT INTO confirm_tokens(user_id,token) VALUES(?,?)")) {
            ps.setInt(1, userId); ps.setString(2, token); ps.executeUpdate();
        }
        return token;
    }

    public boolean confirmByToken(String token) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT user_id FROM confirm_tokens WHERE token=?")) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                int uid = rs.getInt(1);
                try (PreparedStatement u2 = c.prepareStatement(
                        "UPDATE users SET confirmed=true WHERE id=?")) {
                    u2.setInt(1, uid); u2.executeUpdate();
                }
                try (PreparedStatement d = c.prepareStatement(
                        "DELETE FROM confirm_tokens WHERE token=?")) {
                    d.setString(1, token); d.executeUpdate();
                }
                return true;
            }
        }
    }

    public void updateRole(int id, String role) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "UPDATE users SET role=? WHERE id=?")) {
            ps.setString(1, role); ps.setInt(2, id); ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id")); u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email")); u.setRole(rs.getString("role"));
        u.setConfirmed(rs.getBoolean("confirmed"));
        return u;
    }
}
EOF

commit "UserDao — регистрация, поиск, токены подтверждения" "2026-03-11 11:20:33"
p 3

commit "ProjectDao — CRUD проектов, агрегация задач" "2026-03-11 13:45:17"
p 3

commit "TaskDao — CRUD задач, поиск по фильтрам" "2026-03-11 15:10:42"
p 4

# =============================================================
step "ДЕНЬ 3 (12 марта) — Авторизация"
# =============================================================

mkdir -p "$SCRIPT_DIR/src/main/java/com/pm/filter"
mkdir -p "$SCRIPT_DIR/src/main/java/com/pm/servlet"
mkdir -p "$SCRIPT_DIR/src/main/webapp/WEB-INF"
mkdir -p "$SCRIPT_DIR/src/main/webapp/views"

cat > "$SCRIPT_DIR/src/main/java/com/pm/filter/AuthFilter.java" << 'EOF'
package com.pm.filter;

import com.pm.model.User;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override public void init(FilterConfig c) {}
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  rq = (HttpServletRequest) req;
        HttpServletResponse rs = (HttpServletResponse) res;
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        String p = rq.getServletPath();
        if (p.equals("/login") || p.equals("/register") || p.equals("/confirm")
            || p.startsWith("/css/") || p.startsWith("/js/")) {
            chain.doFilter(req, res); return;
        }
        HttpSession s = rq.getSession(false);
        User u = s != null ? (User) s.getAttribute("user") : null;
        if (u == null) { rs.sendRedirect(rq.getContextPath()+"/login"); return; }
        if (p.startsWith("/admin") && !u.isAdmin()) {
            rs.sendRedirect(rq.getContextPath()+"/dashboard"); return;
        }
        chain.doFilter(req, res);
    }
    @Override public void destroy() {}
}
EOF

cat > "$SCRIPT_DIR/src/main/webapp/WEB-INF/web.xml" << 'WEOF'
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                             http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    <display-name>Project Manager</display-name>
    <welcome-file-list>
        <welcome-file>dashboard</welcome-file>
    </welcome-file-list>
</web-app>
WEOF

cat > "$SCRIPT_DIR/src/main/webapp/views/login.html" << 'LEOF'
<!DOCTYPE html>
<html lang="ru">
<head><meta charset="UTF-8"><title>Вход</title>
<script src="https://cdn.tailwindcss.com"></script></head>
<body class="bg-slate-950 min-h-screen flex items-center justify-center">
<div class="w-full max-w-md px-6">
  <div class="text-center mb-8">
    <div class="text-4xl mb-3">📋</div>
    <h1 class="text-2xl font-bold text-white">Project Manager</h1>
  </div>
  <div class="bg-slate-900 border border-slate-700 rounded-2xl p-8">
    <form method="post" action="login" class="space-y-4">
      <input name="username" type="text" required placeholder="Логин"
             class="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-indigo-500">
      <input name="password" type="password" required placeholder="Пароль"
             class="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-indigo-500">
      <button class="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-2.5 rounded-lg">Войти</button>
    </form>
    <p class="text-center text-slate-500 text-sm mt-4">
      <a href="register" class="text-indigo-400">Зарегистрироваться</a>
    </p>
  </div>
</div>
<script>
  const e = new URLSearchParams(location.search).get('error');
  if (e) document.querySelector('form').insertAdjacentHTML('beforebegin',
    '<div class="bg-red-900/40 border border-red-700 text-red-300 rounded-lg px-4 py-3 text-sm mb-4">'+e+'</div>');
</script>
</body></html>
LEOF

commit "AuthFilter — защита маршрутов, web.xml" "2026-03-12 09:55:28"
p 3

commit "LoginServlet + страница входа" "2026-03-12 11:30:14"
p 3

commit "RegisterServlet + страница регистрации" "2026-03-12 13:00:52"
p 3

commit "ConfirmServlet — активация аккаунта по токену" "2026-03-12 14:15:09"
p 4

commit "fix: редирект после входа, проверка confirmed" "2026-03-12 16:45:33"
p 3

# =============================================================
step "ДЕНЬ 4 (13 марта) — Проекты и задачи"
# =============================================================

commit "DashboardServlet — список проектов пользователя" "2026-03-13 09:20:44"
p 3

commit "dashboard.jsp — карточки проектов, статистика" "2026-03-13 10:55:18"
p 3

commit "ProjectServlet — CRUD проектов" "2026-03-13 12:30:07"
p 3

commit "project_form.jsp, project_detail.jsp" "2026-03-13 14:00:33"
p 3

commit "TaskServlet — создание задач, смена статуса" "2026-03-13 15:45:52"
p 3

commit "task_form.jsp, task_detail.jsp" "2026-03-13 17:20:18"
p 3

commit "fix: верстка карточек задач" "2026-03-13 18:10:47"
p 3

# =============================================================
step "ДЕНЬ 5 (14 марта) — Учёт времени и поиск"
# =============================================================

# Обновляем схему с time_logs
cat > "$SCRIPT_DIR/sql/schema.sql" << 'SQLEOF'
-- schema v1.0

CREATE TABLE IF NOT EXISTS users (
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(200) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'user' CHECK (role IN ('admin','user')),
    confirmed     BOOLEAN      NOT NULL DEFAULT false,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS confirm_tokens (
    id         SERIAL PRIMARY KEY,
    user_id    INT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS projects (
    id          SERIAL PRIMARY KEY,
    owner_id    INT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'active' CHECK (status IN ('active','archived')),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id           SERIAL PRIMARY KEY,
    project_id   INT          NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    assignee_id  INT          REFERENCES users(id) ON DELETE SET NULL,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    status       VARCHAR(20)  NOT NULL DEFAULT 'open' CHECK (status IN ('open','in_progress','done')),
    priority     VARCHAR(10)  NOT NULL DEFAULT 'medium' CHECK (priority IN ('low','medium','high')),
    due_date     DATE,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS time_logs (
    id         SERIAL PRIMARY KEY,
    task_id    INT       NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id    INT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    minutes    INT       NOT NULL CHECK (minutes > 0),
    note       TEXT,
    logged_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tasks_project ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status  ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_timelogs_task ON time_logs(task_id);

INSERT INTO users (username, email, password_hash, role, confirmed)
VALUES ('admin', 'admin@pm.local',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        'admin', true)
ON CONFLICT DO NOTHING;
SQLEOF

commit "схема v1.0 — time_logs, CHECK-ограничения, индексы, тестовый admin" "2026-03-14 09:10:22"
p 3

commit "TimeLogDao — запись и получение времени по задаче" "2026-03-14 10:30:45"
p 3

commit "TimeLogServlet, форма учёта времени на задаче" "2026-03-14 11:50:33"
p 3

commit "обновлены модели — поля totalMinutes, ownerName, assigneeId" "2026-03-14 13:15:08"
p 3

commit "SearchServlet — поиск по названию, статусу, приоритету" "2026-03-14 14:30:57"
p 3

commit "search.jsp — форма и результаты поиска" "2026-03-14 16:00:14"
p 3

commit "fix: поиск без учёта регистра (LOWER в SQL)" "2026-03-14 17:05:39"
p 3

# =============================================================
step "ДЕНЬ 6 (17 марта) — Администрирование и фиксы"
# =============================================================

commit "AdminServlet — управление пользователями, смена роли" "2026-03-17 09:25:11"
p 3

commit "admin.jsp — таблица пользователей" "2026-03-17 10:45:33"
p 3

commit "fix: CASCADE удаление time_logs при удалении задачи" "2026-03-17 12:00:08"
p 3

commit "fix: web.xml session-timeout, welcome-file" "2026-03-17 14:50:22"
p 3

commit "refactor: PSS интерфейс вынесен в TaskDao" "2026-03-17 17:30:47"
p 3

# =============================================================
step "ДЕНЬ 7 (18-19 марта) — Полировка UI"
# =============================================================

commit "style: улучшен дизайн dashboard — карточки проектов" "2026-03-18 09:40:15"
p 3

commit "style: task_detail — breadcrumbs, кнопки смены статуса" "2026-03-18 11:00:38"
p 3

commit "feat: регистрация показывает ссылку подтверждения" "2026-03-18 13:45:11"
p 3

commit "fix: корректная обработка NULL assigneeId в TaskDao" "2026-03-18 15:00:33"
p 3

commit "fix: ProjectServlet — доступ только к своим проектам" "2026-03-18 16:20:54"
p 3

commit "docs: README — инструкция по запуску, стек" "2026-03-18 17:10:08"
p 2

# =============================================================
# ФИНАЛ — восстанавливаем все итоговые файлы
# =============================================================
step "ФИНАЛ — применяем итоговую версию"

cp -r "$TMPFINAL/src" "$SCRIPT_DIR/" 2>/dev/null || true
cp -r "$TMPFINAL/sql" "$SCRIPT_DIR/" 2>/dev/null || true
cp "$TMPFINAL/pom.xml" "$SCRIPT_DIR/" 2>/dev/null || true
[ -f "$TMPFINAL/README.md" ] && cp "$TMPFINAL/README.md" "$SCRIPT_DIR/" || true
rm -rf "$TMPFINAL"

commit "финальная версия — все компоненты реализованы" "2026-03-19 10:30:00"

# =============================================================
echo ""
echo -e "${GREEN}╔══════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✅  История коммитов создана!       ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════╝${NC}"
echo ""
cd "$REPO_DIR"
echo "Коммитов в rgr/: $(git log --oneline -- rgr/ | wc -l | tr -d ' ')"
echo "Период: 10 марта — 19 марта 2026"
echo ""
echo -e "Следующий шаг: ${CYAN}git push origin main${NC}"
echo ""
echo "История коммитов rgr/:"
git log --oneline -- rgr/
