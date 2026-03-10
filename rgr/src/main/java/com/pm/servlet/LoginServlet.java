package com.pm.servlet;

import com.pm.dao.UserDao;
import com.pm.model.User;
import com.pm.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/views/login.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User user = userDao.findByUsername(username);
            if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
                resp.sendRedirect(req.getContextPath() + "/login?error=Неверный+логин+или+пароль");
                return;
            }
            if (!user.isConfirmed()) {
                resp.sendRedirect(req.getContextPath() + "/login?error=Аккаунт+не+подтверждён");
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            LOGGER.info("Login: " + username);
            resp.sendRedirect(req.getContextPath() + "/dashboard");

        } catch (SQLException e) {
            LOGGER.severe("Login error: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/login?error=Ошибка+сервера");
        }
    }
}
