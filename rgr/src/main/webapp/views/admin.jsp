<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.pm.model.*,java.util.List" %>
<%
List<User> users = (List<User>) request.getAttribute("users");
User me = (User) request.getAttribute("user");
String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title>Администрирование — Project Manager</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-950 min-h-screen text-white">

<nav class="bg-slate-900 border-b border-slate-700 px-6 py-3 flex items-center justify-between">
  <a href="<%=ctx%>/dashboard" class="flex items-center gap-2 font-bold text-lg text-indigo-400"><span>📋</span> Project Manager</a>
  <div class="flex items-center gap-4">
    <a href="<%=ctx%>/dashboard" class="text-slate-400 hover:text-white text-sm">Проекты</a>
    <a href="<%=ctx%>/logout" class="text-slate-400 hover:text-red-400 text-sm">Выйти</a>
  </div>
</nav>

<main class="max-w-4xl mx-auto px-6 py-8">
  <h1 class="text-2xl font-bold mb-6">⚙️ Управление пользователями</h1>

  <div class="bg-slate-900 border border-slate-700 rounded-xl overflow-hidden">
    <table class="w-full text-sm">
      <thead>
        <tr class="border-b border-slate-700 text-slate-400 text-xs uppercase">
          <th class="px-5 py-3 text-left">ID</th>
          <th class="px-5 py-3 text-left">Логин</th>
          <th class="px-5 py-3 text-left">Email</th>
          <th class="px-5 py-3 text-left">Роль</th>
          <th class="px-5 py-3 text-left">Статус</th>
          <th class="px-5 py-3 text-left">Действия</th>
        </tr>
      </thead>
      <tbody>
        <% for (User u : users) { %>
        <tr class="border-b border-slate-800 hover:bg-slate-800/50">
          <td class="px-5 py-3 text-slate-500"><%=u.getId()%></td>
          <td class="px-5 py-3 font-medium"><%=u.getUsername()%></td>
          <td class="px-5 py-3 text-slate-400"><%=u.getEmail()%></td>
          <td class="px-5 py-3">
            <span class="text-xs px-2 py-0.5 rounded-full <%="admin".equals(u.getRole()) ? "bg-indigo-800 text-indigo-300" : "bg-slate-700 text-slate-300"%>">
              <%=u.getRole()%>
            </span>
          </td>
          <td class="px-5 py-3">
            <span class="text-xs <%=u.isConfirmed() ? "text-green-400" : "text-yellow-400"%>">
              <%=u.isConfirmed() ? "✅ Подтверждён" : "⏳ Не подтверждён"%>
            </span>
          </td>
          <td class="px-5 py-3">
            <div class="flex gap-2">
              <% if (!u.isConfirmed()) { %>
              <form method="post" action="<%=ctx%>/admin">
                <input type="hidden" name="action" value="confirm">
                <input type="hidden" name="userId" value="<%=u.getId()%>">
                <button type="submit" class="text-xs text-green-400 hover:text-green-300 transition">Подтвердить</button>
              </form>
              <% } %>
              <% if (u.getId() != me.getId()) { %>
              <form method="post" action="<%=ctx%>/admin">
                <input type="hidden" name="action" value="<%="admin".equals(u.getRole()) ? "setUser" : "setAdmin"%>">
                <input type="hidden" name="userId" value="<%=u.getId()%>">
                <button type="submit" class="text-xs text-indigo-400 hover:text-indigo-300 transition">
                  <%="admin".equals(u.getRole()) ? "→ User" : "→ Admin"%>
                </button>
              </form>
              <% } %>
            </div>
          </td>
        </tr>
        <% } %>
      </tbody>
    </table>
  </div>
</main>
</body>
</html>
