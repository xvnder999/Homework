package com.pm.servlet;

import com.pm.dao.UserDao;
import com.pm.model.User;
import com.pm.util.DatabaseUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminServlet.class.getName());
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<User> users = userDao.findAll();
            req.setAttribute("users", users);
            req.setAttribute("user",  req.getSession().getAttribute("user"));
            req.getRequestDispatcher("/views/admin.jsp").forward(req, resp);
        } catch (SQLException e) {
            LOGGER.severe("Admin GET error: " + e.getMessage());
            resp.sendError(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            int    userId = Integer.parseInt(req.getParameter("userId"));

            if ("confirm".equals(action)) {
                forceConfirm(userId);
            } else if ("setAdmin".equals(action)) {
                userDao.updateRole(userId, "admin");
            } else if ("setUser".equals(action)) {
                userDao.updateRole(userId, "user");
            }
            resp.sendRedirect(req.getContextPath() + "/admin");
        } catch (SQLException e) {
            LOGGER.severe("Admin POST error: " + e.getMessage());
            resp.sendError(500);
        }
    }

    private void forceConfirm(int userId) throws SQLException {
        try (Connection c = DatabaseUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET confirmed=true WHERE id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
