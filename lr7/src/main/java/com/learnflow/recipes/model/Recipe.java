package com.learnflow.recipes.model;

public class Recipe {

    private int id;
    private String name;
    private String ingredients;
    private String difficulty;
    private String steps;
    private int cookTime;
    private String imagePath;

    public Recipe() {}

    public Recipe(int id, String name, String ingredients,
                  String difficulty, String steps, int cookTime, String imagePath) {
        this.id          = id;
        this.name        = name;
        this.ingredients = ingredients;
        this.difficulty  = difficulty;
        this.steps       = steps;
        this.cookTime    = cookTime;
        this.imagePath   = imagePath;
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }

    public String getIngredients()                      { return ingredients; }
    public void setIngredients(String ingredients)      { this.ingredients = ingredients; }

    public String getDifficulty()                       { return difficulty; }
    public void setDifficulty(String difficulty)        { this.difficulty = difficulty; }

    public String getSteps()                { return steps; }
    public void setSteps(String steps)      { this.steps = steps; }

    public int getCookTime()                { return cookTime; }
    public void setCookTime(int cookTime)   { this.cookTime = cookTime; }

    public String getImagePath()                    { return imagePath; }
    public void setImagePath(String imagePath)      { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return "Recipe{id=" + id + ", name='" + name + "', difficulty='" + difficulty + "'}";
    }
}
