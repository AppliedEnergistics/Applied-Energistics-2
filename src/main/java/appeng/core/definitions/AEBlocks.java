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

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate;
import net.minecraft.world.level.material.Material;

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
import appeng.block.crafting.PatternProviderBlock;
import appeng.block.grindstone.CrankBlock;
import appeng.block.grindstone.GrinderBlock;
import appeng.block.misc.CellWorkbenchBlock;
import appeng.block.misc.ChargerBlock;
import appeng.block.misc.CondenserBlock;
import appeng.block.misc.FluidInterfaceBlock;
import appeng.block.misc.InscriberBlock;
import appeng.block.misc.ItemInterfaceBlock;
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

/**
 * Internal implementation for the API blocks
 */
@SuppressWarnings("unused")
public final class AEBlocks {

    private static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();
    private static final Properties QUARTZ_PROPERTIES = defaultProps(Material.STONE)
            .strength(3, 5).requiresCorrectToolForDrops();
    private static final Properties SKYSTONE_PROPERTIES = defaultProps(Material.STONE)
            .strength(5, 150).requiresCorrectToolForDrops();
    private static final StateArgumentPredicate<EntityType<?>> NEVER_ALLOW_SPAWN = (p1, p2, p3,
            p4) -> false;
    private static final Properties SKY_STONE_CHEST_PROPS = defaultProps(Material.STONE)
            .strength(5, 150).noOcclusion();

    // spotless:off
    public static final BlockDefinition<QuartzOreBlock> QUARTZ_ORE = block(AEBlockIds.QUARTZ_ORE, () -> new QuartzOreBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<ChargedQuartzOreBlock> QUARTZ_ORE_CHARGED = block(AEBlockIds.QUARTZ_ORE_CHARGED, () -> new ChargedQuartzOreBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<MatrixFrameBlock> MATRIX_FRAME = block(AEBlockIds.MATRIX_FRAME, MatrixFrameBlock::new);

    public static final BlockDefinition<AEDecorativeBlock> QUARTZ_BLOCK = block(AEBlockIds.QUARTZ_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<QuartzPillarBlock> QUARTZ_PILLAR = block(AEBlockIds.QUARTZ_PILLAR, () -> new QuartzPillarBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<AEDecorativeBlock> CHISELED_QUARTZ_BLOCK = block(AEBlockIds.CHISELED_QUARTZ_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES));

    public static final BlockDefinition<QuartzGlassBlock> QUARTZ_GLASS = block(AEBlockIds.QUARTZ_GLASS, () -> new QuartzGlassBlock(defaultProps(Material.GLASS).noOcclusion().isValidSpawn(NEVER_ALLOW_SPAWN)));
    public static final BlockDefinition<QuartzLampBlock> QUARTZ_VIBRANT_GLASS = block(AEBlockIds.QUARTZ_VIBRANT_GLASS, () -> new QuartzLampBlock(defaultProps(Material.GLASS).lightLevel(b -> 15).noOcclusion()
            .isValidSpawn(NEVER_ALLOW_SPAWN)));

    public static final BlockDefinition<QuartzFixtureBlock> QUARTZ_FIXTURE = block(AEBlockIds.QUARTZ_FIXTURE, QuartzFixtureBlock::new);
    public static final BlockDefinition<AEDecorativeBlock> FLUIX_BLOCK = block(AEBlockIds.FLUIX_BLOCK, () -> new AEDecorativeBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SkyStoneBlock> SKY_STONE_BLOCK = block(AEBlockIds.SKY_STONE_BLOCK, () -> new SkyStoneBlock(SkystoneType.STONE,
            defaultProps(Material.STONE).strength(50, 150).requiresCorrectToolForDrops()));

    public static final BlockDefinition<SkyStoneBlock> SMOOTH_SKY_STONE_BLOCK = block(AEBlockIds.SMOOTH_SKY_STONE_BLOCK, () -> new SkyStoneBlock(SkystoneType.BLOCK, SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SkyStoneBlock> SKY_STONE_BRICK = block(AEBlockIds.SKY_STONE_BRICK, () -> new SkyStoneBlock(SkystoneType.BRICK, SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SkyStoneBlock> SKY_STONE_SMALL_BRICK = block(AEBlockIds.SKY_STONE_SMALL_BRICK, () -> new SkyStoneBlock(SkystoneType.SMALL_BRICK, SKYSTONE_PROPERTIES));

    public static final BlockDefinition<SkyChestBlock> SKY_STONE_CHEST = block(AEBlockIds.SKY_STONE_CHEST, () -> new SkyChestBlock(SkyChestBlock.SkyChestType.STONE, SKY_STONE_CHEST_PROPS));
    public static final BlockDefinition<SkyChestBlock> SMOOTH_SKY_STONE_CHEST = block(AEBlockIds.SMOOTH_SKY_STONE_CHEST, () -> new SkyChestBlock(SkyChestBlock.SkyChestType.BLOCK, SKY_STONE_CHEST_PROPS));

    public static final BlockDefinition<SkyCompassBlock> SKY_COMPASS = block(AEBlockIds.SKY_COMPASS, () -> new SkyCompassBlock(defaultProps(Material.DECORATION)));
    public static final BlockDefinition<GrinderBlock> GRINDSTONE = block(AEBlockIds.GRINDSTONE, () -> new GrinderBlock(defaultProps(Material.STONE).strength(3.2f)));
    public static final BlockDefinition<CrankBlock> CRANK = block(AEBlockIds.CRANK, () -> new CrankBlock(defaultProps(Material.WOOD).noOcclusion()));
    public static final BlockDefinition<InscriberBlock> INSCRIBER = block(AEBlockIds.INSCRIBER, () -> new InscriberBlock(defaultProps(Material.METAL).noOcclusion()));
    public static final BlockDefinition<WirelessBlock> WIRELESS_ACCESS_POINT = block(AEBlockIds.WIRELESS_ACCESS_POINT, WirelessBlock::new);
    public static final BlockDefinition<ChargerBlock> CHARGER = block(AEBlockIds.CHARGER, ChargerBlock::new);

    public static final BlockDefinition<TinyTNTBlock> TINY_TNT = block(AEBlockIds.TINY_TNT, () -> new TinyTNTBlock(defaultProps(Material.EXPLOSIVE).sound(SoundType.GRAVEL).strength(0).noOcclusion()));
    public static final BlockDefinition<SecurityStationBlock> SECURITY_STATION = block(AEBlockIds.SECURITY_STATION, SecurityStationBlock::new);

    public static final BlockDefinition<QuantumRingBlock> QUANTUM_RING = block(AEBlockIds.QUANTUM_RING, QuantumRingBlock::new);
    public static final BlockDefinition<QuantumLinkChamberBlock> QUANTUM_LINK = block(AEBlockIds.QUANTUM_LINK, QuantumLinkChamberBlock::new);
    public static final BlockDefinition<SpatialPylonBlock> SPATIAL_PYLON = block(AEBlockIds.SPATIAL_PYLON, SpatialPylonBlock::new);
    public static final BlockDefinition<SpatialIOPortBlock> SPATIAL_IO_PORT = block(AEBlockIds.SPATIAL_IO_PORT, SpatialIOPortBlock::new);
    public static final BlockDefinition<ControllerBlock> CONTROLLER = block(AEBlockIds.CONTROLLER, ControllerBlock::new);
    public static final BlockDefinition<DriveBlock> DRIVE = block(AEBlockIds.DRIVE, DriveBlock::new);
    public static final BlockDefinition<ChestBlock> CHEST = block(AEBlockIds.CHEST, ChestBlock::new);
    public static final BlockDefinition<ItemInterfaceBlock> ITEM_INTERFACE = block(AEBlockIds.ITEM_INTERFACE, ItemInterfaceBlock::new);
    public static final BlockDefinition<FluidInterfaceBlock> FLUID_INTERFACE = block(AEBlockIds.FLUID_INTERFACE, FluidInterfaceBlock::new);
    public static final BlockDefinition<CellWorkbenchBlock> CELL_WORKBENCH = block(AEBlockIds.CELL_WORKBENCH, CellWorkbenchBlock::new);
    public static final BlockDefinition<IOPortBlock> IO_PORT = block(AEBlockIds.IO_PORT, IOPortBlock::new);
    public static final BlockDefinition<CondenserBlock> CONDENSER = block(AEBlockIds.CONDENSER, CondenserBlock::new);
    public static final BlockDefinition<EnergyAcceptorBlock> ENERGY_ACCEPTOR = block(AEBlockIds.ENERGY_ACCEPTOR, EnergyAcceptorBlock::new);
    public static final BlockDefinition<VibrationChamberBlock> VIBRATION_CHAMBER = block(AEBlockIds.VIBRATION_CHAMBER, VibrationChamberBlock::new);
    public static final BlockDefinition<QuartzGrowthAcceleratorBlock> QUARTZ_GROWTH_ACCELERATOR = block(AEBlockIds.QUARTZ_GROWTH_ACCELERATOR, QuartzGrowthAcceleratorBlock::new);
    public static final BlockDefinition<EnergyCellBlock> ENERGY_CELL = block(AEBlockIds.ENERGY_CELL, EnergyCellBlock::new, AEBaseBlockItemChargeable::new);
    public static final BlockDefinition<DenseEnergyCellBlock> DENSE_ENERGY_CELL = block(AEBlockIds.DENSE_ENERGY_CELL, DenseEnergyCellBlock::new, AEBaseBlockItemChargeable::new);
    public static final BlockDefinition<CreativeEnergyCellBlock> CREATIVE_ENERGY_CELL = block(AEBlockIds.CREATIVE_ENERGY_CELL, CreativeEnergyCellBlock::new);

    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_UNIT = block(AEBlockIds.CRAFTING_UNIT, () -> new CraftingUnitBlock(defaultProps(Material.METAL), CraftingUnitType.UNIT));
    public static final BlockDefinition<CraftingUnitBlock> CRAFTING_ACCELERATOR = block(AEBlockIds.CRAFTING_ACCELERATOR, () -> new CraftingUnitBlock(defaultProps(Material.METAL), CraftingUnitType.ACCELERATOR));

    public static final BlockDefinition<CraftingStorageBlock> CRAFTING_STORAGE_1K = block(AEBlockIds.CRAFTING_STORAGE_1K, () -> new CraftingStorageBlock(defaultProps(Material.METAL), CraftingUnitType.STORAGE_1K), CraftingStorageItem::new);
    public static final BlockDefinition<CraftingStorageBlock> CRAFTING_STORAGE_4K = block(AEBlockIds.CRAFTING_STORAGE_4K, () -> new CraftingStorageBlock(defaultProps(Material.METAL), CraftingUnitType.STORAGE_4K), CraftingStorageItem::new);
    public static final BlockDefinition<CraftingStorageBlock> CRAFTING_STORAGE_16K = block(AEBlockIds.CRAFTING_STORAGE_16K, () -> new CraftingStorageBlock(defaultProps(Material.METAL), CraftingUnitType.STORAGE_16K), CraftingStorageItem::new);
    public static final BlockDefinition<CraftingStorageBlock> CRAFTING_STORAGE_64K = block(AEBlockIds.CRAFTING_STORAGE_64K, () -> new CraftingStorageBlock(defaultProps(Material.METAL), CraftingUnitType.STORAGE_64K), CraftingStorageItem::new);
    public static final BlockDefinition<CraftingMonitorBlock> CRAFTING_MONITOR = block(AEBlockIds.CRAFTING_MONITOR, () -> new CraftingMonitorBlock(defaultProps(Material.METAL)));

    public static final BlockDefinition<PatternProviderBlock> PATTERN_PROVIDER = block(AEBlockIds.PATTERN_PROVIDER, PatternProviderBlock::new);
    public static final BlockDefinition<MolecularAssemblerBlock> MOLECULAR_ASSEMBLER = block(AEBlockIds.MOLECULAR_ASSEMBLER, () -> new MolecularAssemblerBlock(defaultProps(Material.METAL).noOcclusion()));

    public static final BlockDefinition<LightDetectorBlock> LIGHT_DETECTOR = block(AEBlockIds.LIGHT_DETECTOR, LightDetectorBlock::new);
    public static final BlockDefinition<PaintSplotchesBlock> PAINT = block(AEBlockIds.PAINT, PaintSplotchesBlock::new);

    public static final BlockDefinition<StairBlock> SKY_STONE_STAIRS = block(AEBlockIds.SKY_STONE_STAIRS, () -> new StairBlock(SKY_STONE_BLOCK.block().defaultBlockState(), SKYSTONE_PROPERTIES));
    public static final BlockDefinition<StairBlock> SMOOTH_SKY_STONE_STAIRS = block(AEBlockIds.SMOOTH_SKY_STONE_STAIRS, () -> new StairBlock(SMOOTH_SKY_STONE_BLOCK.block().defaultBlockState(), SKYSTONE_PROPERTIES));
    public static final BlockDefinition<StairBlock> SKY_STONE_BRICK_STAIRS = block(AEBlockIds.SKY_STONE_BRICK_STAIRS, () -> new StairBlock(SKY_STONE_BRICK.block().defaultBlockState(), SKYSTONE_PROPERTIES));
    public static final BlockDefinition<StairBlock> SKY_STONE_SMALL_BRICK_STAIRS = block(AEBlockIds.SKY_STONE_SMALL_BRICK_STAIRS, () -> new StairBlock(SKY_STONE_SMALL_BRICK.block().defaultBlockState(), SKYSTONE_PROPERTIES));
    public static final BlockDefinition<StairBlock> FLUIX_STAIRS = block(AEBlockIds.FLUIX_STAIRS, () -> new StairBlock(FLUIX_BLOCK.block().defaultBlockState(), QUARTZ_PROPERTIES));
    public static final BlockDefinition<StairBlock> QUARTZ_STAIRS = block(AEBlockIds.QUARTZ_STAIRS, () -> new StairBlock(QUARTZ_BLOCK.block().defaultBlockState(), QUARTZ_PROPERTIES));
    public static final BlockDefinition<StairBlock> CHISELED_QUARTZ_STAIRS = block(AEBlockIds.CHISELED_QUARTZ_STAIRS, () -> new StairBlock(CHISELED_QUARTZ_BLOCK.block().defaultBlockState(), QUARTZ_PROPERTIES));
    public static final BlockDefinition<StairBlock> QUARTZ_PILLAR_STAIRS = block(AEBlockIds.QUARTZ_PILLAR_STAIRS, () -> new StairBlock(QUARTZ_PILLAR.block().defaultBlockState(), QUARTZ_PROPERTIES));

    public static final BlockDefinition<WallBlock> SKY_STONE_WALL = block(AEBlockIds.SKY_STONE_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<WallBlock> SMOOTH_SKY_STONE_WALL = block(AEBlockIds.SMOOTH_SKY_STONE_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<WallBlock> SKY_STONE_BRICK_WALL = block(AEBlockIds.SKY_STONE_BRICK_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<WallBlock> SKY_STONE_SMALL_BRICK_WALL = block(AEBlockIds.SKY_STONE_SMALL_BRICK_WALL, () -> new WallBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<WallBlock> FLUIX_WALL = block(AEBlockIds.FLUIX_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<WallBlock> QUARTZ_WALL = block(AEBlockIds.QUARTZ_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<WallBlock> CHISELED_QUARTZ_WALL = block(AEBlockIds.CHISELED_QUARTZ_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<WallBlock> QUARTZ_PILLAR_WALL = block(AEBlockIds.QUARTZ_PILLAR_WALL, () -> new WallBlock(QUARTZ_PROPERTIES));

    public static final BlockDefinition<CableBusBlock> MULTI_PART = block(AEBlockIds.CABLE_BUS, CableBusBlock::new);

    public static final BlockDefinition<SlabBlock> SKY_STONE_SLAB = block(AEBlockIds.SKY_STONE_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SlabBlock> SMOOTH_SKY_STONE_SLAB = block(AEBlockIds.SMOOTH_SKY_STONE_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SlabBlock> SKY_STONE_BRICK_SLAB = block(AEBlockIds.SKY_STONE_BRICK_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES));
    public static final BlockDefinition<SlabBlock> SKY_STONE_SMALL_BRICK_SLAB = block(AEBlockIds.SKY_STONE_SMALL_BRICK_SLAB, () -> new SlabBlock(SKYSTONE_PROPERTIES));

    public static final BlockDefinition<SlabBlock> FLUIX_SLAB = block(AEBlockIds.FLUIX_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SlabBlock> QUARTZ_SLAB = block(AEBlockIds.QUARTZ_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SlabBlock> CHISELED_QUARTZ_SLAB = block(AEBlockIds.CHISELED_QUARTZ_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));
    public static final BlockDefinition<SlabBlock> QUARTZ_PILLAR_SLAB = block(AEBlockIds.QUARTZ_PILLAR_SLAB, () -> new SlabBlock(QUARTZ_PROPERTIES));

    public static final BlockDefinition<SpatialAnchorBlock> SPATIAL_ANCHOR = block(AEBlockIds.SPATIAL_ANCHOR, SpatialAnchorBlock::new);

    ///
    /// DEBUG BLOCKS
    ///
    public static final BlockDefinition<ItemGenBlock> DEBUG_ITEM_GEN = block(AppEng.makeId("debug_item_gen"), ItemGenBlock::new);
    public static final BlockDefinition<ChunkLoaderBlock> DEBUG_CHUNK_LOADER = block(AppEng.makeId("debug_chunk_loader"), ChunkLoaderBlock::new);
    public static final BlockDefinition<PhantomNodeBlock> DEBUG_PHANTOM_NODE = block(AppEng.makeId("debug_phantom_node"), PhantomNodeBlock::new);
    public static final BlockDefinition<CubeGeneratorBlock> DEBUG_CUBE_GEN = block(AppEng.makeId("debug_cube_gen"), CubeGeneratorBlock::new);
    public static final BlockDefinition<EnergyGeneratorBlock> DEBUG_ENERGY_GEN = block(AppEng.makeId("debug_energy_gen"), EnergyGeneratorBlock::new);
    // spotless:on

    public static List<BlockDefinition<?>> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    private static <T extends Block> BlockDefinition<T> block(ResourceLocation id, Supplier<T> blockSupplier) {
        return block(id, blockSupplier, null);
    }

    private static <T extends Block> BlockDefinition<T> block(ResourceLocation id,
            Supplier<T> blockSupplier,
            @Nullable BiFunction<Block, Item.Properties, BlockItem> itemFactory) {

        // Create block and matching item, and set factory name of both
        T block = blockSupplier.get();

        Item.Properties itemProperties = new Item.Properties();
        itemProperties.tab(CreativeTab.INSTANCE);

        BlockItem item;
        if (itemFactory != null) {
            item = itemFactory.apply(block, itemProperties);
            if (item == null) {
                throw new IllegalArgumentException("BlockItem factory for " + id + " returned null");
            }
        } else if (block instanceof AEBaseBlock) {
            item = new AEBaseBlockItem(block, itemProperties);
        } else {
            item = new BlockItem(block, itemProperties);
        }

        BlockDefinition<T> definition = new BlockDefinition<>(id, block, item);
        CreativeTab.add(definition);

        BLOCKS.add(definition);

        return definition;

    }

    // Used to control in which order static constructors are called
    public static void init() {
    }

}
