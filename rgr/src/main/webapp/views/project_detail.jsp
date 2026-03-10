<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.pm.model.*,java.util.List" %>
<%
Project project = (Project) request.getAttribute("project");
List<Task> tasks = (List<Task>) request.getAttribute("tasks");
List<User> users = (List<User>) request.getAttribute("users");
User me = (User) request.getAttribute("user");
String ctx = request.getContextPath();
String keyword  = request.getParameter("keyword");
String status   = request.getParameter("status");
String priority = request.getParameter("priority");
int hours = project.getTotalMinutes() / 60;
int mins  = project.getTotalMinutes() % 60;
%>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title><%=project.getName()%> — Project Manager</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-950 min-h-screen text-white">

<nav class="bg-slate-900 border-b border-slate-700 px-6 py-3 flex items-center justify-between">
  <a href="<%=ctx%>/dashboard" class="flex items-center gap-2 font-bold text-lg text-indigo-400">
    <span>📋</span> Project Manager
  </a>
  <div class="flex items-center gap-4">
    <a href="<%=ctx%>/search" class="text-slate-400 hover:text-white text-sm">🔍 Поиск</a>
    <% if (me.isAdmin()) { %><a href="<%=ctx%>/admin" class="text-slate-400 hover:text-white text-sm">⚙️ Админ</a><% } %>
    <a href="<%=ctx%>/logout" class="text-slate-400 hover:text-red-400 text-sm">Выйти</a>
  </div>
</nav>

<main class="max-w-5xl mx-auto px-6 py-8">

  <%-- Заголовок проекта --%>
  <div class="flex items-start justify-between mb-6">
    <div>
      <div class="text-slate-400 text-sm mb-1">
        <a href="<%=ctx%>/dashboard" class="hover:text-white">Проекты</a> / <%=project.getName()%>
      </div>
      <h1 class="text-2xl font-bold"><%=project.getName()%></h1>
      <% if (project.getDescription() != null && !project.getDescription().isBlank()) { %>
      <p class="text-slate-400 text-sm mt-1"><%=project.getDescription()%></p>
      <% } %>
      <div class="flex gap-4 text-sm text-slate-500 mt-2">
        <span>📌 <%=project.getTaskCount()%> задач</span>
        <span>⏱ Итого: <%=hours%>ч <%=mins%>м</span>
        <span class="<%="active".equals(project.getStatus()) ? "text-green-400" : "text-slate-400"%>">
          ● <%="active".equals(project.getStatus()) ? "Активен" : "Архив"%>
        </span>
      </div>
    </div>
    <div class="flex gap-2">
      <a href="<%=ctx%>/projects/<%=project.getId()%>/edit"
         class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-lg text-sm transition">✏️ Редактировать</a>
      <a href="<%=ctx%>/tasks/new?projectId=<%=project.getId()%>"
         class="bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">+ Задача</a>
    </div>
  </div>

  <%-- Поиск / фильтры --%>
  <form method="get" class="bg-slate-900 border border-slate-700 rounded-xl p-4 mb-6 flex flex-wrap gap-3 items-end">
    <input type="hidden" name="id" value="ignored">
    <div class="flex-1 min-w-40">
      <label class="block text-slate-400 text-xs mb-1">Поиск по задачам</label>
      <input name="keyword" type="text" value="<%=keyword != null ? keyword : ""%>"
             placeholder="Название задачи..."
             class="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500">
    </div>
    <div>
      <label class="block text-slate-400 text-xs mb-1">Статус</label>
      <select name="status" class="bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500">
        <option value="">Все</option>
        <option value="open"        <%="open".equals(status) ? "selected":""%>>Открыта</option>
        <option value="in_progress" <%="in_progress".equals(status) ? "selected":""%>>В работе</option>
        <option value="done"        <%="done".equals(status) ? "selected":""%>>Готово</option>
      </select>
    </div>
    <div>
      <label class="block text-slate-400 text-xs mb-1">Приоритет</label>
      <select name="priority" class="bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500">
        <option value="">Все</option>
        <option value="high"   <%="high".equals(priority) ? "selected":""%>>Высокий</option>
        <option value="medium" <%="medium".equals(priority) ? "selected":""%>>Средний</option>
        <option value="low"    <%="low".equals(priority) ? "selected":""%>>Низкий</option>
      </select>
    </div>
    <button type="submit" class="bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg text-sm transition">Найти</button>
    <a href="<%=ctx%>/projects/<%=project.getId()%>" class="text-slate-400 hover:text-white text-sm py-2">Сбросить</a>
  </form>

  <%-- Список задач --%>
  <% if (tasks.isEmpty()) { %>
  <div class="text-center py-16 text-slate-500">
    <div class="text-4xl mb-3">📝</div>
    <p>Задач не найдено</p>
  </div>
  <% } else { %>
  <div class="space-y-2">
    <% for (Task t : tasks) {
       String pColor = "high".equals(t.getPriority()) ? "text-red-400 bg-red-900/30 border-red-800"
                     : "medium".equals(t.getPriority()) ? "text-yellow-400 bg-yellow-900/30 border-yellow-800"
                     : "text-slate-400 bg-slate-800 border-slate-600";
       String sColor = "done".equals(t.getStatus()) ? "text-green-400"
                     : "in_progress".equals(t.getStatus()) ? "text-blue-400" : "text-slate-400";
       String sLabel = "done".equals(t.getStatus()) ? "✅ Готово"
                     : "in_progress".equals(t.getStatus()) ? "🔄 В работе" : "⭕ Открыта";
       int th = t.getTotalMinutes() / 60, tm = t.getTotalMinutes() % 60;
    %>
    <a href="<%=ctx%>/tasks/<%=t.getId()%>"
       class="flex items-center gap-4 bg-slate-900 border border-slate-700 hover:border-indigo-500 rounded-xl px-5 py-4 transition group">
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 mb-1">
          <span class="text-xs border px-2 py-0.5 rounded-full <%=pColor%>"><%=t.getPriority()%></span>
          <% if (t.getAssigneeName() != null) { %>
          <span class="text-xs text-slate-500">@<%=t.getAssigneeName()%></span>
          <% } %>
        </div>
        <p class="font-medium text-sm truncate <%="done".equals(t.getStatus()) ? "line-through text-slate-500" : ""%>"><%=t.getTitle()%></p>
      </div>
      <div class="flex items-center gap-4 text-xs text-slate-500 shrink-0">
        <span class="<%=sColor%>"><%=sLabel%></span>
        <span>⏱ <%=th%>ч <%=tm%>м</span>
        <% if (t.getDueDate() != null) { %><span>📅 <%=t.getDueDate()%></span><% } %>
      </div>
    </a>
    <% } %>
  </div>
  <% } %>
</main>
</body>
</html>
