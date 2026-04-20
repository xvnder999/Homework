package com.learnflow.recipes.dao;

import com.learnflow.recipes.model.Recipe;
import com.learnflow.recipes.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RecipeDao {

    private static final Logger LOGGER = Logger.getLogger(RecipeDao.class.getName());

    private static final String SQL_SELECT_ALL =
        "SELECT id, name, ingredients, difficulty, steps, cook_time FROM recipes ORDER BY id";

    private static final String SQL_SELECT_BY_ID =
        "SELECT id, name, ingredients, difficulty, steps, cook_time FROM recipes WHERE id = ?";

    private static final String SQL_SEARCH_BY_NAME =
        "SELECT id, name, ingredients, difficulty, steps, cook_time " +
        "FROM recipes WHERE LOWER(name) LIKE LOWER(?) ORDER BY name";

    private static final String SQL_INSERT =
        "INSERT INTO recipes (name, ingredients, difficulty, steps, cook_time) " +
        "VALUES (?, ?, ?, ?, ?) RETURNING id";

    private static final String SQL_UPDATE =
        "UPDATE recipes SET name=?, ingredients=?, difficulty=?, steps=?, cook_time=? WHERE id=?";

    private static final String SQL_DELETE =
        "DELETE FROM recipes WHERE id=?";

   
    public List<Recipe> findAll() throws SQLException {
        List<Recipe> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        LOGGER.info("findAll: возвращено " + list.size() + " рецептов");
        return list;
    }

    /**
     * Находит рецепт по идентификатору.
     *
     * @param id идентификатор рецепта
     * @return рецепт или null, если не найден
     */
    public Recipe findById(int id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Recipe r = mapRow(rs);
                    LOGGER.info("findById(" + id + "): найден рецепт '" + r.getName() + "'");
                    return r;
                }
            }
        }
        LOGGER.warning("findById(" + id + "): рецепт не найден");
        return null;
    }

    /**
     * Ищет рецепты по части названия (без учёта регистра).
     *
     * @param query поисковый запрос
     */
    public List<Recipe> searchByName(String query) throws SQLException {
        List<Recipe> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH_BY_NAME)) {
            ps.setString(1, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        LOGGER.info("searchByName('" + query + "'): найдено " + list.size() + " рецептов");
        return list;
    }

    /**
     * Добавляет новый рецепт и возвращает его с присвоенным id.
     *
     * @param recipe рецепт без id
     * @return рецепт с присвоенным id
     */
    public Recipe insert(Recipe recipe) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, recipe.getName());
            ps.setString(2, recipe.getIngredients());
            ps.setString(3, recipe.getDifficulty());
            ps.setString(4, recipe.getSteps());
            ps.setInt(5, recipe.getCookTime());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    recipe.setId(rs.getInt(1));
                    LOGGER.info("insert: добавлен рецепт id=" + recipe.getId());
                }
            }
        }
        return recipe;
    }

    /**
     * Обновляет существующий рецепт.
     *
     * @param recipe рецепт с корректным id
     * @return true если запись обновлена, false если id не найден
     */
    public boolean update(Recipe recipe) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, recipe.getName());
            ps.setString(2, recipe.getIngredients());
            ps.setString(3, recipe.getDifficulty());
            ps.setString(4, recipe.getSteps());
            ps.setInt(5, recipe.getCookTime());
            ps.setInt(6, recipe.getId());
            int rows = ps.executeUpdate();
            LOGGER.info("update id=" + recipe.getId() + ": обновлено строк=" + rows);
            return rows > 0;
        }
    }

    /**
     * Удаляет рецепт по идентификатору.
     *
     * @param id идентификатор рецепта
     * @return true если запись удалена
     */
    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            LOGGER.info("delete id=" + id + ": удалено строк=" + rows);
            return rows > 0;
        }
    }

    private Recipe mapRow(ResultSet rs) throws SQLException {
        return new Recipe(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("ingredients"),
            rs.getString("difficulty"),
            rs.getString("steps"),
            rs.getInt("cook_time")
        );
    }
}
