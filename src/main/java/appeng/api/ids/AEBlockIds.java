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

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/**
 * Contains {@link Block} ids for various blocks defined by Ae2.
 */
@SuppressWarnings("unused")
public final class AEBlockIds {

    ///
    /// WORLDGEN/MISC
    ///
    // Budding certus quartz
    public static final Identifier FLAWLESS_BUDDING_QUARTZ = id("flawless_budding_quartz");
    public static final Identifier FLAWED_BUDDING_QUARTZ = id("flawed_budding_quartz");
    public static final Identifier CHIPPED_BUDDING_QUARTZ = id("chipped_budding_quartz");
    public static final Identifier DAMAGED_BUDDING_QUARTZ = id("damaged_budding_quartz");
    // Certus quartz clusters
    public static final Identifier SMALL_QUARTZ_BUD = id("small_quartz_bud");
    public static final Identifier MEDIUM_QUARTZ_BUD = id("medium_quartz_bud");
    public static final Identifier LARGE_QUARTZ_BUD = id("large_quartz_bud");
    public static final Identifier QUARTZ_CLUSTER = id("quartz_cluster");

    public static final Identifier MYSTERIOUS_CUBE = id("mysterious_cube");
    public static final Identifier NOT_SO_MYSTERIOUS_CUBE = id("not_so_mysterious_cube");
    public static final Identifier QUARTZ_FIXTURE = id("quartz_fixture");
    public static final Identifier SKY_STONE_CHEST = id("sky_stone_chest");
    public static final Identifier SMOOTH_SKY_STONE_CHEST = id("smooth_sky_stone_chest");
    public static final Identifier SKY_STONE_TANK = id("sky_stone_tank");
    public static final Identifier LIGHT_DETECTOR = id("light_detector");
    public static final Identifier PAINT = id("paint");

    ///
    /// ME NETWORK
    ///
    public static final Identifier INSCRIBER = id("inscriber");
    public static final Identifier WIRELESS_ACCESS_POINT = id("wireless_access_point");
    public static final Identifier CHARGER = id("charger");
    public static final Identifier QUANTUM_RING = id("quantum_ring");
    public static final Identifier QUANTUM_LINK = id("quantum_link");
    public static final Identifier CONTROLLER = id("controller");
    public static final Identifier DRIVE = id("drive");
    public static final Identifier ME_CHEST = id("me_chest");
    public static final Identifier INTERFACE = id("interface");
    public static final Identifier CELL_WORKBENCH = id("cell_workbench");
    public static final Identifier IO_PORT = id("io_port");
    public static final Identifier CONDENSER = id("condenser");
    public static final Identifier ENERGY_ACCEPTOR = id("energy_acceptor");
    public static final Identifier CRYSTAL_RESONANCE_GENERATOR = id("crystal_resonance_generator");

    public static final Identifier VIBRATION_CHAMBER = id("vibration_chamber");
    public static final Identifier GROWTH_ACCELERATOR = id("growth_accelerator");
    public static final Identifier ENERGY_CELL = id("energy_cell");
    public static final Identifier DENSE_ENERGY_CELL = id("dense_energy_cell");
    public static final Identifier CABLE_BUS = id("cable_bus");

    ///
    /// SPATIAL
    ///
    public static final Identifier MATRIX_FRAME = id("matrix_frame");
    public static final Identifier TINY_TNT = id("tiny_tnt");
    public static final Identifier SPATIAL_PYLON = id("spatial_pylon");
    public static final Identifier SPATIAL_IO_PORT = id("spatial_io_port");
    public static final Identifier SPATIAL_ANCHOR = id("spatial_anchor");

    ///
    /// AUTO CRAFTING
    ///
    public static final Identifier CREATIVE_ENERGY_CELL = id("creative_energy_cell");
    public static final Identifier CRAFTING_UNIT = id("crafting_unit");
    public static final Identifier CRAFTING_ACCELERATOR = id("crafting_accelerator");
    public static final Identifier CRAFTING_STORAGE_1K = id("1k_crafting_storage");
    public static final Identifier CRAFTING_STORAGE_4K = id("4k_crafting_storage");
    public static final Identifier CRAFTING_STORAGE_16K = id("16k_crafting_storage");
    public static final Identifier CRAFTING_STORAGE_64K = id("64k_crafting_storage");
    public static final Identifier CRAFTING_STORAGE_256K = id("256k_crafting_storage");
    public static final Identifier CRAFTING_MONITOR = id("crafting_monitor");
    public static final Identifier PATTERN_PROVIDER = id("pattern_provider");
    public static final Identifier MOLECULAR_ASSEMBLER = id("molecular_assembler");

    ///
    /// DECORATIVE BLOCKS
    ///
    public static final Identifier QUARTZ_BLOCK = id("quartz_block");
    public static final Identifier CUT_QUARTZ_BLOCK = id("cut_quartz_block");
    public static final Identifier SMOOTH_QUARTZ_BLOCK = id("smooth_quartz_block");
    public static final Identifier QUARTZ_BRICKS = id("quartz_bricks");
    public static final Identifier QUARTZ_PILLAR = id("quartz_pillar");
    public static final Identifier CHISELED_QUARTZ_BLOCK = id("chiseled_quartz_block");
    public static final Identifier FLUIX_BLOCK = id("fluix_block");
    public static final Identifier SKY_STONE_BLOCK = id("sky_stone_block");
    public static final Identifier SMOOTH_SKY_STONE_BLOCK = id("smooth_sky_stone_block");
    public static final Identifier SKY_STONE_BRICK = id("sky_stone_brick");
    public static final Identifier SKY_STONE_SMALL_BRICK = id("sky_stone_small_brick");
    public static final Identifier QUARTZ_GLASS = id("quartz_glass");
    public static final Identifier QUARTZ_VIBRANT_GLASS = id("quartz_vibrant_glass");

    ///
    /// STAIRS
    ///
    public static final Identifier SKY_STONE_STAIRS = id("sky_stone_stairs");
    public static final Identifier SMOOTH_SKY_STONE_STAIRS = id("smooth_sky_stone_stairs");
    public static final Identifier SKY_STONE_BRICK_STAIRS = id("sky_stone_brick_stairs");
    public static final Identifier SKY_STONE_SMALL_BRICK_STAIRS = id("sky_stone_small_brick_stairs");
    public static final Identifier FLUIX_STAIRS = id("fluix_stairs");
    public static final Identifier QUARTZ_STAIRS = id("quartz_stairs");
    public static final Identifier CUT_QUARTZ_STAIRS = id("cut_quartz_stairs");
    public static final Identifier SMOOTH_QUARTZ_STAIRS = id("smooth_quartz_stairs");
    public static final Identifier QUARTZ_BRICK_STAIRS = id("quartz_brick_stairs");
    public static final Identifier CHISELED_QUARTZ_STAIRS = id("chiseled_quartz_stairs");
    public static final Identifier QUARTZ_PILLAR_STAIRS = id("quartz_pillar_stairs");

    ///
    /// WALLS
    ///
    public static final Identifier SKY_STONE_WALL = id("sky_stone_wall");
    public static final Identifier SMOOTH_SKY_STONE_WALL = id("smooth_sky_stone_wall");
    public static final Identifier SKY_STONE_BRICK_WALL = id("sky_stone_brick_wall");
    public static final Identifier SKY_STONE_SMALL_BRICK_WALL = id("sky_stone_small_brick_wall");
    public static final Identifier FLUIX_WALL = id("fluix_wall");
    public static final Identifier QUARTZ_WALL = id("quartz_wall");
    public static final Identifier CUT_QUARTZ_WALL = id("cut_quartz_wall");
    public static final Identifier SMOOTH_QUARTZ_WALL = id("smooth_quartz_wall");
    public static final Identifier QUARTZ_BRICK_WALL = id("quartz_brick_wall");
    public static final Identifier CHISELED_QUARTZ_WALL = id("chiseled_quartz_wall");
    public static final Identifier QUARTZ_PILLAR_WALL = id("quartz_pillar_wall");

    ///
    /// SLABS
    ///
    public static final Identifier SKY_STONE_SLAB = id("sky_stone_slab");
    public static final Identifier SMOOTH_SKY_STONE_SLAB = id("smooth_sky_stone_slab");
    public static final Identifier SKY_STONE_BRICK_SLAB = id("sky_stone_brick_slab");
    public static final Identifier SKY_STONE_SMALL_BRICK_SLAB = id("sky_stone_small_brick_slab");
    public static final Identifier FLUIX_SLAB = id("fluix_slab");
    public static final Identifier QUARTZ_SLAB = id("quartz_slab");
    public static final Identifier CUT_QUARTZ_SLAB = id("cut_quartz_slab");
    public static final Identifier SMOOTH_QUARTZ_SLAB = id("smooth_quartz_slab");
    public static final Identifier QUARTZ_BRICK_SLAB = id("quartz_brick_slab");
    public static final Identifier CHISELED_QUARTZ_SLAB = id("chiseled_quartz_slab");
    public static final Identifier QUARTZ_PILLAR_SLAB = id("quartz_pillar_slab");

    public static final Identifier CRANK = id("crank");

    private static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(AEConstants.MOD_ID, id);
    }
}
