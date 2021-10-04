package appeng.siteexport.model;

import java.util.HashMap;
import java.util.Map;

public class SiteExportJson {
    public String generated;

    public String gameVersion;

    public String modVersion;

    public Map<String, CraftingRecipeJson> craftingRecipes = new HashMap<>();

    public Map<String, ItemInfoJson> items = new HashMap<>();
}
