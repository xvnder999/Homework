package com.learnflow.recipesapi.servlet;

import com.learnflow.recipesapi.dao.RecipeDao;
import com.learnflow.recipesapi.model.Recipe;
import com.learnflow.recipesapi.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/api/recipes/*")
public class RecipeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RecipeServlet.class.getName());
    private final RecipeDao dao = new RecipeDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setJson(resp);
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo(); // null | "/" | "/123" | "/search"

        try {
            // GET /api/recipes/search?ingredient=рис
            if (pathInfo != null && pathInfo.equals("/search")) {
                String ingredient = req.getParameter("ingredient");
                if (ingredient == null || ingredient.trim().isEmpty()) {
                    resp.setStatus(400);
                    out.print(JsonUtil.error("Параметр 'ingredient' обязателен", 400));
                    return;
                }
                List<Recipe> results = dao.searchByIngredient(ingredient.trim());
                LOGGER.info("GET /api/recipes/search?ingredient=" + ingredient);
                out.print(JsonUtil.toJson(results));
                return;
            }

            // GET /api/recipes/{id}
            if (pathInfo != null && pathInfo.length() > 1) {
                int id = parseId(pathInfo.substring(1));
                if (id < 0) {
                    resp.setStatus(400);
                    out.print(JsonUtil.error("Некорректный id", 400));
                    return;
                }
                Recipe recipe = dao.findById(id);
                if (recipe == null) {
                    resp.setStatus(404);
                    out.print(JsonUtil.error("Рецепт с id=" + id + " не найден", 404));
                    return;
                }
                LOGGER.info("GET /api/recipes/" + id);
                out.print(JsonUtil.toJson(recipe));
                return;
            }

            // GET /api/recipes
            List<Recipe> all = dao.findAll();
            LOGGER.info("GET /api/recipes — возвращено " + all.size());
            out.print(JsonUtil.toJson(all));

        } catch (SQLException e) {
            LOGGER.severe("GET — ошибка БД: " + e.getMessage());
            resp.setStatus(500);
            out.print(JsonUtil.error("Ошибка базы данных", 500));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setJson(resp);
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            Recipe recipe = parseBody(req);
            Recipe created = dao.insert(recipe);
            resp.setStatus(201);
            LOGGER.info("POST /api/recipes — создан id=" + created.getId());
            out.print(JsonUtil.toJson(created));

        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            out.print(JsonUtil.error(e.getMessage(), 400));
        } catch (SQLException e) {
            LOGGER.severe("POST — ошибка БД: " + e.getMessage());
            resp.setStatus(500);
            out.print(JsonUtil.error("Ошибка базы данных", 500));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setJson(resp);
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(400);
            out.print(JsonUtil.error("Укажите id в URL: /api/recipes/{id}", 400));
            return;
        }

        try {
            int id = parseId(pathInfo.substring(1));
            if (id < 0) {
                resp.setStatus(400);
                out.print(JsonUtil.error("Некорректный id", 400));
                return;
            }
            boolean deleted = dao.delete(id);
            if (!deleted) {
                resp.setStatus(404);
                out.print(JsonUtil.error("Рецепт с id=" + id + " не найден", 404));
                return;
            }
            LOGGER.info("DELETE /api/recipes/" + id);
            resp.setStatus(200);
            out.print("{\"message\":\"Рецепт удалён\",\"status\":200}");

        } catch (SQLException e) {
            LOGGER.severe("DELETE — ошибка БД: " + e.getMessage());
            resp.setStatus(500);
            out.print(JsonUtil.error("Ошибка базы данных", 500));
        }
    }

    private Recipe parseBody(HttpServletRequest req) throws IllegalArgumentException {
        String name        = req.getParameter("name");
        String cookTimeStr = req.getParameter("cookTime");
        String ingredients = req.getParameter("ingredients");

        if (isBlank(name))        throw new IllegalArgumentException("Поле 'name' обязательно");
        if (isBlank(cookTimeStr)) throw new IllegalArgumentException("Поле 'cookTime' обязательно");
        if (isBlank(ingredients)) throw new IllegalArgumentException("Поле 'ingredients' обязательно");

        int cookTime;
        try {
            cookTime = Integer.parseInt(cookTimeStr.trim());
            if (cookTime <= 0) throw new IllegalArgumentException("cookTime должен быть > 0");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("cookTime должен быть целым числом");
        }

        Recipe r = new Recipe();
        r.setName(name.trim());
        r.setCookTime(cookTime);
        r.setIngredients(ingredients.trim());
        return r;
    }

    private void setJson(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
    }

    private int parseId(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
