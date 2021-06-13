package appeng.api.definitions;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.ResourceLocation;

import appeng.api.util.AEColor;

public final class AEItemIds {
    public static final ResourceLocation TOOL_DEBUG_CARD = new ResourceLocation("appliedenergistics2", "debug_card");

    public static final ResourceLocation TOOL_ERASER = new ResourceLocation("appliedenergistics2", "debug_eraser");

    public static final ResourceLocation CERTUS_QUARTZ_AXE = new ResourceLocation("appliedenergistics2",
            "certus_quartz_axe");

    public static final ResourceLocation CERTUS_QUARTZ_HOE = new ResourceLocation("appliedenergistics2",
            "certus_quartz_hoe");

    public static final ResourceLocation NETHER_QUARTZ_HOE = new ResourceLocation("appliedenergistics2",
            "nether_quartz_hoe");

    public static final ResourceLocation NETWORK_TOOL = new ResourceLocation("appliedenergistics2", "network_tool");

    public static final ResourceLocation PORTABLE_CELL64K = new ResourceLocation("appliedenergistics2",
            "64k_portable_cell");

    public static final ResourceLocation VIEW_CELL = new ResourceLocation("appliedenergistics2", "view_cell");

    public static final ResourceLocation FLUID_CELL4K = new ResourceLocation("appliedenergistics2",
            "4k_fluid_storage_cell");

    public static final ResourceLocation FLUID_CELL1K = new ResourceLocation("appliedenergistics2",
            "1k_fluid_storage_cell");

    public static final ResourceLocation CELL_CREATIVE = new ResourceLocation("appliedenergistics2",
            "creative_storage_cell");

    public static final ResourceLocation PORTABLE_CELL4K = new ResourceLocation("appliedenergistics2",
            "4k_portable_cell");

    public static final ResourceLocation CELL4K = new ResourceLocation("appliedenergistics2", "4k_storage_cell");

    public static final ResourceLocation FLUID_CELL64K = new ResourceLocation("appliedenergistics2",
            "64k_fluid_storage_cell");

    public static final ResourceLocation MEMORY_CARD = new ResourceLocation("appliedenergistics2", "memory_card");

    public static final ResourceLocation CELL64K = new ResourceLocation("appliedenergistics2", "64k_storage_cell");

    public static final ResourceLocation SPATIAL_CELL128 = new ResourceLocation("appliedenergistics2",
            "128_cubed_spatial_storage_cell");

    public static final ResourceLocation NETHER_QUARTZ_AXE = new ResourceLocation("appliedenergistics2",
            "nether_quartz_axe");

    public static final ResourceLocation NETHER_QUARTZ_SEED = new ResourceLocation("appliedenergistics2",
            "nether_quartz_seed");

    public static final ResourceLocation CELL16K = new ResourceLocation("appliedenergistics2", "16k_storage_cell");

    public static final ResourceLocation ENCODED_PATTERN = new ResourceLocation("appliedenergistics2",
            "encoded_pattern");

    public static final ResourceLocation SPATIAL_CELL16 = new ResourceLocation("appliedenergistics2",
            "16_cubed_spatial_storage_cell");

    public static final ResourceLocation PORTABLE_CELL1K = new ResourceLocation("appliedenergistics2", "portable_cell");

    public static final ResourceLocation MASS_CANNON = new ResourceLocation("appliedenergistics2", "matter_cannon");

    public static final ResourceLocation CERTUS_QUARTZ_PICK = new ResourceLocation("appliedenergistics2",
            "certus_quartz_pickaxe");

    public static final ResourceLocation PORTABLE_CELL16K = new ResourceLocation("appliedenergistics2",
            "16k_portable_cell");

    public static final ResourceLocation CELL1K = new ResourceLocation("appliedenergistics2", "1k_storage_cell");

    public static final ResourceLocation SPATIAL_CELL2 = new ResourceLocation("appliedenergistics2",
            "2_cubed_spatial_storage_cell");

    public static final ResourceLocation FLUIX_CRYSTAL_SEED = new ResourceLocation("appliedenergistics2",
            "fluix_crystal_seed");

    public static final ResourceLocation DUMMY_FLUID_ITEM = new ResourceLocation("appliedenergistics2",
            "dummy_fluid_item");

    public static final ResourceLocation FLUID_CELL16K = new ResourceLocation("appliedenergistics2",
            "16k_fluid_storage_cell");

    public static final ResourceLocation NETHER_QUARTZ_PICK = new ResourceLocation("appliedenergistics2",
            "nether_quartz_pickaxe");

    public static final ResourceLocation BIOMETRIC_CARD = new ResourceLocation("appliedenergistics2", "biometric_card");

    public static final ResourceLocation COLORED_PAINT_BALL_WHITE = new ResourceLocation("appliedenergistics2",
            "white_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_ORANGE = new ResourceLocation("appliedenergistics2",
            "orange_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_MAGENTA = new ResourceLocation("appliedenergistics2",
            "magenta_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_LIGHT_BLUE = new ResourceLocation("appliedenergistics2",
            "light_blue_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_YELLOW = new ResourceLocation("appliedenergistics2",
            "yellow_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_LIME = new ResourceLocation("appliedenergistics2",
            "lime_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_PINK = new ResourceLocation("appliedenergistics2",
            "pink_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_GRAY = new ResourceLocation("appliedenergistics2",
            "gray_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_LIGHT_GRAY = new ResourceLocation("appliedenergistics2",
            "light_gray_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_CYAN = new ResourceLocation("appliedenergistics2",
            "cyan_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_PURPLE = new ResourceLocation("appliedenergistics2",
            "purple_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_BLUE = new ResourceLocation("appliedenergistics2",
            "blue_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_BROWN = new ResourceLocation("appliedenergistics2",
            "brown_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_GREEN = new ResourceLocation("appliedenergistics2",
            "green_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_RED = new ResourceLocation("appliedenergistics2",
            "red_paint_ball");

    public static final ResourceLocation COLORED_PAINT_BALL_BLACK = new ResourceLocation("appliedenergistics2",
            "black_paint_ball");

    public static final Map<AEColor, ResourceLocation> COLORED_PAINT_BALL = ImmutableMap
            .<AEColor, ResourceLocation>builder().put(AEColor.WHITE, COLORED_PAINT_BALL_WHITE)
            .put(AEColor.ORANGE, COLORED_PAINT_BALL_ORANGE)
            .put(AEColor.MAGENTA, COLORED_PAINT_BALL_MAGENTA)
            .put(AEColor.LIGHT_BLUE, COLORED_PAINT_BALL_LIGHT_BLUE)
            .put(AEColor.YELLOW, COLORED_PAINT_BALL_YELLOW)
            .put(AEColor.LIME, COLORED_PAINT_BALL_LIME)
            .put(AEColor.PINK, COLORED_PAINT_BALL_PINK)
            .put(AEColor.GRAY, COLORED_PAINT_BALL_GRAY)
            .put(AEColor.LIGHT_GRAY, COLORED_PAINT_BALL_LIGHT_GRAY)
            .put(AEColor.CYAN, COLORED_PAINT_BALL_CYAN)
            .put(AEColor.PURPLE, COLORED_PAINT_BALL_PURPLE)
            .put(AEColor.BLUE, COLORED_PAINT_BALL_BLUE)
            .put(AEColor.BROWN, COLORED_PAINT_BALL_BROWN)
            .put(AEColor.GREEN, COLORED_PAINT_BALL_GREEN)
            .put(AEColor.RED, COLORED_PAINT_BALL_RED)
            .put(AEColor.BLACK, COLORED_PAINT_BALL_BLACK)
            .build();

    public static final ResourceLocation ENTROPY_MANIPULATOR = new ResourceLocation("appliedenergistics2",
            "entropy_manipulator");

    public static final ResourceLocation TOOL_REPLICATOR_CARD = new ResourceLocation("appliedenergistics2",
            "debug_replicator_card");

    public static final ResourceLocation TOOL_METEORITE_PLACER = new ResourceLocation("appliedenergistics2",
            "debug_meteorite_placer");

    public static final ResourceLocation FACADE = new ResourceLocation("appliedenergistics2", "facade");

    public static final ResourceLocation CHARGED_STAFF = new ResourceLocation("appliedenergistics2", "charged_staff");

    public static final ResourceLocation PORTABLE_CELL = new ResourceLocation("appliedenergistics2", "portable_cell");

    public static final ResourceLocation COLOR_APPLICATOR = new ResourceLocation("appliedenergistics2",
            "color_applicator");

    public static final ResourceLocation WIRELESS_TERMINAL = new ResourceLocation("appliedenergistics2",
            "wireless_terminal");

    public static final ResourceLocation CERTUS_CRYSTAL_SEED = new ResourceLocation("appliedenergistics2",
            "certus_crystal_seed");

    public static final ResourceLocation CERTUS_QUARTZ_SHOVEL = new ResourceLocation("appliedenergistics2",
            "certus_quartz_shovel");

    public static final ResourceLocation NETHER_QUARTZ_SHOVEL = new ResourceLocation("appliedenergistics2",
            "nether_quartz_shovel");

    public static final ResourceLocation NETHER_QUARTZ_SWORD = new ResourceLocation("appliedenergistics2",
            "nether_quartz_sword");

    public static final ResourceLocation CERTUS_QUARTZ_SWORD = new ResourceLocation("appliedenergistics2",
            "certus_quartz_sword");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_WHITE = new ResourceLocation("appliedenergistics2",
            "white_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_ORANGE = new ResourceLocation("appliedenergistics2",
            "orange_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_MAGENTA = new ResourceLocation("appliedenergistics2",
            "magenta_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_LIGHT_BLUE = new ResourceLocation(
            "appliedenergistics2", "light_blue_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_YELLOW = new ResourceLocation("appliedenergistics2",
            "yellow_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_LIME = new ResourceLocation("appliedenergistics2",
            "lime_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_PINK = new ResourceLocation("appliedenergistics2",
            "pink_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_GRAY = new ResourceLocation("appliedenergistics2",
            "gray_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_LIGHT_GRAY = new ResourceLocation(
            "appliedenergistics2", "light_gray_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_CYAN = new ResourceLocation("appliedenergistics2",
            "cyan_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_PURPLE = new ResourceLocation("appliedenergistics2",
            "purple_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_BLUE = new ResourceLocation("appliedenergistics2",
            "blue_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_BROWN = new ResourceLocation("appliedenergistics2",
            "brown_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_GREEN = new ResourceLocation("appliedenergistics2",
            "green_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_RED = new ResourceLocation("appliedenergistics2",
            "red_lumen_paint_ball");

    public static final ResourceLocation COLORED_LUMEN_PAINT_BALL_BLACK = new ResourceLocation("appliedenergistics2",
            "black_lumen_paint_ball");

    public static final Map<AEColor, ResourceLocation> COLORED_LUMEN_PAINT_BALL = ImmutableMap
            .<AEColor, ResourceLocation>builder().put(AEColor.WHITE, COLORED_LUMEN_PAINT_BALL_WHITE)
            .put(AEColor.ORANGE, COLORED_LUMEN_PAINT_BALL_ORANGE)
            .put(AEColor.MAGENTA, COLORED_LUMEN_PAINT_BALL_MAGENTA)
            .put(AEColor.LIGHT_BLUE, COLORED_LUMEN_PAINT_BALL_LIGHT_BLUE)
            .put(AEColor.YELLOW, COLORED_LUMEN_PAINT_BALL_YELLOW)
            .put(AEColor.LIME, COLORED_LUMEN_PAINT_BALL_LIME)
            .put(AEColor.PINK, COLORED_LUMEN_PAINT_BALL_PINK)
            .put(AEColor.GRAY, COLORED_LUMEN_PAINT_BALL_GRAY)
            .put(AEColor.LIGHT_GRAY, COLORED_LUMEN_PAINT_BALL_LIGHT_GRAY)
            .put(AEColor.CYAN, COLORED_LUMEN_PAINT_BALL_CYAN)
            .put(AEColor.PURPLE, COLORED_LUMEN_PAINT_BALL_PURPLE)
            .put(AEColor.BLUE, COLORED_LUMEN_PAINT_BALL_BLUE)
            .put(AEColor.BROWN, COLORED_LUMEN_PAINT_BALL_BROWN)
            .put(AEColor.GREEN, COLORED_LUMEN_PAINT_BALL_GREEN)
            .put(AEColor.RED, COLORED_LUMEN_PAINT_BALL_RED)
            .put(AEColor.BLACK, COLORED_LUMEN_PAINT_BALL_BLACK)
            .build();

    public static final ResourceLocation NETHER_QUARTZ_WRENCH = new ResourceLocation("appliedenergistics2",
            "nether_quartz_wrench");

    public static final ResourceLocation CERTUS_QUARTZ_WRENCH = new ResourceLocation("appliedenergistics2",
            "certus_quartz_wrench");

    public static final ResourceLocation CERTUS_QUARTZ_KNIFE = new ResourceLocation("appliedenergistics2",
            "certus_quartz_cutting_knife");

    public static final ResourceLocation NETHER_QUARTZ_KNIFE = new ResourceLocation("appliedenergistics2",
            "nether_quartz_cutting_knife");

}
