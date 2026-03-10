<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.pm.model.User, com.pm.model.Project, java.util.List" %>
<%
User me = (User) request.getAttribute("user");
List<Project> projects = (List<Project>) request.getAttribute("projects");
String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Дашборд — Project Manager</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-950 min-h-screen text-white">

<%-- NAV --%>
<nav class="bg-slate-900 border-b border-slate-700 px-6 py-3 flex items-center justify-between">
  <a href="<%=ctx%>/dashboard" class="flex items-center gap-2 font-bold text-lg text-indigo-400">
    <span>📋</span> Project Manager
  </a>
  <div class="flex items-center gap-4">
    <a href="<%=ctx%>/search" class="text-slate-400 hover:text-white text-sm transition">🔍 Поиск</a>
    <% if (me.isAdmin()) { %>
    <a href="<%=ctx%>/admin" class="text-slate-400 hover:text-white text-sm transition">⚙️ Админ</a>
    <% } %>
    <span class="text-slate-500 text-sm"><%=me.getUsername()%>
      <span class="text-xs bg-indigo-800 text-indigo-300 px-2 py-0.5 rounded-full ml-1"><%=me.getRole()%></span>
    </span>
    <a href="<%=ctx%>/logout" class="text-slate-400 hover:text-red-400 text-sm transition">Выйти</a>
  </div>
</nav>

<main class="max-w-5xl mx-auto px-6 py-8">
  <div class="flex items-center justify-between mb-8">
    <div>
      <h1 class="text-2xl font-bold">Мои проекты</h1>
      <p class="text-slate-400 text-sm mt-1">Всего: <%=projects.size()%></p>
    </div>
    <a href="<%=ctx%>/projects/new"
       class="bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg text-sm font-semibold transition">
      + Новый проект
    </a>
  </div>

  <% if (projects.isEmpty()) { %>
  <div class="text-center py-20 text-slate-500">
    <div class="text-5xl mb-4">📂</div>
    <p class="text-lg">Проектов пока нет</p>
    <a href="<%=ctx%>/projects/new" class="text-indigo-400 hover:text-indigo-300 text-sm mt-2 inline-block">Создать первый проект →</a>
  </div>
  <% } else { %>
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
    <% for (Project p : projects) {
       String statusColor = "active".equals(p.getStatus()) ? "bg-green-900/40 text-green-400 border-green-800" : "bg-slate-700 text-slate-400 border-slate-600";
       int hours = p.getTotalMinutes() / 60;
       int mins  = p.getTotalMinutes() % 60;
    %>
    <a href="<%=ctx%>/projects/<%=p.getId()%>"
       class="bg-slate-900 border border-slate-700 hover:border-indigo-500 rounded-xl p-5 transition block">
      <div class="flex items-start justify-between mb-3">
        <h3 class="font-semibold text-base leading-tight"><%=p.getName()%></h3>
        <span class="text-xs border px-2 py-0.5 rounded-full ml-2 shrink-0 <%=statusColor%>">
          <%="active".equals(p.getStatus()) ? "Активен" : "Архив"%>
        </span>
      </div>
      <% if (p.getDescription() != null && !p.getDescription().isBlank()) { %>
      <p class="text-slate-400 text-sm mb-4 line-clamp-2"><%=p.getDescription()%></p>
      <% } %>
      <div class="flex gap-4 text-xs text-slate-500 mt-auto">
        <span>📌 <%=p.getTaskCount()%> задач</span>
        <span>⏱ <%=hours%>ч <%=mins%>м</span>
        <% if (me.isAdmin()) { %><span class="text-slate-600">@<%=p.getOwnerName()%></span><% } %>
      </div>
    </a>
    <% } %>
  </div>
  <% } %>
</main>
</body>
</html>
