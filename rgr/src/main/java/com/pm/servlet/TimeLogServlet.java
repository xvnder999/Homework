package com.pm.servlet;

import com.pm.dao.TimeLogDao;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet("/timelogs/*")
public class TimeLogServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TimeLogServlet.class.getName());
    private final TimeLogDao timeLogDao = new TimeLogDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        String taskId = req.getParameter("taskId");
        try {
            if (path != null && path.length() > 1) {
                String[] parts = path.split("/");
                int id = Integer.parseInt(parts[1]);
                if ("delete".equals(req.getParameter("action"))) {
                    timeLogDao.delete(id);
                }
            }
        } catch (NumberFormatException | SQLException e) {
            LOGGER.severe("TimeLog error: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/tasks/" + (taskId != null ? taskId : ""));
    }
}
