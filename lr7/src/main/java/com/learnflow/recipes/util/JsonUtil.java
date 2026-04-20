package com.learnflow.recipes.util;

import com.learnflow.recipes.model.Recipe;
import java.util.List;

public final class JsonUtil {

    private JsonUtil() {}

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String toJson(Recipe r) {
        String img = r.getImagePath() != null ? r.getImagePath() : "";
        return "{"
            + "\"id\":"            + r.getId()                    + ","
            + "\"name\":\""        + escape(r.getName())          + "\","
            + "\"ingredients\":\"" + escape(r.getIngredients())   + "\","
            + "\"difficulty\":\""  + escape(r.getDifficulty())    + "\","
            + "\"steps\":\""       + escape(r.getSteps())         + "\","
            + "\"cookTime\":"      + r.getCookTime()              + ","
            + "\"imagePath\":\""   + escape(img)                  + "\""
            + "}";
    }

    public static String toJson(List<Recipe> recipes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < recipes.size(); i++) {
            sb.append(toJson(recipes.get(i)));
            if (i < recipes.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String error(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    public static String success(String message) {
        return "{\"success\":\"" + escape(message) + "\"}";
    }
}
