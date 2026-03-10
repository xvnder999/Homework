package com.pm.servlet;

import com.pm.dao.ProjectDao;
import com.pm.model.Project;
import com.pm.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());
    private final ProjectDao projectDao = new ProjectDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("user");

        try {
            List<Project> projects = user.isAdmin()
                    ? projectDao.findAll()
                    : projectDao.findByOwner(user.getId());

            req.setAttribute("projects", projects);
            req.setAttribute("user", user);
            req.getRequestDispatcher("/views/dashboard.jsp").forward(req, resp);

        } catch (SQLException e) {
            LOGGER.severe("Dashboard error: " + e.getMessage());
            resp.sendError(500, "Ошибка загрузки проектов");
        }
    }
}
