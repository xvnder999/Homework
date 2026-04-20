package com.learnflow.recipesapi.dao;

import com.learnflow.recipesapi.model.Recipe;
import com.learnflow.recipesapi.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RecipeDao {

    private static final Logger LOGGER = Logger.getLogger(RecipeDao.class.getName());

    private static final String SQL_SELECT_ALL =
        "SELECT id, name, cook_time, ingredients FROM recipes ORDER BY id";

    private static final String SQL_SELECT_BY_ID =
        "SELECT id, name, cook_time, ingredients FROM recipes WHERE id = ?";

    private static final String SQL_SEARCH_BY_INGREDIENT =
        "SELECT id, name, cook_time, ingredients FROM recipes " +
        "WHERE LOWER(ingredients) LIKE LOWER(?) ORDER BY name";

    private static final String SQL_INSERT =
        "INSERT INTO recipes (name, cook_time, ingredients) VALUES (?, ?, ?) RETURNING id";

    private static final String SQL_DELETE =
        "DELETE FROM recipes WHERE id = ?";

    public List<Recipe> findAll() throws SQLException {
        List<Recipe> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        LOGGER.info("findAll: " + list.size() + " рецептов");
        return list;
    }

    public Recipe findById(int id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        LOGGER.warning("findById(" + id + "): не найден");
        return null;
    }

    public List<Recipe> searchByIngredient(String ingredient) throws SQLException {
        List<Recipe> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH_BY_INGREDIENT)) {
            ps.setString(1, "%" + ingredient + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        LOGGER.info("searchByIngredient('" + ingredient + "'): " + list.size() + " рецептов");
        return list;
    }

    public Recipe insert(Recipe recipe) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, recipe.getName());
            ps.setInt(2, recipe.getCookTime());
            ps.setString(3, recipe.getIngredients());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    recipe.setId(rs.getInt(1));
                    LOGGER.info("insert: id=" + recipe.getId());
                }
            }
        }
        return recipe;
    }

    public boolean delete(int id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            LOGGER.info("delete id=" + id + ": строк=" + rows);
            return rows > 0;
        }
    }

    private Recipe mapRow(ResultSet rs) throws SQLException {
        return new Recipe(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("cook_time"),
            rs.getString("ingredients")
        );
    }
}
