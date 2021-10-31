package appeng.siteexport.model;

public class CraftingRecipeJson {
    public String id;
    public boolean shapeless;
    // Exactly 9 entries
    public String[][] ingredients;
    // The width&height are only relevant for shaped recipes
    public int width;
    public int height;
    public String resultItem;
    public int resultCount;
}
