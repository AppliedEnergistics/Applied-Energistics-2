package appeng.siteexport.model;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteExportJson {
    public long generated;

    public String gameVersion;

    public String modVersion;
    public String defaultNamespace;

    public Map<ResourceLocation, ExportedPageJson> pages = new HashMap<>();
    public Map<String, JsonElement> pageIndices = new HashMap<>();
    public Map<String, CraftingRecipeJson> craftingRecipes = new HashMap<>();

    public Map<String, SmeltingRecipeJson> smeltingRecipes = new HashMap<>();

    public Map<String, InscriberRecipeJson> inscriberRecipes = new HashMap<>();

    public Map<String, ItemInfoJson> items = new HashMap<>();

    public List<P2PTypeInfo> p2pTunnelTypes = new ArrayList<>();

    public Map<String, Map<DyeColor, String>> coloredVersions = new HashMap<>();
    public List<NavigationNodeJson> navigationRootNodes = new ArrayList<>();

}
