package com.pm.servlet;

import com.pm.dao.UserDao;
import com.pm.model.User;
import com.pm.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/views/register.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String email    = req.getParameter("email");
        String password = req.getParameter("password");
        String confirm  = req.getParameter("confirm");

        try {
            if (isBlank(username) || isBlank(email) || isBlank(password)) {
                redirect(resp, req, "Все поля обязательны");
                return;
            }
            if (!password.equals(confirm)) {
                redirect(resp, req, "Пароли не совпадают");
                return;
            }
            if (userDao.findByUsername(username) != null) {
                redirect(resp, req, "Имя пользователя уже занято");
                return;
            }
            if (userDao.findByEmail(email) != null) {
                redirect(resp, req, "Email уже зарегистрирован");
                return;
            }

            User user = userDao.insert(username, email, PasswordUtil.hash(password));
            String token = userDao.createConfirmToken(user.getId());

            String confirmUrl = req.getScheme() + "://" + req.getServerName()
                    + ":" + req.getServerPort()
                    + req.getContextPath() + "/confirm?token=" + token;

            LOGGER.info("Confirm URL for " + username + ": " + confirmUrl);

            resp.sendRedirect(req.getContextPath() + "/register?success=1&token=" + token);

        } catch (SQLException e) {
            LOGGER.severe("Register error: " + e.getMessage());
            redirect(resp, req, "Ошибка сервера. Попробуйте позже.");
        }
    }

    private void redirect(HttpServletResponse resp, HttpServletRequest req, String error)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + "/register?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
