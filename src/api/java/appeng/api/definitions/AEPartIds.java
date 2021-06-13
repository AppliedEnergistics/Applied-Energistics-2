package appeng.api.definitions;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.ResourceLocation;

import appeng.api.util.AEColor;

public final class AEPartIds {
    public static final ResourceLocation TERMINAL = new ResourceLocation("appliedenergistics2", "terminal");

    public static final ResourceLocation MONITOR = new ResourceLocation("appliedenergistics2", "monitor");

    public static final ResourceLocation QUARTZ_FIBER = new ResourceLocation("appliedenergistics2", "quartz_fiber");

    public static final ResourceLocation P2_P_TUNNEL_LIGHT = new ResourceLocation("appliedenergistics2",
            "light_p2p_tunnel");

    public static final ResourceLocation FLUID_STORAGE_BUS = new ResourceLocation("appliedenergistics2",
            "fluid_storage_bus");

    public static final ResourceLocation LEVEL_EMITTER = new ResourceLocation("appliedenergistics2", "level_emitter");

    public static final ResourceLocation FLUID_IFACE = new ResourceLocation("appliedenergistics2",
            "cable_fluid_interface");

    public static final ResourceLocation TOGGLE_BUS = new ResourceLocation("appliedenergistics2", "toggle_bus");

    public static final ResourceLocation DARK_MONITOR = new ResourceLocation("appliedenergistics2", "dark_monitor");

    public static final ResourceLocation CRAFTING_TERMINAL = new ResourceLocation("appliedenergistics2",
            "crafting_terminal");

    public static final ResourceLocation P2_P_TUNNEL_FLUIDS = new ResourceLocation("appliedenergistics2",
            "fluid_p2p_tunnel");

    public static final ResourceLocation P2_P_TUNNEL_M_E = new ResourceLocation("appliedenergistics2", "me_p2p_tunnel");

    public static final ResourceLocation P2_P_TUNNEL_F_E = new ResourceLocation("appliedenergistics2", "fe_p2p_tunnel");

    public static final ResourceLocation SEMI_DARK_MONITOR = new ResourceLocation("appliedenergistics2",
            "semi_dark_monitor");

    public static final ResourceLocation FORMATION_PLANE = new ResourceLocation("appliedenergistics2",
            "formation_plane");

    public static final ResourceLocation STORAGE_MONITOR = new ResourceLocation("appliedenergistics2",
            "storage_monitor");

    public static final ResourceLocation FLUID_TERMINAL = new ResourceLocation("appliedenergistics2", "fluid_terminal");

    public static final ResourceLocation FLUID_EXPORT_BUS = new ResourceLocation("appliedenergistics2",
            "fluid_export_bus");

    public static final ResourceLocation CABLE_ANCHOR = new ResourceLocation("appliedenergistics2", "cable_anchor");

    public static final ResourceLocation PATTERN_TERMINAL = new ResourceLocation("appliedenergistics2",
            "pattern_terminal");

    public static final ResourceLocation FLUID_IMPORT_BUS = new ResourceLocation("appliedenergistics2",
            "fluid_import_bus");

    public static final ResourceLocation P2_P_TUNNEL_ITEMS = new ResourceLocation("appliedenergistics2",
            "item_p2p_tunnel");

    public static final ResourceLocation IFACE = new ResourceLocation("appliedenergistics2", "cable_interface");

    public static final ResourceLocation FLUID_ANNIHILATION_PLANE = new ResourceLocation("appliedenergistics2",
            "fluid_annihilation_plane");

    public static final ResourceLocation FLUID_FORMATIONN_PLANE = new ResourceLocation("appliedenergistics2",
            "fluid_formation_plane");

    public static final ResourceLocation INTERFACE_TERMINAL = new ResourceLocation("appliedenergistics2",
            "interface_terminal");

    public static final ResourceLocation CONVERSION_MONITOR = new ResourceLocation("appliedenergistics2",
            "conversion_monitor");

    public static final ResourceLocation FLUID_LEVEL_EMITTER = new ResourceLocation("appliedenergistics2",
            "fluid_level_emitter");

    public static final ResourceLocation CABLE_DENSE_SMART_WHITE = new ResourceLocation("appliedenergistics2",
            "white_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_ORANGE = new ResourceLocation("appliedenergistics2",
            "orange_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_MAGENTA = new ResourceLocation("appliedenergistics2",
            "magenta_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_LIGHT_BLUE = new ResourceLocation("appliedenergistics2",
            "light_blue_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_YELLOW = new ResourceLocation("appliedenergistics2",
            "yellow_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_LIME = new ResourceLocation("appliedenergistics2",
            "lime_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_PINK = new ResourceLocation("appliedenergistics2",
            "pink_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_GRAY = new ResourceLocation("appliedenergistics2",
            "gray_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_LIGHT_GRAY = new ResourceLocation("appliedenergistics2",
            "light_gray_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_CYAN = new ResourceLocation("appliedenergistics2",
            "cyan_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_PURPLE = new ResourceLocation("appliedenergistics2",
            "purple_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_BLUE = new ResourceLocation("appliedenergistics2",
            "blue_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_BROWN = new ResourceLocation("appliedenergistics2",
            "brown_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_GREEN = new ResourceLocation("appliedenergistics2",
            "green_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_RED = new ResourceLocation("appliedenergistics2",
            "red_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_BLACK = new ResourceLocation("appliedenergistics2",
            "black_smart_dense_cable");

    public static final ResourceLocation CABLE_DENSE_SMART_TRANSPARENT = new ResourceLocation("appliedenergistics2",
            "fluix_smart_dense_cable");

    public static final Map<AEColor, ResourceLocation> CABLE_DENSE_SMART = ImmutableMap
            .<AEColor, ResourceLocation>builder().put(AEColor.WHITE, CABLE_DENSE_SMART_WHITE)
            .put(AEColor.ORANGE, CABLE_DENSE_SMART_ORANGE)
            .put(AEColor.MAGENTA, CABLE_DENSE_SMART_MAGENTA)
            .put(AEColor.LIGHT_BLUE, CABLE_DENSE_SMART_LIGHT_BLUE)
            .put(AEColor.YELLOW, CABLE_DENSE_SMART_YELLOW)
            .put(AEColor.LIME, CABLE_DENSE_SMART_LIME)
            .put(AEColor.PINK, CABLE_DENSE_SMART_PINK)
            .put(AEColor.GRAY, CABLE_DENSE_SMART_GRAY)
            .put(AEColor.LIGHT_GRAY, CABLE_DENSE_SMART_LIGHT_GRAY)
            .put(AEColor.CYAN, CABLE_DENSE_SMART_CYAN)
            .put(AEColor.PURPLE, CABLE_DENSE_SMART_PURPLE)
            .put(AEColor.BLUE, CABLE_DENSE_SMART_BLUE)
            .put(AEColor.BROWN, CABLE_DENSE_SMART_BROWN)
            .put(AEColor.GREEN, CABLE_DENSE_SMART_GREEN)
            .put(AEColor.RED, CABLE_DENSE_SMART_RED)
            .put(AEColor.BLACK, CABLE_DENSE_SMART_BLACK)
            .put(AEColor.TRANSPARENT, CABLE_DENSE_SMART_TRANSPARENT)
            .build();

    public static final ResourceLocation CABLE_SMART_WHITE = new ResourceLocation("appliedenergistics2",
            "white_smart_cable");

    public static final ResourceLocation CABLE_SMART_ORANGE = new ResourceLocation("appliedenergistics2",
            "orange_smart_cable");

    public static final ResourceLocation CABLE_SMART_MAGENTA = new ResourceLocation("appliedenergistics2",
            "magenta_smart_cable");

    public static final ResourceLocation CABLE_SMART_LIGHT_BLUE = new ResourceLocation("appliedenergistics2",
            "light_blue_smart_cable");

    public static final ResourceLocation CABLE_SMART_YELLOW = new ResourceLocation("appliedenergistics2",
            "yellow_smart_cable");

    public static final ResourceLocation CABLE_SMART_LIME = new ResourceLocation("appliedenergistics2",
            "lime_smart_cable");

    public static final ResourceLocation CABLE_SMART_PINK = new ResourceLocation("appliedenergistics2",
            "pink_smart_cable");

    public static final ResourceLocation CABLE_SMART_GRAY = new ResourceLocation("appliedenergistics2",
            "gray_smart_cable");

    public static final ResourceLocation CABLE_SMART_LIGHT_GRAY = new ResourceLocation("appliedenergistics2",
            "light_gray_smart_cable");

    public static final ResourceLocation CABLE_SMART_CYAN = new ResourceLocation("appliedenergistics2",
            "cyan_smart_cable");

    public static final ResourceLocation CABLE_SMART_PURPLE = new ResourceLocation("appliedenergistics2",
            "purple_smart_cable");

    public static final ResourceLocation CABLE_SMART_BLUE = new ResourceLocation("appliedenergistics2",
            "blue_smart_cable");

    public static final ResourceLocation CABLE_SMART_BROWN = new ResourceLocation("appliedenergistics2",
            "brown_smart_cable");

    public static final ResourceLocation CABLE_SMART_GREEN = new ResourceLocation("appliedenergistics2",
            "green_smart_cable");

    public static final ResourceLocation CABLE_SMART_RED = new ResourceLocation("appliedenergistics2",
            "red_smart_cable");

    public static final ResourceLocation CABLE_SMART_BLACK = new ResourceLocation("appliedenergistics2",
            "black_smart_cable");

    public static final ResourceLocation CABLE_SMART_TRANSPARENT = new ResourceLocation("appliedenergistics2",
            "fluix_smart_cable");

    public static final Map<AEColor, ResourceLocation> CABLE_SMART = ImmutableMap.<AEColor, ResourceLocation>builder()
            .put(AEColor.WHITE, CABLE_SMART_WHITE)
            .put(AEColor.ORANGE, CABLE_SMART_ORANGE)
            .put(AEColor.MAGENTA, CABLE_SMART_MAGENTA)
            .put(AEColor.LIGHT_BLUE, CABLE_SMART_LIGHT_BLUE)
            .put(AEColor.YELLOW, CABLE_SMART_YELLOW)
            .put(AEColor.LIME, CABLE_SMART_LIME)
            .put(AEColor.PINK, CABLE_SMART_PINK)
            .put(AEColor.GRAY, CABLE_SMART_GRAY)
            .put(AEColor.LIGHT_GRAY, CABLE_SMART_LIGHT_GRAY)
            .put(AEColor.CYAN, CABLE_SMART_CYAN)
            .put(AEColor.PURPLE, CABLE_SMART_PURPLE)
            .put(AEColor.BLUE, CABLE_SMART_BLUE)
            .put(AEColor.BROWN, CABLE_SMART_BROWN)
            .put(AEColor.GREEN, CABLE_SMART_GREEN)
            .put(AEColor.RED, CABLE_SMART_RED)
            .put(AEColor.BLACK, CABLE_SMART_BLACK)
            .put(AEColor.TRANSPARENT, CABLE_SMART_TRANSPARENT)
            .build();

    public static final ResourceLocation IMPORT_BUS = new ResourceLocation("appliedenergistics2", "import_bus");

    public static final ResourceLocation ENERGY_ACCEPTOR = new ResourceLocation("appliedenergistics2",
            "cable_energy_acceptor");

    public static final ResourceLocation EXPORT_BUS = new ResourceLocation("appliedenergistics2", "export_bus");

    public static final ResourceLocation STORAGE_BUS = new ResourceLocation("appliedenergistics2", "storage_bus");

    public static final ResourceLocation CABLE_GLASS_WHITE = new ResourceLocation("appliedenergistics2",
            "white_glass_cable");

    public static final ResourceLocation CABLE_GLASS_ORANGE = new ResourceLocation("appliedenergistics2",
            "orange_glass_cable");

    public static final ResourceLocation CABLE_GLASS_MAGENTA = new ResourceLocation("appliedenergistics2",
            "magenta_glass_cable");

    public static final ResourceLocation CABLE_GLASS_LIGHT_BLUE = new ResourceLocation("appliedenergistics2",
            "light_blue_glass_cable");

    public static final ResourceLocation CABLE_GLASS_YELLOW = new ResourceLocation("appliedenergistics2",
            "yellow_glass_cable");

    public static final ResourceLocation CABLE_GLASS_LIME = new ResourceLocation("appliedenergistics2",
            "lime_glass_cable");

    public static final ResourceLocation CABLE_GLASS_PINK = new ResourceLocation("appliedenergistics2",
            "pink_glass_cable");

    public static final ResourceLocation CABLE_GLASS_GRAY = new ResourceLocation("appliedenergistics2",
            "gray_glass_cable");

    public static final ResourceLocation CABLE_GLASS_LIGHT_GRAY = new ResourceLocation("appliedenergistics2",
            "light_gray_glass_cable");

    public static final ResourceLocation CABLE_GLASS_CYAN = new ResourceLocation("appliedenergistics2",
            "cyan_glass_cable");

    public static final ResourceLocation CABLE_GLASS_PURPLE = new ResourceLocation("appliedenergistics2",
            "purple_glass_cable");

    public static final ResourceLocation CABLE_GLASS_BLUE = new ResourceLocation("appliedenergistics2",
            "blue_glass_cable");

    public static final ResourceLocation CABLE_GLASS_BROWN = new ResourceLocation("appliedenergistics2",
            "brown_glass_cable");

    public static final ResourceLocation CABLE_GLASS_GREEN = new ResourceLocation("appliedenergistics2",
            "green_glass_cable");

    public static final ResourceLocation CABLE_GLASS_RED = new ResourceLocation("appliedenergistics2",
            "red_glass_cable");

    public static final ResourceLocation CABLE_GLASS_BLACK = new ResourceLocation("appliedenergistics2",
            "black_glass_cable");

    public static final ResourceLocation CABLE_GLASS_TRANSPARENT = new ResourceLocation("appliedenergistics2",
            "fluix_glass_cable");

    public static final Map<AEColor, ResourceLocation> CABLE_GLASS = ImmutableMap.<AEColor, ResourceLocation>builder()
            .put(AEColor.WHITE, CABLE_GLASS_WHITE)
            .put(AEColor.ORANGE, CABLE_GLASS_ORANGE)
            .put(AEColor.MAGENTA, CABLE_GLASS_MAGENTA)
            .put(AEColor.LIGHT_BLUE, CABLE_GLASS_LIGHT_BLUE)
            .put(AEColor.YELLOW, CABLE_GLASS_YELLOW)
            .put(AEColor.LIME, CABLE_GLASS_LIME)
            .put(AEColor.PINK, CABLE_GLASS_PINK)
            .put(AEColor.GRAY, CABLE_GLASS_GRAY)
            .put(AEColor.LIGHT_GRAY, CABLE_GLASS_LIGHT_GRAY)
            .put(AEColor.CYAN, CABLE_GLASS_CYAN)
            .put(AEColor.PURPLE, CABLE_GLASS_PURPLE)
            .put(AEColor.BLUE, CABLE_GLASS_BLUE)
            .put(AEColor.BROWN, CABLE_GLASS_BROWN)
            .put(AEColor.GREEN, CABLE_GLASS_GREEN)
            .put(AEColor.RED, CABLE_GLASS_RED)
            .put(AEColor.BLACK, CABLE_GLASS_BLACK)
            .put(AEColor.TRANSPARENT, CABLE_GLASS_TRANSPARENT)
            .build();

    public static final ResourceLocation CABLE_COVERED_WHITE = new ResourceLocation("appliedenergistics2",
            "white_covered_cable");

    public static final ResourceLocation CABLE_COVERED_ORANGE = new ResourceLocation("appliedenergistics2",
            "orange_covered_cable");

    public static final ResourceLocation CABLE_COVERED_MAGENTA = new ResourceLocation("appliedenergistics2",
            "magenta_covered_cable");

    public static final ResourceLocation CABLE_COVERED_LIGHT_BLUE = new ResourceLocation("appliedenergistics2",
            "light_blue_covered_cable");

    public static final ResourceLocation CABLE_COVERED_YELLOW = new ResourceLocation("appliedenergistics2",
            "yellow_covered_cable");

    public static final ResourceLocation CABLE_COVERED_LIME = new ResourceLocation("appliedenergistics2",
            "lime_covered_cable");

    public static final ResourceLocation CABLE_COVERED_PINK = new ResourceLocation("appliedenergistics2",
            "pink_covered_cable");

    public static final ResourceLocation CABLE_COVERED_GRAY = new ResourceLocation("appliedenergistics2",
            "gray_covered_cable");

    public static final ResourceLocation CABLE_COVERED_LIGHT_GRAY = new ResourceLocation("appliedenergistics2",
            "light_gray_covered_cable");

    public static final ResourceLocation CABLE_COVERED_CYAN = new ResourceLocation("appliedenergistics2",
            "cyan_covered_cable");

    public static final ResourceLocation CABLE_COVERED_PURPLE = new ResourceLocation("appliedenergistics2",
            "purple_covered_cable");

    public static final ResourceLocation CABLE_COVERED_BLUE = new ResourceLocation("appliedenergistics2",
            "blue_covered_cable");

    public static final ResourceLocation CABLE_COVERED_BROWN = new ResourceLocation("appliedenergistics2",
            "brown_covered_cable");

    public static final ResourceLocation CABLE_COVERED_GREEN = new ResourceLocation("appliedenergistics2",
            "green_covered_cable");

    public static final ResourceLocation CABLE_COVERED_RED = new ResourceLocation("appliedenergistics2",
            "red_covered_cable");

    public static final ResourceLocation CABLE_COVERED_BLACK = new ResourceLocation("appliedenergistics2",
            "black_covered_cable");

    public static final ResourceLocation CABLE_COVERED_TRANSPARENT = new ResourceLocation("appliedenergistics2",
            "fluix_covered_cable");

    public static final Map<AEColor, ResourceLocation> CABLE_COVERED = ImmutableMap.<AEColor, ResourceLocation>builder()
            .put(AEColor.WHITE, CABLE_COVERED_WHITE)
            .put(AEColor.ORANGE, CABLE_COVERED_ORANGE)
            .put(AEColor.MAGENTA, CABLE_COVERED_MAGENTA)
            .put(AEColor.LIGHT_BLUE, CABLE_COVERED_LIGHT_BLUE)
            .put(AEColor.YELLOW, CABLE_COVERED_YELLOW)
            .put(AEColor.LIME, CABLE_COVERED_LIME)
            .put(AEColor.PINK, CABLE_COVERED_PINK)
            .put(AEColor.GRAY, CABLE_COVERED_GRAY)
            .put(AEColor.LIGHT_GRAY, CABLE_COVERED_LIGHT_GRAY)
            .put(AEColor.CYAN, CABLE_COVERED_CYAN)
            .put(AEColor.PURPLE, CABLE_COVERED_PURPLE)
            .put(AEColor.BLUE, CABLE_COVERED_BLUE)
            .put(AEColor.BROWN, CABLE_COVERED_BROWN)
            .put(AEColor.GREEN, CABLE_COVERED_GREEN)
            .put(AEColor.RED, CABLE_COVERED_RED)
            .put(AEColor.BLACK, CABLE_COVERED_BLACK)
            .put(AEColor.TRANSPARENT, CABLE_COVERED_TRANSPARENT)
            .build();

    public static final ResourceLocation INVERTED_TOGGLE_BUS = new ResourceLocation("appliedenergistics2",
            "inverted_toggle_bus");

    public static final ResourceLocation ANNIHILATION_PLANE = new ResourceLocation("appliedenergistics2",
            "annihilation_plane");

    public static final ResourceLocation CABLE_DENSE_COVERED_WHITE = new ResourceLocation("appliedenergistics2",
            "white_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_ORANGE = new ResourceLocation("appliedenergistics2",
            "orange_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_MAGENTA = new ResourceLocation("appliedenergistics2",
            "magenta_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_LIGHT_BLUE = new ResourceLocation("appliedenergistics2",
            "light_blue_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_YELLOW = new ResourceLocation("appliedenergistics2",
            "yellow_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_LIME = new ResourceLocation("appliedenergistics2",
            "lime_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_PINK = new ResourceLocation("appliedenergistics2",
            "pink_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_GRAY = new ResourceLocation("appliedenergistics2",
            "gray_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_LIGHT_GRAY = new ResourceLocation("appliedenergistics2",
            "light_gray_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_CYAN = new ResourceLocation("appliedenergistics2",
            "cyan_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_PURPLE = new ResourceLocation("appliedenergistics2",
            "purple_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_BLUE = new ResourceLocation("appliedenergistics2",
            "blue_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_BROWN = new ResourceLocation("appliedenergistics2",
            "brown_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_GREEN = new ResourceLocation("appliedenergistics2",
            "green_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_RED = new ResourceLocation("appliedenergistics2",
            "red_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_BLACK = new ResourceLocation("appliedenergistics2",
            "black_covered_dense_cable");

    public static final ResourceLocation CABLE_DENSE_COVERED_TRANSPARENT = new ResourceLocation("appliedenergistics2",
            "fluix_covered_dense_cable");

    public static final Map<AEColor, ResourceLocation> CABLE_DENSE_COVERED = ImmutableMap
            .<AEColor, ResourceLocation>builder().put(AEColor.WHITE, CABLE_DENSE_COVERED_WHITE)
            .put(AEColor.ORANGE, CABLE_DENSE_COVERED_ORANGE)
            .put(AEColor.MAGENTA, CABLE_DENSE_COVERED_MAGENTA)
            .put(AEColor.LIGHT_BLUE, CABLE_DENSE_COVERED_LIGHT_BLUE)
            .put(AEColor.YELLOW, CABLE_DENSE_COVERED_YELLOW)
            .put(AEColor.LIME, CABLE_DENSE_COVERED_LIME)
            .put(AEColor.PINK, CABLE_DENSE_COVERED_PINK)
            .put(AEColor.GRAY, CABLE_DENSE_COVERED_GRAY)
            .put(AEColor.LIGHT_GRAY, CABLE_DENSE_COVERED_LIGHT_GRAY)
            .put(AEColor.CYAN, CABLE_DENSE_COVERED_CYAN)
            .put(AEColor.PURPLE, CABLE_DENSE_COVERED_PURPLE)
            .put(AEColor.BLUE, CABLE_DENSE_COVERED_BLUE)
            .put(AEColor.BROWN, CABLE_DENSE_COVERED_BROWN)
            .put(AEColor.GREEN, CABLE_DENSE_COVERED_GREEN)
            .put(AEColor.RED, CABLE_DENSE_COVERED_RED)
            .put(AEColor.BLACK, CABLE_DENSE_COVERED_BLACK)
            .put(AEColor.TRANSPARENT, CABLE_DENSE_COVERED_TRANSPARENT)
            .build();

    public static final ResourceLocation IDENTITY_ANNIHILATION_PLANE = new ResourceLocation("appliedenergistics2",
            "identity_annihilation_plane");

    public static final ResourceLocation P2_P_TUNNEL_REDSTONE = new ResourceLocation("appliedenergistics2",
            "redstone_p2p_tunnel");

}
