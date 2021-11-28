/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.ids;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;

import appeng.api.util.AEColor;

/**
 * Contains {@link net.minecraft.world.item.Item} ids for various cable bus parts defined by AE2.
 */
@SuppressWarnings("unused")
public final class AEPartIds {

    ///
    /// CABLES
    ///

    public static final ResourceLocation CABLE_GLASS_WHITE = id("white_glass_cable");
    public static final ResourceLocation CABLE_GLASS_ORANGE = id("orange_glass_cable");
    public static final ResourceLocation CABLE_GLASS_MAGENTA = id("magenta_glass_cable");
    public static final ResourceLocation CABLE_GLASS_LIGHT_BLUE = id("light_blue_glass_cable");
    public static final ResourceLocation CABLE_GLASS_YELLOW = id("yellow_glass_cable");
    public static final ResourceLocation CABLE_GLASS_LIME = id("lime_glass_cable");
    public static final ResourceLocation CABLE_GLASS_PINK = id("pink_glass_cable");
    public static final ResourceLocation CABLE_GLASS_GRAY = id("gray_glass_cable");
    public static final ResourceLocation CABLE_GLASS_LIGHT_GRAY = id("light_gray_glass_cable");
    public static final ResourceLocation CABLE_GLASS_CYAN = id("cyan_glass_cable");
    public static final ResourceLocation CABLE_GLASS_PURPLE = id("purple_glass_cable");
    public static final ResourceLocation CABLE_GLASS_BLUE = id("blue_glass_cable");
    public static final ResourceLocation CABLE_GLASS_BROWN = id("brown_glass_cable");
    public static final ResourceLocation CABLE_GLASS_GREEN = id("green_glass_cable");
    public static final ResourceLocation CABLE_GLASS_RED = id("red_glass_cable");
    public static final ResourceLocation CABLE_GLASS_BLACK = id("black_glass_cable");
    public static final ResourceLocation CABLE_GLASS_TRANSPARENT = id("fluix_glass_cable");
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

    public static final ResourceLocation CABLE_COVERED_WHITE = id("white_covered_cable");
    public static final ResourceLocation CABLE_COVERED_ORANGE = id("orange_covered_cable");
    public static final ResourceLocation CABLE_COVERED_MAGENTA = id("magenta_covered_cable");
    public static final ResourceLocation CABLE_COVERED_LIGHT_BLUE = id("light_blue_covered_cable");
    public static final ResourceLocation CABLE_COVERED_YELLOW = id("yellow_covered_cable");
    public static final ResourceLocation CABLE_COVERED_LIME = id("lime_covered_cable");
    public static final ResourceLocation CABLE_COVERED_PINK = id("pink_covered_cable");
    public static final ResourceLocation CABLE_COVERED_GRAY = id("gray_covered_cable");
    public static final ResourceLocation CABLE_COVERED_LIGHT_GRAY = id("light_gray_covered_cable");
    public static final ResourceLocation CABLE_COVERED_CYAN = id("cyan_covered_cable");
    public static final ResourceLocation CABLE_COVERED_PURPLE = id("purple_covered_cable");
    public static final ResourceLocation CABLE_COVERED_BLUE = id("blue_covered_cable");
    public static final ResourceLocation CABLE_COVERED_BROWN = id("brown_covered_cable");
    public static final ResourceLocation CABLE_COVERED_GREEN = id("green_covered_cable");
    public static final ResourceLocation CABLE_COVERED_RED = id("red_covered_cable");
    public static final ResourceLocation CABLE_COVERED_BLACK = id("black_covered_cable");
    public static final ResourceLocation CABLE_COVERED_TRANSPARENT = id("fluix_covered_cable");
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

    public static final ResourceLocation CABLE_SMART_WHITE = id("white_smart_cable");
    public static final ResourceLocation CABLE_SMART_ORANGE = id("orange_smart_cable");
    public static final ResourceLocation CABLE_SMART_MAGENTA = id("magenta_smart_cable");
    public static final ResourceLocation CABLE_SMART_LIGHT_BLUE = id("light_blue_smart_cable");
    public static final ResourceLocation CABLE_SMART_YELLOW = id("yellow_smart_cable");
    public static final ResourceLocation CABLE_SMART_LIME = id("lime_smart_cable");
    public static final ResourceLocation CABLE_SMART_PINK = id("pink_smart_cable");
    public static final ResourceLocation CABLE_SMART_GRAY = id("gray_smart_cable");
    public static final ResourceLocation CABLE_SMART_LIGHT_GRAY = id("light_gray_smart_cable");
    public static final ResourceLocation CABLE_SMART_CYAN = id("cyan_smart_cable");
    public static final ResourceLocation CABLE_SMART_PURPLE = id("purple_smart_cable");
    public static final ResourceLocation CABLE_SMART_BLUE = id("blue_smart_cable");
    public static final ResourceLocation CABLE_SMART_BROWN = id("brown_smart_cable");
    public static final ResourceLocation CABLE_SMART_GREEN = id("green_smart_cable");
    public static final ResourceLocation CABLE_SMART_RED = id("red_smart_cable");
    public static final ResourceLocation CABLE_SMART_BLACK = id("black_smart_cable");
    public static final ResourceLocation CABLE_SMART_TRANSPARENT = id("fluix_smart_cable");
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

    public static final ResourceLocation CABLE_DENSE_COVERED_WHITE = id("white_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_ORANGE = id("orange_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_MAGENTA = id("magenta_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_LIGHT_BLUE = id("light_blue_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_YELLOW = id("yellow_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_LIME = id("lime_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_PINK = id("pink_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_GRAY = id("gray_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_LIGHT_GRAY = id("light_gray_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_CYAN = id("cyan_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_PURPLE = id("purple_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_BLUE = id("blue_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_BROWN = id("brown_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_GREEN = id("green_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_RED = id("red_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_BLACK = id("black_covered_dense_cable");
    public static final ResourceLocation CABLE_DENSE_COVERED_TRANSPARENT = id("fluix_covered_dense_cable");
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

    public static final ResourceLocation CABLE_DENSE_SMART_WHITE = id("white_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_ORANGE = id("orange_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_MAGENTA = id("magenta_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_LIGHT_BLUE = id("light_blue_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_YELLOW = id("yellow_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_LIME = id("lime_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_PINK = id("pink_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_GRAY = id("gray_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_LIGHT_GRAY = id("light_gray_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_CYAN = id("cyan_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_PURPLE = id("purple_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_BLUE = id("blue_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_BROWN = id("brown_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_GREEN = id("green_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_RED = id("red_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_BLACK = id("black_smart_dense_cable");
    public static final ResourceLocation CABLE_DENSE_SMART_TRANSPARENT = id("fluix_smart_dense_cable");
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

    ///
    /// Buses
    ///
    public static final ResourceLocation QUARTZ_FIBER = id("quartz_fiber");
    public static final ResourceLocation TOGGLE_BUS = id("toggle_bus");
    public static final ResourceLocation INVERTED_TOGGLE_BUS = id("inverted_toggle_bus");
    public static final ResourceLocation CABLE_ANCHOR = id("cable_anchor");
    public static final ResourceLocation STORAGE_BUS = id("storage_bus");
    public static final ResourceLocation IMPORT_BUS = id("import_bus");
    public static final ResourceLocation EXPORT_BUS = id("export_bus");
    public static final ResourceLocation LEVEL_EMITTER = id("level_emitter");
    public static final ResourceLocation ENERGY_LEVEL_EMITTER = id("energy_level_emitter");
    public static final ResourceLocation PATTERN_PROVIDER = id("cable_pattern_provider");
    public static final ResourceLocation FLUID_INTERFACE = id("cable_fluid_interface");
    public static final ResourceLocation ITEM_INTERFACE = id("cable_item_interface");
    public static final ResourceLocation INTERFACE_TERMINAL = id("interface_terminal");
    public static final ResourceLocation ITEM_CONVERSION_MONITOR = id("item_conversion_monitor");
    public static final ResourceLocation ENERGY_ACCEPTOR = id("cable_energy_acceptor");

    ///
    /// Monitors and terminals
    ///
    public static final ResourceLocation MONITOR = id("monitor");
    public static final ResourceLocation SEMI_DARK_MONITOR = id("semi_dark_monitor");
    public static final ResourceLocation DARK_MONITOR = id("dark_monitor");
    public static final ResourceLocation TERMINAL = id("terminal");
    public static final ResourceLocation CRAFTING_TERMINAL = id("crafting_terminal");
    public static final ResourceLocation PATTERN_TERMINAL = id("pattern_terminal");
    public static final ResourceLocation FLUID_TERMINAL = id("fluid_terminal");
    public static final ResourceLocation STORAGE_MONITOR = id("storage_monitor");

    ///
    /// Planes
    ///
    public static final ResourceLocation ITEM_FORMATION_PLANE = id("item_formation_plane");
    public static final ResourceLocation ITEM_ANNIHILATION_PLANE = id("item_annihilation_plane");
    public static final ResourceLocation ITEM_IDENTITY_ANNIHILATION_PLANE = id("item_identity_annihilation_plane");
    public static final ResourceLocation FLUID_FORMATION_PLANE = id("fluid_formation_plane");
    public static final ResourceLocation FLUID_ANNIHILATION_PLANE = id("fluid_annihilation_plane");

    ///
    /// P2P
    ///
    public static final ResourceLocation ME_P2P_TUNNEL = id("me_p2p_tunnel");
    public static final ResourceLocation REDSTONE_P2P_TUNNEL = id("redstone_p2p_tunnel");
    public static final ResourceLocation ITEM_P2P_TUNNEL = id("item_p2p_tunnel");
    public static final ResourceLocation FLUID_P2P_TUNNEL = id("fluid_p2p_tunnel");
    public static final ResourceLocation FE_P2P_TUNNEL = id("fe_p2p_tunnel");
    public static final ResourceLocation LIGHT_P2P_TUNNEL = id("light_p2p_tunnel");

    private static ResourceLocation id(String id) {
        return new ResourceLocation(AEConstants.MOD_ID, id);
    }
}
