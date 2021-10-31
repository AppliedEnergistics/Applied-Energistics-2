package appeng.siteexport.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.DyeColor;

public class SiteExportJson {
    public String generated;

    public String gameVersion;

    public String modVersion;

    public Map<String, CraftingRecipeJson> craftingRecipes = new HashMap<>();

    public Map<String, SmeltingRecipeJson> smeltingRecipes = new HashMap<>();

    public Map<String, InscriberRecipeJson> inscriberRecipes = new HashMap<>();

    public Map<String, ItemInfoJson> items = new HashMap<>();

    public List<P2PTypeInfo> p2pTunnelTypes = new ArrayList<>();

    public Map<String, Map<DyeColor, String>> coloredVersions = new HashMap<>();
}
