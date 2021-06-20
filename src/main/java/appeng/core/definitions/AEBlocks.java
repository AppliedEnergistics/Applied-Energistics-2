/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.definitions;

import static appeng.block.AEBaseBlock.defaultProps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

import appeng.api.ids.AEBlockIds;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.block.AEBaseBlockItemChargeable;
import appeng.block.crafting.AbstractCraftingUnitBlock.CraftingUnitType;
import appeng.block.crafting.CraftingMonitorBlock;
import appeng.block.crafting.CraftingStorageBlock;
import appeng.block.crafting.CraftingStorageItem;
import appeng.block.crafting.CraftingUnitBlock;
import appeng.block.crafting.MolecularAssemblerBlock;
import appeng.block.grindstone.CrankBlock;
import appeng.block.grindstone.GrinderBlock;
import appeng.block.misc.CellWorkbenchBlock;
import appeng.block.misc.ChargerBlock;
import appeng.block.misc.CondenserBlock;
import appeng.block.misc.InscriberBlock;
import appeng.block.misc.InterfaceBlock;
import appeng.block.misc.LightDetectorBlock;
import appeng.block.misc.QuartzFixtureBlock;
import appeng.block.misc.QuartzGrowthAcceleratorBlock;
import appeng.block.misc.SecurityStationBlock;
import appeng.block.misc.SkyCompassBlock;
import appeng.block.misc.TinyTNTBlock;
import appeng.block.misc.VibrationChamberBlock;
import appeng.block.networking.CableBusBlock;
import appeng.block.networking.ControllerBlock;
import appeng.block.networking.CreativeEnergyCellBlock;
import appeng.block.networking.DenseEnergyCellBlock;
import appeng.block.networking.EnergyAcceptorBlock;
import appeng.block.networking.EnergyCellBlock;
import appeng.block.networking.WirelessBlock;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.block.qnb.QuantumLinkChamberBlock;
import appeng.block.qnb.QuantumRingBlock;
import appeng.block.spatial.MatrixFrameBlock;
import appeng.block.spatial.SpatialAnchorBlock;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.block.spatial.SpatialPylonBlock;
import appeng.block.storage.ChestBlock;
import appeng.block.storage.DriveBlock;
import appeng.block.storage.IOPortBlock;
import appeng.block.storage.SkyChestBlock;
import appeng.core.AEItemGroup;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.debug.ChunkLoaderBlock;
import appeng.debug.CubeGeneratorBlock;
import appeng.debug.EnergyGeneratorBlock;
import appeng.debug.ItemGenBlock;
import appeng.debug.PhantomNodeBlock;
import appeng.decorative.AEDecorativeBlock;
import appeng.decorative.solid.ChargedQuartzOreBlock;
import appeng.decorative.solid.QuartzGlassBlock;
import appeng.decorative.solid.QuartzLampBlock;
import appeng.decorative.solid.QuartzOreBlock;
import appeng.decorative.solid.QuartzPillarBlock;
import appeng.decorative.solid.SkyStoneBlock;
import appeng.decorative.solid.SkyStoneBlock.SkystoneType;
import appeng.fluids.block.FluidInterfaceBlock;

/**
 * Internal implementation for the API blocks
 */
@SuppressWarnings("unused")
public final class AEBlocks {

    private static final List<BlockDefinition> BLOCKS = new ArrayList<>();
    private static final AbstractBlock.Properties QUARTZ_PROPERTIES = defaultProps(Material.ROCK)
            .hardnessAndResistance(3, 5).setRequiresTool().harvestLevel(1);
    private static final AbstractBlock.Properties SKYSTONE_PROPERTIES = defaultProps(Material.ROCK)
            .hardnessAndResistance(50, 150).setRequiresTool();
    private static final AbstractBlock.IExtendedPositionPredicate<EntityType<?>> NEVER_ALLOW_SPAWN = (p1, p2, p3,
            p4) -> false;
    private static final AbstractBlock.Properties SKY_STONE_CHEST_PROPS = defaultProps(Material.ROCK)
            .hardnessAndResistance(50, 150).notSolid();

    // spotless:off
    public static final BlockDefinition QUARTZ_ORE = block(AEBlockIds.QUARTZ_ORE, () -> new QuartzOreBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition QUARTZ_ORE_CHARGED = block(AEBlockIds.QUARTZ_ORE_CHARGED, () -> new ChargedQuartzOreBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition MATRIX_FRAME = block(AEBlockIds.MATRIX_FRAME, MatrixFrameBlock::new).build();

    public static final BlockDefinition QUARTZ_BLOCK = decoBlock(AEBlockIds.QUARTZ_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition QUARTZ_PILLAR = decoBlock(AEBlockIds.QUARTZ_PILLAR, () -> new QuartzPillarBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition CHISELED_QUARTZ_BLOCK = decoBlock(AEBlockIds.CHISELED_QUARTZ_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES)).build();

    public static final BlockDefinition QUARTZ_GLASS = block(AEBlockIds.QUARTZ_GLASS, () -> new QuartzGlassBlock(defaultProps(Material.GLASS).notSolid().setAllowsSpawn(NEVER_ALLOW_SPAWN))).build();
    public static final BlockDefinition QUARTZ_VIBRANT_GLASS = decoBlock(AEBlockIds.QUARTZ_VIBRANT_GLASS, () -> new QuartzLampBlock(defaultProps(Material.GLASS).setLightLevel(b -> 15).notSolid()
            .setAllowsSpawn(NEVER_ALLOW_SPAWN))).build();

    public static final BlockDefinition QUARTZ_FIXTURE = block(AEBlockIds.QUARTZ_FIXTURE, QuartzFixtureBlock::new).build();
    public static final BlockDefinition FLUIX_BLOCK = block(AEBlockIds.FLUIX_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition SKY_STONE_BLOCK = block(AEBlockIds.SKY_STONE_BLOCK, () -> new SkyStoneBlock(SkystoneType.STONE,
            defaultProps(Material.ROCK).hardnessAndResistance(50, 150).setRequiresTool().harvestLevel(3))).build();

    public static final BlockDefinition SMOOTH_SKY_STONE_BLOCK = block(AEBlockIds.SMOOTH_SKY_STONE_BLOCK, () -> new SkyStoneBlock(SkystoneType.BLOCK, SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SKY_STONE_BRICK = decoBlock(AEBlockIds.SKY_STONE_BRICK, () -> new SkyStoneBlock(SkystoneType.BRICK, SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SKY_STONE_SMALL_BRICK = decoBlock(AEBlockIds.SKY_STONE_SMALL_BRICK, () -> new SkyStoneBlock(SkystoneType.SMALL_BRICK, SKYSTONE_PROPERTIES)).build();

    public static final BlockDefinition SKY_STONE_CHEST = block(AEBlockIds.SKY_STONE_CHEST, () -> new SkyChestBlock(SkyChestBlock.SkyChestType.STONE, SKY_STONE_CHEST_PROPS)).build();
    public static final BlockDefinition SMOOTH_SKY_STONE_CHEST = block(AEBlockIds.SMOOTH_SKY_STONE_CHEST, () -> new SkyChestBlock(SkyChestBlock.SkyChestType.BLOCK, SKY_STONE_CHEST_PROPS)).build();

    public static final BlockDefinition SKY_COMPASS = block(AEBlockIds.SKY_COMPASS, () -> new SkyCompassBlock(defaultProps(Material.MISCELLANEOUS))).build();
    public static final BlockDefinition GRINDSTONE = block(AEBlockIds.GRINDSTONE, () -> new GrinderBlock(defaultProps(Material.ROCK).hardnessAndResistance(3.2f))).build();
    public static final BlockDefinition CRANK = block(AEBlockIds.CRANK, () -> new CrankBlock(defaultProps(Material.WOOD).harvestTool(ToolType.AXE).harvestLevel(0).notSolid())).build();
    public static final BlockDefinition INSCRIBER = block(AEBlockIds.INSCRIBER, () -> new InscriberBlock(defaultProps(Material.IRON).notSolid())).build();
    public static final BlockDefinition WIRELESS_ACCESS_POINT = block(AEBlockIds.WIRELESS_ACCESS_POINT, WirelessBlock::new).build();
    public static final BlockDefinition CHARGER = block(AEBlockIds.CHARGER, ChargerBlock::new).build();

    public static final BlockDefinition TINY_TNT = block(AEBlockIds.TINY_TNT, () -> new TinyTNTBlock(defaultProps(Material.TNT).sound(SoundType.GROUND).hardnessAndResistance(0).notSolid())).build();
    public static final BlockDefinition SECURITY_STATION = block(AEBlockIds.SECURITY_STATION, SecurityStationBlock::new).build();

    public static final BlockDefinition QUANTUM_RING = block(AEBlockIds.QUANTUM_RING, QuantumRingBlock::new).build();
    public static final BlockDefinition QUANTUM_LINK = block(AEBlockIds.QUANTUM_LINK, QuantumLinkChamberBlock::new).build();
    public static final BlockDefinition SPATIAL_PYLON = block(AEBlockIds.SPATIAL_PYLON, SpatialPylonBlock::new).build();
    public static final BlockDefinition SPATIAL_IO_PORT = block(AEBlockIds.SPATIAL_IO_PORT, SpatialIOPortBlock::new).build();
    public static final BlockDefinition CONTROLLER = block(AEBlockIds.CONTROLLER, ControllerBlock::new).build();
    public static final BlockDefinition DRIVE = block(AEBlockIds.DRIVE, DriveBlock::new).build();
    public static final BlockDefinition CHEST = block(AEBlockIds.CHEST, ChestBlock::new).build();
    public static final BlockDefinition INTERFACE = block(AEBlockIds.ITEM_INTERFACE, InterfaceBlock::new).build();
    public static final BlockDefinition FLUID_INTERFACE = block(AEBlockIds.FLUID_INTERFACE, FluidInterfaceBlock::new).build();
    public static final BlockDefinition CELL_WORKBENCH = block(AEBlockIds.CELL_WORKBENCH, CellWorkbenchBlock::new).build();
    public static final BlockDefinition IO_PORT = block(AEBlockIds.IO_PORT, IOPortBlock::new).build();
    public static final BlockDefinition CONDENSER = block(AEBlockIds.CONDENSER, CondenserBlock::new).build();
    public static final BlockDefinition ENERGY_ACCEPTOR = block(AEBlockIds.ENERGY_ACCEPTOR, EnergyAcceptorBlock::new).build();
    public static final BlockDefinition VIBRATION_CHAMBER = block(AEBlockIds.VIBRATION_CHAMBER, VibrationChamberBlock::new).build();
    public static final BlockDefinition QUARTZ_GROWTH_ACCELERATOR = block(AEBlockIds.QUARTZ_GROWTH_ACCELERATOR, QuartzGrowthAcceleratorBlock::new).build();
    public static final BlockDefinition ENERGY_CELL = block(AEBlockIds.ENERGY_CELL, EnergyCellBlock::new).item(AEBaseBlockItemChargeable::new).build();
    public static final BlockDefinition DENSE_ENERGY_CELL = block(AEBlockIds.DENSE_ENERGY_CELL, DenseEnergyCellBlock::new).item(AEBaseBlockItemChargeable::new).build();
    public static final BlockDefinition CREATIVE_ENERGY_CELL = block(AEBlockIds.CREATIVE_ENERGY_CELL, CreativeEnergyCellBlock::new).build();

    public static final BlockDefinition CRAFTING_UNIT = craftingBlock(AEBlockIds.CRAFTING_UNIT, () -> new CraftingUnitBlock(defaultProps(Material.IRON), CraftingUnitType.UNIT)).build();
    public static final BlockDefinition CRAFTING_ACCELERATOR = craftingBlock(AEBlockIds.CRAFTING_ACCELERATOR, () -> new CraftingUnitBlock(defaultProps(Material.IRON), CraftingUnitType.ACCELERATOR)).build();

    public static final BlockDefinition CRAFTING_STORAGE_1K = craftingBlock(AEBlockIds.CRAFTING_STORAGE_1K, () -> new CraftingStorageBlock(defaultProps(Material.IRON), CraftingUnitType.STORAGE_1K)).item(CraftingStorageItem::new).build();
    public static final BlockDefinition CRAFTING_STORAGE_4K = craftingBlock(AEBlockIds.CRAFTING_STORAGE_4K, () -> new CraftingStorageBlock(defaultProps(Material.IRON), CraftingUnitType.STORAGE_4K)).item(CraftingStorageItem::new).build();
    public static final BlockDefinition CRAFTING_STORAGE_16K = craftingBlock(AEBlockIds.CRAFTING_STORAGE_16K, () -> new CraftingStorageBlock(defaultProps(Material.IRON), CraftingUnitType.STORAGE_16K)).item(CraftingStorageItem::new).build();
    public static final BlockDefinition CRAFTING_STORAGE_64K = craftingBlock(AEBlockIds.CRAFTING_STORAGE_64K, () -> new CraftingStorageBlock(defaultProps(Material.IRON), CraftingUnitType.STORAGE_64K)).item(CraftingStorageItem::new).build();
    public static final BlockDefinition CRAFTING_MONITOR = craftingBlock(AEBlockIds.CRAFTING_MONITOR, () -> new CraftingMonitorBlock(defaultProps(Material.IRON))).build();

    public static final BlockDefinition MOLECULAR_ASSEMBLER = block(AEBlockIds.MOLECULAR_ASSEMBLER, () -> new MolecularAssemblerBlock(defaultProps(Material.IRON).notSolid())).build();

    public static final BlockDefinition LIGHT_DETECTOR = block(AEBlockIds.LIGHT_DETECTOR, LightDetectorBlock::new).build();
    public static final BlockDefinition PAINT = block(AEBlockIds.PAINT, PaintSplotchesBlock::new).build();

    public static final BlockDefinition SKY_STONE_STAIRS = decoBlock(AEBlockIds.SKY_STONE_STAIRS, () -> new StairsBlock(SKY_STONE_BLOCK.block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SMOOTH_SKY_STONE_STAIRS = decoBlock(AEBlockIds.SMOOTH_SKY_STONE_STAIRS, () -> new StairsBlock(SMOOTH_SKY_STONE_BLOCK.block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SKY_STONE_BRICK_STAIRS = decoBlock(AEBlockIds.SKY_STONE_BRICK_STAIRS, () -> new StairsBlock(SKY_STONE_BRICK.block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SKY_STONE_SMALL_BRICK_STAIRS = decoBlock(AEBlockIds.SKY_STONE_SMALL_BRICK_STAIRS, () -> new StairsBlock(SKY_STONE_SMALL_BRICK.block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition FLUIX_STAIRS = decoBlock(AEBlockIds.FLUIX_STAIRS, () -> new StairsBlock(FLUIX_BLOCK.block()::getDefaultState, QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition QUARTZ_STAIRS = decoBlock(AEBlockIds.QUARTZ_STAIRS, () -> new StairsBlock(QUARTZ_BLOCK.block()::getDefaultState, QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition CHISELED_QUARTZ_STAIRS = decoBlock(AEBlockIds.CHISELED_QUARTZ_STAIRS, () -> new StairsBlock(CHISELED_QUARTZ_BLOCK.block()::getDefaultState, QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition QUARTZ_PILLAR_STAIRS = decoBlock(AEBlockIds.QUARTZ_PILLAR_STAIRS, () -> new StairsBlock(QUARTZ_PILLAR.block()::getDefaultState, QUARTZ_PROPERTIES)).build();

    public static final BlockDefinition SKY_STONE_WALL = decoBlock(AEBlockIds.SKY_STONE_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SMOOTH_SKY_STONE_WALL = decoBlock(AEBlockIds.SMOOTH_SKY_STONE_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SKY_STONE_BRICK_WALL = decoBlock(AEBlockIds.SKY_STONE_BRICK_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SKY_STONE_SMALL_BRICK_WALL = decoBlock(AEBlockIds.SKY_STONE_SMALL_BRICK_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition FLUIX_WALL = decoBlock(AEBlockIds.FLUIX_WALL, () -> new WallBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition QUARTZ_WALL = decoBlock(AEBlockIds.QUARTZ_WALL, () -> new WallBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition CHISELED_QUARTZ_WALL = decoBlock(AEBlockIds.CHISELED_QUARTZ_WALL, () -> new WallBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition QUARTZ_PILLAR_WALL = decoBlock(AEBlockIds.QUARTZ_PILLAR_WALL, () -> new WallBlock(QUARTZ_PROPERTIES)).build();

    public static final BlockDefinition MULTI_PART = block(AEBlockIds.CABLE_BUS, CableBusBlock::new).build();

    public static final BlockDefinition SKY_STONE_SLAB = decoBlock(AEBlockIds.SKY_STONE_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SMOOTH_SKY_STONE_SLAB = decoBlock(AEBlockIds.SMOOTH_SKY_STONE_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SKY_STONE_BRICK_SLAB = decoBlock(AEBlockIds.SKY_STONE_BRICK_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES)).build();
    public static final BlockDefinition SKY_STONE_SMALL_BRICK_SLAB = decoBlock(AEBlockIds.SKY_STONE_SMALL_BRICK_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES)).build();

    public static final BlockDefinition FLUIX_SLAB = decoBlock(AEBlockIds.FLUIX_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition QUARTZ_SLAB = decoBlock(AEBlockIds.QUARTZ_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition CHISELED_QUARTZ_SLAB = decoBlock(AEBlockIds.CHISELED_QUARTZ_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES)).build();
    public static final BlockDefinition QUARTZ_PILLAR_SLAB = decoBlock(AEBlockIds.QUARTZ_PILLAR_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES)).build();

    public static final BlockDefinition SPATIAL_ANCHOR = block(AEBlockIds.SPATIAL_ANCHOR, SpatialAnchorBlock::new).build();

    ///
    /// DEBUG BLOCKS
    ///
    public static final BlockDefinition DEBUG_ITEM_GEN = block(AppEng.makeId("debug_item_gen"), ItemGenBlock::new).build();
    public static final BlockDefinition DEBUG_CHUNK_LOADER = block(AppEng.makeId("debug_chunk_loader"), ChunkLoaderBlock::new).build();
    public static final BlockDefinition DEBUG_PHANTOM_NODE = block(AppEng.makeId("debug_phantom_node"), PhantomNodeBlock::new).build();
    public static final BlockDefinition DEBUG_CUBE_GEN = block(AppEng.makeId("debug_cube_gen"), CubeGeneratorBlock::new).build();
    public static final BlockDefinition DEBUG_ENERGY_GEN = block(AppEng.makeId("debug_energy_gen"), EnergyGeneratorBlock::new).build();
    // spotless:on

    public static List<BlockDefinition> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    private static Builder block(ResourceLocation id, Supplier<Block> blockSupplier) {
        return new Builder(id, blockSupplier);
    }

    private static Builder decoBlock(ResourceLocation id, Supplier<Block> blockSupplier) {
        return new Builder(id, blockSupplier);
    }

    private static Builder craftingBlock(ResourceLocation id, Supplier<Block> blockSupplier) {
        return new Builder(id, blockSupplier);
    }

    // Used to control in which order static constructors are called
    public static void init() {
    }

    private static class Builder {

        private final ResourceLocation id;

        private final Supplier<? extends Block> blockSupplier;

        private final ItemGroup itemGroup = CreativeTab.INSTANCE;

        private BiFunction<Block, Item.Properties, BlockItem> itemFactory;

        Builder(ResourceLocation id, Supplier<? extends Block> blockSupplier) {
            this.id = id;
            this.blockSupplier = blockSupplier;
        }

        public Builder item(BiFunction<Block, Item.Properties, BlockItem> factory) {
            this.itemFactory = factory;
            return this;
        }

        public BlockDefinition build() {
            // Create block and matching item, and set factory name of both
            Block block = this.blockSupplier.get();
            block.setRegistryName(this.id);

            BlockItem item = this.constructItemFromBlock(block);
            item.setRegistryName(this.id);

            BlockDefinition definition = new BlockDefinition(this.id, block, item);

            if (itemGroup instanceof AEItemGroup) {
                ((AEItemGroup) itemGroup).add(definition);
            }

            BLOCKS.add(definition);

            return definition;
        }

        private BlockItem constructItemFromBlock(Block block) {
            Item.Properties itemProperties = new Item.Properties();

            if (itemGroup != null) {
                itemProperties.group(itemGroup);
            }

            if (this.itemFactory != null) {
                BlockItem item = this.itemFactory.apply(block, itemProperties);
                if (item == null) {
                    throw new IllegalArgumentException("BlockItem factory for " + id + " returned null");
                }
                return item;
            } else if (block instanceof AEBaseBlock) {
                return new AEBaseBlockItem(block, itemProperties);
            } else {
                return new BlockItem(block, itemProperties);
            }
        }
    }

}
