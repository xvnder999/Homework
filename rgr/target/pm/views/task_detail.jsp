<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.pm.model.*,java.util.List" %>
<%
Task task = (Task) request.getAttribute("task");
List<TimeLog> logs = (List<TimeLog>) request.getAttribute("logs");
User me = (User) request.getAttribute("user");
String ctx = request.getContextPath();
int hours = task.getTotalMinutes() / 60;
int mins  = task.getTotalMinutes() % 60;
String pColor = "high".equals(task.getPriority()) ? "text-red-400 bg-red-900/30 border-red-800"
              : "medium".equals(task.getPriority()) ? "text-yellow-400 bg-yellow-900/30 border-yellow-800"
              : "text-slate-400 bg-slate-800 border-slate-600";
String sLabel = "done".equals(task.getStatus()) ? "✅ Готово"
              : "in_progress".equals(task.getStatus()) ? "🔄 В работе" : "⭕ Открыта";
String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title><%=task.getTitle()%> — Project Manager</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-950 min-h-screen text-white">

<nav class="bg-slate-900 border-b border-slate-700 px-6 py-3 flex items-center justify-between">
  <a href="<%=ctx%>/dashboard" class="flex items-center gap-2 font-bold text-lg text-indigo-400"><span>📋</span> Project Manager</a>
  <div class="flex items-center gap-4">
    <a href="<%=ctx%>/search" class="text-slate-400 hover:text-white text-sm">🔍 Поиск</a>
    <a href="<%=ctx%>/logout" class="text-slate-400 hover:text-red-400 text-sm">Выйти</a>
  </div>
</nav>

<main class="max-w-3xl mx-auto px-6 py-8">

  <%-- breadcrumb --%>
  <div class="text-slate-400 text-sm mb-4">
    <a href="<%=ctx%>/dashboard" class="hover:text-white">Проекты</a> /
    <a href="<%=ctx%>/projects/<%=task.getProjectId()%>" class="hover:text-white"><%=task.getProjectName()%></a> /
    <%=task.getTitle()%>
  </div>

  <%-- Карточка задачи --%>
  <div class="bg-slate-900 border border-slate-700 rounded-2xl p-6 mb-6">
    <div class="flex items-start justify-between mb-4">
      <div class="flex-1">
        <div class="flex items-center gap-2 mb-2">
          <span class="text-xs border px-2 py-0.5 rounded-full <%=pColor%>"><%=task.getPriority()%></span>
          <span class="text-sm text-slate-400"><%=sLabel%></span>
        </div>
        <h1 class="text-xl font-bold <%="done".equals(task.getStatus()) ? "line-through text-slate-500" : ""%>">
          <%=task.getTitle()%>
        </h1>
      </div>
      <a href="<%=ctx%>/tasks/<%=task.getId()%>/edit"
         class="bg-slate-700 hover:bg-slate-600 text-white px-3 py-2 rounded-lg text-sm ml-4 transition">✏️</a>
    </div>

    <% if (task.getDescription() != null && !task.getDescription().isBlank()) { %>
    <p class="text-slate-300 text-sm mb-4 whitespace-pre-wrap"><%=task.getDescription()%></p>
    <% } %>

    <div class="grid grid-cols-2 gap-3 text-sm border-t border-slate-700 pt-4">
      <div><span class="text-slate-500">Исполнитель: </span>
        <span class="text-white"><%=task.getAssigneeName() != null ? task.getAssigneeName() : "Не назначен"%></span>
      </div>
      <div><span class="text-slate-500">Дедлайн: </span>
        <span class="text-white"><%=task.getDueDate() != null ? task.getDueDate().toString() : "—"%></span>
      </div>
      <div><span class="text-slate-500">Проект: </span>
        <a href="<%=ctx%>/projects/<%=task.getProjectId()%>" class="text-indigo-400 hover:text-indigo-300"><%=task.getProjectName()%></a>
      </div>
      <div><span class="text-slate-500">Итого времени: </span>
        <span class="text-white font-semibold"><%=hours%>ч <%=mins%>м</span>
      </div>
    </div>

    <%-- Быстрое изменение статуса --%>
    <div class="flex gap-2 mt-4 pt-4 border-t border-slate-700">
      <span class="text-slate-500 text-sm self-center">Статус:</span>
      <% String[] statuses = {"open","in_progress","done"};
         String[] sLabels  = {"Открыта","В работе","Готово"};
         for (int i = 0; i < statuses.length; i++) { %>
      <form method="post" action="<%=ctx%>/tasks/<%=task.getId()%>/status">
        <input type="hidden" name="status" value="<%=statuses[i]%>">
        <button type="submit"
                class="text-xs px-3 py-1.5 rounded-lg transition <%=statuses[i].equals(task.getStatus()) ? "bg-indigo-600 text-white" : "bg-slate-700 hover:bg-slate-600 text-slate-300"%>">
          <%=sLabels[i]%>
        </button>
      </form>
      <% } %>
    </div>
  </div>

  <%-- Учёт времени --%>
  <div class="bg-slate-900 border border-slate-700 rounded-2xl p-6">
    <h2 class="font-semibold text-base mb-4">⏱ Учёт времени</h2>

    <% if (error != null) { %>
    <div class="bg-red-900/40 border border-red-700 text-red-300 rounded-lg px-4 py-3 text-sm mb-4"><%=error%></div>
    <% } %>

    <form method="post" action="<%=ctx%>/tasks/<%=task.getId()%>/log" class="flex gap-3 mb-6">
      <div class="w-28">
        <label class="block text-slate-400 text-xs mb-1">Минуты *</label>
        <input name="minutes" type="number" min="1" required
               class="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500"
               placeholder="30">
      </div>
      <div class="flex-1">
        <label class="block text-slate-400 text-xs mb-1">Комментарий</label>
        <input name="note" type="text"
               class="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500"
               placeholder="Что было сделано...">
      </div>
      <div class="self-end">
        <button type="submit" class="bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg text-sm transition">
          Добавить
        </button>
      </div>
    </form>

    <% if (logs.isEmpty()) { %>
    <p class="text-slate-500 text-sm text-center py-4">Время ещё не логировалось</p>
    <% } else { %>
    <div class="space-y-2">
      <% for (TimeLog log : logs) {
         int lh = log.getMinutes() / 60, lm = log.getMinutes() % 60;
      %>
      <div class="flex items-center gap-3 text-sm bg-slate-800/50 rounded-lg px-4 py-3">
        <span class="font-semibold text-indigo-300 w-16 shrink-0"><%=lh > 0 ? lh+"ч " : ""%><%=lm%>м</span>
        <span class="text-slate-400 flex-1"><%=log.getNote() != null ? log.getNote() : ""%></span>
        <span class="text-slate-600 text-xs shrink-0">@<%=log.getUsername()%></span>
        <span class="text-slate-600 text-xs shrink-0"><%=log.getLoggedAt().toString().substring(0,16)%></span>
        <form method="post" action="<%=ctx%>/timelogs/<%=log.getId()%>">
          <input type="hidden" name="action" value="delete">
          <input type="hidden" name="taskId" value="<%=task.getId()%>">
          <button type="submit" class="text-slate-600 hover:text-red-400 transition text-xs">✕</button>
        </form>
      </div>
      <% } %>
    </div>
    <% } %>
  </div>

</main>
</body>
</html>
