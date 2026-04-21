<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.pm.model.Project" %>
<%
Project project = (Project) request.getAttribute("project");
boolean isEdit = project != null;
String ctx = request.getContextPath();
String action = isEdit ? ctx + "/projects/" + project.getId() : ctx + "/projects/new";
String title = isEdit ? "Редактировать проект" : "Новый проект";
String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title><%=title%> — Project Manager</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-950 min-h-screen text-white flex items-center justify-center py-10">
<div class="w-full max-w-lg px-6">
  <a href="<%=ctx%>/dashboard" class="text-slate-400 hover:text-white text-sm mb-6 inline-block">← Назад</a>
  <div class="bg-slate-900 border border-slate-700 rounded-2xl p-8">
    <h2 class="text-xl font-bold mb-6"><%=title%></h2>
    <% if (error != null) { %>
    <div class="bg-red-900/40 border border-red-700 text-red-300 rounded-lg px-4 py-3 text-sm mb-4"><%=error%></div>
    <% } %>
    <form method="post" action="<%=action%>" class="space-y-4">
      <div>
        <label class="block text-slate-400 text-sm mb-1">Название *</label>
        <input name="name" type="text" required
               value="<%=isEdit ? project.getName() : ""%>"
               class="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-indigo-500 transition"
               placeholder="Название проекта">
      </div>
      <div>
        <label class="block text-slate-400 text-sm mb-1">Описание</label>
        <textarea name="description" rows="3"
                  class="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-indigo-500 transition resize-none"
                  placeholder="Краткое описание проекта"><%=isEdit && project.getDescription() != null ? project.getDescription() : ""%></textarea>
      </div>
      <% if (isEdit) { %>
      <div>
        <label class="block text-slate-400 text-sm mb-1">Статус</label>
        <select name="status" class="w-full bg-slate-800 border border-slate-600 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-indigo-500 transition">
          <option value="active" <%="active".equals(project.getStatus()) ? "selected" : ""%>>Активен</option>
          <option value="archived" <%="archived".equals(project.getStatus()) ? "selected" : ""%>>Архив</option>
        </select>
      </div>
      <% } %>
      <div class="flex gap-3 pt-2">
        <button type="submit" class="flex-1 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-2.5 rounded-lg transition">
          <%=isEdit ? "Сохранить" : "Создать"%>
        </button>
        <a href="<%=ctx%>/dashboard" class="flex-1 text-center bg-slate-700 hover:bg-slate-600 text-white font-semibold py-2.5 rounded-lg transition">
          Отмена
        </a>
      </div>
    </form>
    <% if (isEdit) { %>
    <div class="mt-6 pt-6 border-t border-slate-700">
      <form method="post" action="<%=ctx%>/projects/<%=project.getId()%>/delete"
            onsubmit="return confirm('Удалить проект и все задачи?')">
        <button type="submit" class="text-red-400 hover:text-red-300 text-sm transition">🗑 Удалить проект</button>
      </form>
    </div>
    <% } %>
  </div>
</div>
</body>
</html>
