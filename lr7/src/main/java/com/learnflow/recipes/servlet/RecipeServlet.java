package com.learnflow.recipes.servlet;

import com.learnflow.recipes.dao.RecipeDao;
import com.learnflow.recipes.model.Recipe;
import com.learnflow.recipes.util.JsonUtil;

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

/**
 * Сервлет для управления рецептами.
 *
 * <p>Поддерживаемые маршруты:
 * <ul>
 *   <li>GET  /recipes          — список всех рецептов</li>
 *   <li>GET  /recipes?search=  — поиск по названию</li>
 *   <li>GET  /recipes?id=      — получить рецепт по id</li>
 *   <li>POST /recipes          — добавить рецепт</li>
 *   <li>PUT  /recipes          — обновить рецепт (id в теле)</li>
 *   <li>DELETE /recipes?id=    — удалить рецепт</li>
 * </ul>
 */
@WebServlet("/recipes")
public class RecipeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RecipeServlet.class.getName());
    private final RecipeDao dao = new RecipeDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String idParam     = req.getParameter("id");
        String searchParam = req.getParameter("search");

        try {
            if (idParam != null) {
                int id = Integer.parseInt(idParam);
                Recipe recipe = dao.findById(id);
                if (recipe == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(JsonUtil.error("Рецепт с id=" + id + " не найден"));
                } else {
                    out.print(JsonUtil.toJson(recipe));
                }

            } else if (searchParam != null) {
                List<Recipe> results = dao.searchByName(searchParam.trim());
                out.print(JsonUtil.toJson(results));

            } else {
                List<Recipe> all = dao.findAll();
                out.print(JsonUtil.toJson(all));
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.error("Некорректный id: " + idParam));
        } catch (SQLException e) {
            LOGGER.severe("GET /recipes — ошибка БД: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.error("Ошибка базы данных"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            Recipe recipe = parseRecipeFromRequest(req, false);
            Recipe created = dao.insert(recipe);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(JsonUtil.toJson(created));
            LOGGER.info("POST /recipes — создан рецепт id=" + created.getId());

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.error(e.getMessage()));
        } catch (SQLException e) {
            LOGGER.severe("POST /recipes — ошибка БД: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.error("Ошибка базы данных"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            Recipe recipe = parseRecipeFromRequest(req, true);
            boolean updated = dao.update(recipe);
            if (!updated) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.error("Рецепт с id=" + recipe.getId() + " не найден"));
            } else {
                out.print(JsonUtil.toJson(recipe));
                LOGGER.info("PUT /recipes — обновлён рецепт id=" + recipe.getId());
            }

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.error(e.getMessage()));
        } catch (SQLException e) {
            LOGGER.severe("PUT /recipes — ошибка БД: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.error("Ошибка базы данных"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.error("Параметр id обязателен"));
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            boolean deleted = dao.delete(id);
            if (!deleted) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.error("Рецепт с id=" + id + " не найден"));
            } else {
                out.print(JsonUtil.success("Рецепт удалён"));
                LOGGER.info("DELETE /recipes — удалён рецепт id=" + id);
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.error("Некорректный id: " + idParam));
        } catch (SQLException e) {
            LOGGER.severe("DELETE /recipes — ошибка БД: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.error("Ошибка базы данных"));
        }
    }

    /**
     * Извлекает и валидирует параметры рецепта из запроса.
     *
     * @param needId true если требуется параметр id (для PUT)
     */
    private Recipe parseRecipeFromRequest(HttpServletRequest req, boolean needId)
            throws IllegalArgumentException {

        String name        = req.getParameter("name");
        String ingredients = req.getParameter("ingredients");
        String difficulty  = req.getParameter("difficulty");
        String steps       = req.getParameter("steps");
        String cookTimeStr = req.getParameter("cookTime");

        if (isBlank(name))        throw new IllegalArgumentException("Поле 'name' обязательно");
        if (isBlank(ingredients)) throw new IllegalArgumentException("Поле 'ingredients' обязательно");
        if (isBlank(difficulty))  throw new IllegalArgumentException("Поле 'difficulty' обязательно");
        if (isBlank(steps))       throw new IllegalArgumentException("Поле 'steps' обязательно");
        if (isBlank(cookTimeStr)) throw new IllegalArgumentException("Поле 'cookTime' обязательно");

        if (!difficulty.equals("простой") && !difficulty.equals("средний") && !difficulty.equals("комплексный")) {
            throw new IllegalArgumentException("difficulty должен быть: простой, средний или комплексный");
        }

        int cookTime;
        try {
            cookTime = Integer.parseInt(cookTimeStr);
            if (cookTime <= 0) throw new IllegalArgumentException("cookTime должен быть положительным числом");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("cookTime должен быть целым числом");
        }

        Recipe recipe = new Recipe();
        recipe.setName(name.trim());
        recipe.setIngredients(ingredients.trim());
        recipe.setDifficulty(difficulty.trim());
        recipe.setSteps(steps.trim());
        recipe.setCookTime(cookTime);

        if (needId) {
            String idParam = req.getParameter("id");
            if (isBlank(idParam)) throw new IllegalArgumentException("Поле 'id' обязательно для обновления");
            try {
                recipe.setId(Integer.parseInt(idParam));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("id должен быть целым числом");
            }
        }

        return recipe;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
