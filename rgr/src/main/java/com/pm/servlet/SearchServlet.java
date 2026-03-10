package com.pm.servlet;

import com.pm.dao.TaskDao;
import com.pm.model.Task;
import com.pm.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());
    private final TaskDao taskDao = new TaskDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user     = (User) req.getSession().getAttribute("user");
        String keyword  = req.getParameter("keyword");
        String status   = req.getParameter("status");
        String priority = req.getParameter("priority");

        try {
            List<Task> tasks = taskDao.searchAll(keyword, status, priority, user.getId(), user.isAdmin());
            req.setAttribute("tasks",    tasks);
            req.setAttribute("keyword",  keyword);
            req.setAttribute("status",   status);
            req.setAttribute("priority", priority);
            req.setAttribute("user",     user);
            req.getRequestDispatcher("/views/search.jsp").forward(req, resp);
        } catch (SQLException e) {
            LOGGER.severe("Search error: " + e.getMessage());
            resp.sendError(500);
        }
    }
}
