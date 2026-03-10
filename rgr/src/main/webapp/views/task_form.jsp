<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.pm.model.*,java.util.List" %>
<%
Task task      = (Task) request.getAttribute("task");
Project project = (Project) request.getAttribute("project");
List<User> users = (List<User>) request.getAttribute("users");
boolean isEdit = task != null;
String ctx = request.getContextPath();
int projectId = isEdit ? task.getProjectId() : (project != null ? project.getId() : 0);
String action = isEdit ? ctx + "/tasks/" + task.getId() : ctx + "/tasks/new";
String pageTitle = isEdit ? "Редактировать задачу" : "Новая задача";
String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title><%=pageTitle%> — Project Manager</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-950 min-h-screen text-white flex items-center justify-center py-10">
<div class="w-full max-w-lg px-6">
  <a href="<%=ctx%>/projects/<%=projectId%>" class="text-slate-400 hover:text-white text-sm mb-6 inline-block">← К проекту</a>
  <div class="bg-slate-900 border border-slate-700 rounded-2xl p-8">
    <h2 class="text-xl font-bold mb-6"><%=pageTitle%></h2>
    <% if (error != null) { %>
    <div class="bg-red-900/40 border border-red-700 text-red-300 rounded-lg px-4 py-3 text-sm mb-4"><%=error%></div>
    <% } %>
    <form method="post" action="<%=action%>" class="space-y-4">
      <input type="hidden" name="projectId" value="<%=projectId%>">
      <div>
        <label class="block text-slate-400 text-sm mb-1">Название *</label>
        <input name="title" type="text" required
               value="<%=isEdit ? task.getTitle() : ""%>"
               class="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-indigo-500 transition"
               placeholder="Что нужно сделать?">
      </div>
      <div>
        <label class="block text-slate-400 text-sm mb-1">Описание</label>
        <textarea name="description" rows="3"
                  class="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-indigo-500 transition resize-none"
                  placeholder="Подробное описание задачи"><%=isEdit && task.getDescription() != null ? task.getDescription() : ""%></textarea>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-slate-400 text-sm mb-1">Приоритет</label>
          <select name="priority" class="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:border-indigo-500 transition">
            <option value="low"    <%=isEdit && "low".equals(task.getPriority()) ? "selected":""%>>Низкий</option>
            <option value="medium" <%=!isEdit || "medium".equals(task.getPriority()) ? "selected":""%>>Средний</option>
            <option value="high"   <%=isEdit && "high".equals(task.getPriority()) ? "selected":""%>>Высокий</option>
          </select>
        </div>
        <% if (isEdit) { %>
        <div>
          <label class="block text-slate-400 text-sm mb-1">Статус</label>
          <select name="status" class="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:border-indigo-500 transition">
            <option value="open"        <%="open".equals(task.getStatus()) ? "selected":""%>>Открыта</option>
            <option value="in_progress" <%="in_progress".equals(task.getStatus()) ? "selected":""%>>В работе</option>
            <option value="done"        <%="done".equals(task.getStatus()) ? "selected":""%>>Готово</option>
          </select>
        </div>
        <% } %>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-slate-400 text-sm mb-1">Исполнитель</label>
          <select name="assigneeId" class="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:border-indigo-500 transition">
            <option value="">Не назначен</option>
            <% for (User u : users) { %>
            <option value="<%=u.getId()%>" <%=isEdit && task.getAssigneeId() != null && task.getAssigneeId() == u.getId() ? "selected":""%>>
              <%=u.getUsername()%>
            </option>
            <% } %>
          </select>
        </div>
        <div>
          <label class="block text-slate-400 text-sm mb-1">Дедлайн</label>
          <input name="dueDate" type="date"
                 value="<%=isEdit && task.getDueDate() != null ? task.getDueDate().toString() : ""%>"
                 class="w-full bg-slate-800 border border-slate-600 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:border-indigo-500 transition">
        </div>
      </div>
      <div class="flex gap-3 pt-2">
        <button type="submit" class="flex-1 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-2.5 rounded-lg transition">
          <%=isEdit ? "Сохранить" : "Создать"%>
        </button>
        <a href="<%=ctx%>/projects/<%=projectId%>" class="flex-1 text-center bg-slate-700 hover:bg-slate-600 text-white font-semibold py-2.5 rounded-lg transition">Отмена</a>
      </div>
    </form>
    <% if (isEdit) { %>
    <div class="mt-6 pt-6 border-t border-slate-700">
      <form method="post" action="<%=ctx%>/tasks/<%=task.getId()%>/delete"
            onsubmit="return confirm('Удалить задачу?')">
        <button type="submit" class="text-red-400 hover:text-red-300 text-sm transition">🗑 Удалить задачу</button>
      </form>
    </div>
    <% } %>
  </div>
</div>
</body>
</html>
