package com.learnflow.recipesapi.util;

import com.learnflow.recipesapi.model.Recipe;
import java.time.Instant;
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
        return "{"
            + "\"id\":"            + r.getId()                  + ","
            + "\"name\":\""        + escape(r.getName())        + "\","
            + "\"cookTime\":"      + r.getCookTime()            + ","
            + "\"ingredients\":\"" + escape(r.getIngredients()) + "\""
            + "}";
    }

    public static String toJson(List<Recipe> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    public static String error(String message, int status) {
        return "{"
            + "\"message\":\""   + escape(message)          + "\","
            + "\"timestamp\":\"" + Instant.now().toString()  + "\","
            + "\"status\":"      + status
            + "}";
    }
}
