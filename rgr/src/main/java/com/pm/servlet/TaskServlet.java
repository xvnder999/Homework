package com.pm.servlet;

import com.pm.dao.TaskDao;
import com.pm.dao.TimeLogDao;
import com.pm.dao.UserDao;
import com.pm.dao.ProjectDao;
import com.pm.model.Project;
import com.pm.model.Task;
import com.pm.model.TimeLog;
import com.pm.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/tasks/*")
public class TaskServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TaskServlet.class.getName());
    private final TaskDao    taskDao    = new TaskDao();
    private final TimeLogDao timeLogDao = new TimeLogDao();
    private final UserDao    userDao    = new UserDao();
    private final ProjectDao projectDao = new ProjectDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        User user = (User) req.getSession().getAttribute("user");

        try {
            if (path == null || path.equals("/")) {
                resp.sendRedirect(req.getContextPath() + "/dashboard");
                return;
            }

            if (path.equals("/new")) {
                String projectId = req.getParameter("projectId");
                if (projectId == null) { resp.sendError(400); return; }
                Project project = projectDao.findById(Integer.parseInt(projectId));
                if (project == null) { resp.sendError(404); return; }
                List<User> users = userDao.findAll();
                req.setAttribute("project", project);
                req.setAttribute("users",   users);
                req.getRequestDispatcher("/views/task_form.jsp").forward(req, resp);
                return;
            }

            String[] parts = path.split("/");
            int id = Integer.parseInt(parts[1]);
            Task task = taskDao.findById(id);
            if (task == null) { resp.sendError(404); return; }

            if (parts.length == 3 && parts[2].equals("edit")) {
                List<User> users = userDao.findAll();
                req.setAttribute("task",  task);
                req.setAttribute("users", users);
                req.getRequestDispatcher("/views/task_form.jsp").forward(req, resp);
                return;
            }

            List<TimeLog> logs = timeLogDao.findByTask(id);
            req.setAttribute("task",  task);
            req.setAttribute("logs",  logs);
            req.setAttribute("user",  user);
            req.getRequestDispatcher("/views/task_detail.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(400);
        } catch (SQLException e) {
            LOGGER.severe("Task GET error: " + e.getMessage());
            resp.sendError(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        User user = (User) req.getSession().getAttribute("user");

        try {
            // создание новой задачи
            if (path == null || path.equals("/new") || path.equals("/")) {
                int    projectId  = Integer.parseInt(req.getParameter("projectId"));
                String title      = req.getParameter("title");
                String desc       = req.getParameter("description");
                String priority   = req.getParameter("priority");
                String dueDate    = req.getParameter("dueDate");
                String assigneeStr = req.getParameter("assigneeId");
                Integer assigneeId = (assigneeStr != null && !assigneeStr.isBlank())
                        ? Integer.parseInt(assigneeStr) : null;

                if (isBlank(title)) {
                    resp.sendRedirect(req.getContextPath() + "/tasks/new?projectId=" + projectId + "&error=Название+обязательно");
                    return;
                }
                Task task = taskDao.insert(projectId, assigneeId, title, desc, priority, dueDate);
                resp.sendRedirect(req.getContextPath() + "/tasks/" + task.getId());
                return;
            }

            String[] parts = path.split("/");
            int id = Integer.parseInt(parts[1]);
            Task task = taskDao.findById(id);
            if (task == null) { resp.sendError(404); return; }

            // логирование времени
            if (parts.length == 3 && parts[2].equals("log")) {
                String minStr = req.getParameter("minutes");
                String note   = req.getParameter("note");
                if (isBlank(minStr)) {
                    resp.sendRedirect(req.getContextPath() + "/tasks/" + id + "?error=Укажите+минуты");
                    return;
                }
                int minutes = Integer.parseInt(minStr);
                if (minutes <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/tasks/" + id + "?error=Минуты+должны+быть+больше+0");
                    return;
                }
                timeLogDao.insert(id, user.getId(), minutes, note);
                resp.sendRedirect(req.getContextPath() + "/tasks/" + id);
                return;
            }

            // удаление задачи
            if (parts.length == 3 && parts[2].equals("delete")) {
                int projectId = task.getProjectId();
                taskDao.delete(id);
                resp.sendRedirect(req.getContextPath() + "/projects/" + projectId);
                return;
            }

            // изменение статуса (быстрое)
            if (parts.length == 3 && parts[2].equals("status")) {
                String status = req.getParameter("status");
                taskDao.updateStatus(id, status);
                resp.sendRedirect(req.getContextPath() + "/tasks/" + id);
                return;
            }

            // обновление задачи
            String title      = req.getParameter("title");
            String desc       = req.getParameter("description");
            String priority   = req.getParameter("priority");
            String status     = req.getParameter("status");
            String dueDate    = req.getParameter("dueDate");
            String assigneeStr = req.getParameter("assigneeId");
            Integer assigneeId = (assigneeStr != null && !assigneeStr.isBlank())
                    ? Integer.parseInt(assigneeStr) : null;

            taskDao.update(id, title, desc, priority, status, assigneeId, dueDate);
            resp.sendRedirect(req.getContextPath() + "/tasks/" + id);

        } catch (NumberFormatException e) {
            resp.sendError(400);
        } catch (SQLException e) {
            LOGGER.severe("Task POST error: " + e.getMessage());
            resp.sendError(500);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
