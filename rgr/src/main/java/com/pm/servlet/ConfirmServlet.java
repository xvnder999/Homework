package com.pm.servlet;

import com.pm.dao.UserDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet("/confirm")
public class ConfirmServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ConfirmServlet.class.getName());
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token = req.getParameter("token");
        if (token == null || token.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/login?error=Недействительная+ссылка");
            return;
        }

        try {
            boolean ok = userDao.confirmByToken(token);
            if (ok) {
                resp.sendRedirect(req.getContextPath() + "/login?success=Аккаунт+подтверждён");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login?error=Ссылка+недействительна+или+устарела");
            }
        } catch (SQLException e) {
            LOGGER.severe("Confirm error: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/login?error=Ошибка+сервера");
        }
    }
}
