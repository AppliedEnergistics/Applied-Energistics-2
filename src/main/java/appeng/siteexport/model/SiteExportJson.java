package appeng.siteexport.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class SiteExportJson {
    public String defaultNamespace;

    public Map<ResourceLocation, ExportedPageJson> pages = new HashMap<>();
    public Map<String, JsonElement> pageIndices = new HashMap<>();
    /**
     * Recipes indexed by their recipe ID.
     */
    public Map<String, JsonElement> recipes = new HashMap<>();

    public Map<String, ItemInfoJson> items = new HashMap<>();

    public List<P2PTypeInfo> p2pTunnelTypes = new ArrayList<>();

    public Map<String, Map<DyeColor, String>> coloredVersions = new HashMap<>();
    public List<NavigationNodeJson> navigationRootNodes = new ArrayList<>();

    public Map<String, FluidInfoJson> fluids = new HashMap<>();

    public Map<String, String> defaultConfigValues = new HashMap<>();

}
