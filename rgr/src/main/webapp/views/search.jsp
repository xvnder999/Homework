<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.pm.model.*,java.util.List" %>
<%
List<Task> tasks = (List<Task>) request.getAttribute("tasks");
User me = (User) request.getAttribute("user");
String keyword  = (String) request.getAttribute("keyword");
String status   = (String) request.getAttribute("status");
String priority = (String) request.getAttribute("priority");
String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title>Поиск задач — Project Manager</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-950 min-h-screen text-white">

<nav class="bg-slate-900 border-b border-slate-700 px-6 py-3 flex items-center justify-between">
  <a href="<%=ctx%>/dashboard" class="flex items-center gap-2 font-bold text-lg text-indigo-400"><span>📋</span> Project Manager</a>
  <div class="flex items-center gap-4">
    <% if (me.isAdmin()) { %><a href="<%=ctx%>/admin" class="text-slate-400 hover:text-white text-sm">⚙️ Админ</a><% } %>
    <a href="<%=ctx%>/logout" class="text-slate-400 hover:text-red-400 text-sm">Выйти</a>
  </div>
</nav>

<main class="max-w-4xl mx-auto px-6 py-8">
  <h1 class="text-2xl font-bold mb-6">🔍 Поиск задач</h1>

  <form method="get" class="bg-slate-900 border border-slate-700 rounded-xl p-5 mb-6 flex flex-wrap gap-3 items-end">
    <div class="flex-1 min-w-48">
      <label class="block text-slate-400 text-xs mb-1">Ключевое слово</label>
      <input name="keyword" type="text" value="<%=keyword != null ? keyword : ""%>"
             placeholder="Название задачи..."
             class="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500">
    </div>
    <div>
      <label class="block text-slate-400 text-xs mb-1">Статус</label>
      <select name="status" class="bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500">
        <option value="">Все</option>
        <option value="open"        <%="open".equals(status)?"selected":""%>>Открыта</option>
        <option value="in_progress" <%="in_progress".equals(status)?"selected":""%>>В работе</option>
        <option value="done"        <%="done".equals(status)?"selected":""%>>Готово</option>
      </select>
    </div>
    <div>
      <label class="block text-slate-400 text-xs mb-1">Приоритет</label>
      <select name="priority" class="bg-slate-800 border border-slate-600 rounded-lg px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500">
        <option value="">Все</option>
        <option value="high"   <%="high".equals(priority)?"selected":""%>>Высокий</option>
        <option value="medium" <%="medium".equals(priority)?"selected":""%>>Средний</option>
        <option value="low"    <%="low".equals(priority)?"selected":""%>>Низкий</option>
      </select>
    </div>
    <button type="submit" class="bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-2 rounded-lg text-sm transition font-semibold">
      Найти
    </button>
  </form>

  <% if (tasks == null || tasks.isEmpty()) { %>
  <div class="text-center py-16 text-slate-500">
    <div class="text-4xl mb-3">🔍</div>
    <p><%=tasks == null ? "Введите запрос для поиска" : "Задачи не найдены"%></p>
  </div>
  <% } else { %>
  <p class="text-slate-500 text-sm mb-4">Найдено: <%=tasks.size()%></p>
  <div class="space-y-2">
    <% for (Task t : tasks) {
       String pColor = "high".equals(t.getPriority()) ? "text-red-400 bg-red-900/30 border-red-800"
                     : "medium".equals(t.getPriority()) ? "text-yellow-400 bg-yellow-900/30 border-yellow-800"
                     : "text-slate-400 bg-slate-800 border-slate-600";
       String sLabel = "done".equals(t.getStatus()) ? "✅ Готово"
                     : "in_progress".equals(t.getStatus()) ? "🔄 В работе" : "⭕ Открыта";
    %>
    <a href="<%=ctx%>/tasks/<%=t.getId()%>"
       class="flex items-center gap-4 bg-slate-900 border border-slate-700 hover:border-indigo-500 rounded-xl px-5 py-4 transition">
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 mb-1">
          <span class="text-xs border px-2 py-0.5 rounded-full <%=pColor%>"><%=t.getPriority()%></span>
          <span class="text-xs text-indigo-400"><%=t.getProjectName()%></span>
        </div>
        <p class="font-medium text-sm truncate"><%=t.getTitle()%></p>
      </div>
      <span class="text-sm text-slate-400 shrink-0"><%=sLabel%></span>
    </a>
    <% } %>
  </div>
  <% } %>
</main>
</body>
</html>
