package com.pm.servlet;

import com.pm.dao.ProjectDao;
import com.pm.dao.TaskDao;
import com.pm.dao.UserDao;
import com.pm.model.Project;
import com.pm.model.Task;
import com.pm.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/projects/*")
public class ProjectServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProjectServlet.class.getName());
    private final ProjectDao projectDao = new ProjectDao();
    private final TaskDao    taskDao    = new TaskDao();
    private final UserDao    userDao    = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo(); // null | /new | /{id} | /{id}/edit
        User user = (User) req.getSession().getAttribute("user");

        try {
            if (path == null || path.equals("/")) {
                resp.sendRedirect(req.getContextPath() + "/dashboard");
                return;
            }

            if (path.equals("/new")) {
                req.getRequestDispatcher("/views/project_form.jsp").forward(req, resp);
                return;
            }

            String[] parts = path.split("/");
            int id = Integer.parseInt(parts[1]);
            Project project = projectDao.findById(id);

            if (project == null) { resp.sendError(404); return; }
            if (!user.isAdmin() && project.getOwnerId() != user.getId()) {
                resp.sendError(403); return;
            }

            if (parts.length == 3 && parts[2].equals("edit")) {
                req.setAttribute("project", project);
                req.getRequestDispatcher("/views/project_form.jsp").forward(req, resp);
                return;
            }

            // project detail page
            String keyword  = req.getParameter("keyword");
            String status   = req.getParameter("status");
            String priority = req.getParameter("priority");

            List<Task> tasks = (keyword != null || status != null || priority != null)
                    ? taskDao.search(id, keyword, status, priority)
                    : taskDao.findByProject(id);

            List<User> users = userDao.findAll();

            req.setAttribute("project",  project);
            req.setAttribute("tasks",    tasks);
            req.setAttribute("users",    users);
            req.setAttribute("user",     user);
            req.setAttribute("keyword",  keyword);
            req.setAttribute("status",   status);
            req.setAttribute("priority", priority);
            req.getRequestDispatcher("/views/project_detail.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(400);
        } catch (SQLException e) {
            LOGGER.severe("Project GET error: " + e.getMessage());
            resp.sendError(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        User user = (User) req.getSession().getAttribute("user");

        try {
            if (path == null || path.equals("/new") || path.equals("/")) {
                String name = req.getParameter("name");
                String desc = req.getParameter("description");
                if (isBlank(name)) {
                    resp.sendRedirect(req.getContextPath() + "/projects/new?error=Название+обязательно");
                    return;
                }
                Project p = projectDao.insert(user.getId(), name, desc);
                resp.sendRedirect(req.getContextPath() + "/projects/" + p.getId());
                return;
            }

            String[] parts = path.split("/");
            int id = Integer.parseInt(parts[1]);
            Project project = projectDao.findById(id);

            if (project == null) { resp.sendError(404); return; }
            if (!user.isAdmin() && project.getOwnerId() != user.getId()) {
                resp.sendError(403); return;
            }

            if (parts.length == 3 && parts[2].equals("delete")) {
                projectDao.delete(id);
                resp.sendRedirect(req.getContextPath() + "/dashboard");
                return;
            }

            // update
            String name   = req.getParameter("name");
            String desc   = req.getParameter("description");
            String status = req.getParameter("status");
            projectDao.update(id, name, desc, status);
            resp.sendRedirect(req.getContextPath() + "/projects/" + id);

        } catch (NumberFormatException e) {
            resp.sendError(400);
        } catch (SQLException e) {
            LOGGER.severe("Project POST error: " + e.getMessage());
            resp.sendError(500);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
