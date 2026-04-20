package com.learnflow.recipesapi.model;

public class Recipe {

    private int id;
    private String name;
    private int cookTime;
    private String ingredients;

    public Recipe() {}

    public Recipe(int id, String name, int cookTime, String ingredients) {
        this.id          = id;
        this.name        = name;
        this.cookTime    = cookTime;
        this.ingredients = ingredients;
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public int getCookTime()                    { return cookTime; }
    public void setCookTime(int cookTime)       { this.cookTime = cookTime; }

    public String getIngredients()              { return ingredients; }
    public void setIngredients(String v)        { this.ingredients = v; }
}
