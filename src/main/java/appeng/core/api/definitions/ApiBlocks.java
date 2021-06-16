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

package appeng.core.api.definitions;

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
import appeng.core.features.BlockDefinition;
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
public final class ApiBlocks {

    public static final BlockDefinition quartzOre;
    public static final BlockDefinition quartzOreCharged;
    public static final BlockDefinition matrixFrame;
    public static final BlockDefinition quartzBlock;
    public static final BlockDefinition quartzPillar;
    public static final BlockDefinition chiseledQuartzBlock;
    public static final BlockDefinition quartzGlass;
    public static final BlockDefinition quartzVibrantGlass;
    public static final BlockDefinition quartzFixture;
    public static final BlockDefinition fluixBlock;
    public static final BlockDefinition skyStoneBlock;
    public static final BlockDefinition smoothSkyStoneBlock;
    public static final BlockDefinition skyStoneBrick;
    public static final BlockDefinition skyStoneSmallBrick;
    public static final BlockDefinition skyStoneChest;
    public static final BlockDefinition smoothSkyStoneChest;
    public static final BlockDefinition skyCompass;
    public static final BlockDefinition grindstone;
    public static final BlockDefinition crank;
    public static final BlockDefinition inscriber;
    public static final BlockDefinition wirelessAccessPoint;
    public static final BlockDefinition charger;
    public static final BlockDefinition tinyTNT;
    public static final BlockDefinition securityStation;
    public static final BlockDefinition quantumRing;
    public static final BlockDefinition quantumLink;
    public static final BlockDefinition spatialPylon;
    public static final BlockDefinition spatialIOPort;
    public static final BlockDefinition spatialAnchor;
    public static final BlockDefinition multiPart;
    public static final BlockDefinition controller;
    public static final BlockDefinition drive;
    public static final BlockDefinition chest;
    public static final BlockDefinition iface;
    public static final BlockDefinition fluidIface;
    public static final BlockDefinition cellWorkbench;
    public static final BlockDefinition iOPort;
    public static final BlockDefinition condenser;
    public static final BlockDefinition energyAcceptor;
    public static final BlockDefinition vibrationChamber;
    public static final BlockDefinition quartzGrowthAccelerator;
    public static final BlockDefinition energyCell;
    public static final BlockDefinition energyCellDense;
    public static final BlockDefinition energyCellCreative;
    public static final BlockDefinition craftingUnit;
    public static final BlockDefinition craftingAccelerator;
    public static final BlockDefinition craftingStorage1k;
    public static final BlockDefinition craftingStorage4k;
    public static final BlockDefinition craftingStorage16k;
    public static final BlockDefinition craftingStorage64k;
    public static final BlockDefinition craftingMonitor;
    public static final BlockDefinition molecularAssembler;
    public static final BlockDefinition lightDetector;
    public static final BlockDefinition paint;
    public static final BlockDefinition skyStoneStairs;
    public static final BlockDefinition smoothSkyStoneStairs;
    public static final BlockDefinition skyStoneBrickStairs;
    public static final BlockDefinition skyStoneSmallBrickStairs;
    public static final BlockDefinition fluixStairs;
    public static final BlockDefinition quartzStairs;
    public static final BlockDefinition chiseledQuartzStairs;
    public static final BlockDefinition quartzPillarStairs;
    public static final BlockDefinition skyStoneWall;
    public static final BlockDefinition smoothSkyStoneWall;
    public static final BlockDefinition skyStoneBrickWall;
    public static final BlockDefinition skyStoneSmallBrickWall;
    public static final BlockDefinition fluixWall;
    public static final BlockDefinition quartzWall;
    public static final BlockDefinition chiseledQuartzWall;
    public static final BlockDefinition quartzPillarWall;
    public static final BlockDefinition skyStoneSlab;
    public static final BlockDefinition smoothSkyStoneSlab;
    public static final BlockDefinition skyStoneBrickSlab;
    public static final BlockDefinition skyStoneSmallBrickSlab;
    public static final BlockDefinition fluixSlab;
    public static final BlockDefinition quartzSlab;
    public static final BlockDefinition chiseledQuartzSlab;
    public static final BlockDefinition quartzPillarSlab;
    public static final BlockDefinition itemGen;
    public static final BlockDefinition chunkLoader;
    public static final BlockDefinition phantomNode;
    public static final BlockDefinition cubeGenerator;
    public static final BlockDefinition energyGenerator;
    private static final List<BlockDefinition> BLOCKS = new ArrayList<>();
    private static final AbstractBlock.Properties QUARTZ_PROPERTIES = defaultProps(Material.ROCK)
            .hardnessAndResistance(3, 5).setRequiresTool().harvestLevel(1);
    private static final AbstractBlock.Properties SKYSTONE_PROPERTIES = defaultProps(Material.ROCK)
            .hardnessAndResistance(50, 150).setRequiresTool();

    static {
        quartzOre = block("quartz_ore", () -> new QuartzOreBlock(QUARTZ_PROPERTIES))
                .build();
        quartzOreCharged = block("charged_quartz_ore", () -> new ChargedQuartzOreBlock(QUARTZ_PROPERTIES))
                .build();
        matrixFrame = block("matrix_frame", MatrixFrameBlock::new).build();

        quartzBlock = decoBlock("quartz_block", () -> new AEDecorativeBlock(QUARTZ_PROPERTIES)).build();
        quartzPillar = decoBlock("quartz_pillar", () -> new QuartzPillarBlock(QUARTZ_PROPERTIES)).build();
        chiseledQuartzBlock = decoBlock("chiseled_quartz_block", () -> new AEDecorativeBlock(QUARTZ_PROPERTIES))
                .build();

        AbstractBlock.IExtendedPositionPredicate<EntityType<?>> neverAllowSpawn = (p1, p2, p3, p4) -> false;
        quartzGlass = block("quartz_glass",
                () -> new QuartzGlassBlock(defaultProps(Material.GLASS).notSolid().setAllowsSpawn(neverAllowSpawn)))

                        .build();
        quartzVibrantGlass = decoBlock("quartz_vibrant_glass",
                () -> new QuartzLampBlock(defaultProps(Material.GLASS).setLightLevel(b -> 15).notSolid()
                        .setAllowsSpawn(neverAllowSpawn)))
                                .build();

        quartzFixture = block("quartz_fixture", QuartzFixtureBlock::new)
                .build();

        fluixBlock = block("fluix_block", () -> new AEDecorativeBlock(QUARTZ_PROPERTIES))

                .build();

        skyStoneBlock = block("sky_stone_block", () -> new SkyStoneBlock(SkystoneType.STONE,
                defaultProps(Material.ROCK).hardnessAndResistance(50, 150).setRequiresTool().harvestLevel(3)))

                        .build();

        smoothSkyStoneBlock = block("smooth_sky_stone_block",
                () -> new SkyStoneBlock(SkystoneType.BLOCK, SKYSTONE_PROPERTIES))

                        .build();
        skyStoneBrick = decoBlock("sky_stone_brick", () -> new SkyStoneBlock(SkystoneType.BRICK, SKYSTONE_PROPERTIES))
                .build();
        skyStoneSmallBrick = decoBlock("sky_stone_small_brick",
                () -> new SkyStoneBlock(SkystoneType.SMALL_BRICK, SKYSTONE_PROPERTIES)).build();

        AbstractBlock.Properties skyStoneChestProps = defaultProps(Material.ROCK).hardnessAndResistance(50, 150)
                .notSolid();

        skyStoneChest = block("sky_stone_chest",
                () -> new SkyChestBlock(SkyChestBlock.SkyChestType.STONE, skyStoneChestProps))
                        .build();
        smoothSkyStoneChest = block("smooth_sky_stone_chest",
                () -> new SkyChestBlock(SkyChestBlock.SkyChestType.BLOCK, skyStoneChestProps))
                        .build();

        skyCompass = block("sky_compass", () -> new SkyCompassBlock(defaultProps(Material.MISCELLANEOUS)))

                .build();
        grindstone = block("grindstone",
                () -> new GrinderBlock(defaultProps(Material.ROCK).hardnessAndResistance(3.2f)))

                        .build();
        crank = block("crank",
                () -> new CrankBlock(
                        defaultProps(Material.WOOD).harvestTool(ToolType.AXE).harvestLevel(0).notSolid()))

                                .build();
        inscriber = block("inscriber", () -> new InscriberBlock(defaultProps(Material.IRON).notSolid()))

                .build();
        wirelessAccessPoint = block("wireless_access_point", WirelessBlock::new)

                .build();
        charger = block("charger", ChargerBlock::new)
                .build();

        tinyTNT = block("tiny_tnt",
                () -> new TinyTNTBlock(
                        defaultProps(Material.TNT).sound(SoundType.GROUND).hardnessAndResistance(0).notSolid()))

                                .build();
        securityStation = block("security_station", SecurityStationBlock::new)

                .build();

        quantumRing = block("quantum_ring", QuantumRingBlock::new)

                .build();
        quantumLink = block("quantum_link", QuantumLinkChamberBlock::new)

                .build();
        spatialPylon = block("spatial_pylon", SpatialPylonBlock::new)
                .build();
        spatialIOPort = block("spatial_io_port", SpatialIOPortBlock::new)
                .build();
        controller = block("controller", ControllerBlock::new)
                .build();
        drive = block("drive", DriveBlock::new)
                .build();
        chest = block("chest", ChestBlock::new)
                .build();
        iface = block("interface", InterfaceBlock::new)
                .build();
        fluidIface = block("fluid_interface", FluidInterfaceBlock::new)

                .build();
        cellWorkbench = block("cell_workbench", CellWorkbenchBlock::new)
                .build();
        iOPort = block("io_port", IOPortBlock::new)
                .build();
        condenser = block("condenser", CondenserBlock::new)
                .build();
        energyAcceptor = block("energy_acceptor", EnergyAcceptorBlock::new)

                .build();
        vibrationChamber = block("vibration_chamber", VibrationChamberBlock::new)

                .build();
        quartzGrowthAccelerator = block("quartz_growth_accelerator", QuartzGrowthAcceleratorBlock::new)
                .build();
        energyCell = block("energy_cell", EnergyCellBlock::new)
                .item(AEBaseBlockItemChargeable::new)
                .build();
        energyCellDense = block("dense_energy_cell", DenseEnergyCellBlock::new)
                .item(AEBaseBlockItemChargeable::new)
                .build();
        energyCellCreative = block("creative_energy_cell", CreativeEnergyCellBlock::new)

                .build();

        AbstractBlock.Properties craftingBlockProps = defaultProps(Material.IRON);
        craftingUnit = craftingBlock("crafting_unit",
                () -> new CraftingUnitBlock(craftingBlockProps, CraftingUnitType.UNIT))
                        .build();
        craftingAccelerator = craftingBlock("crafting_accelerator",
                () -> new CraftingUnitBlock(craftingBlockProps, CraftingUnitType.ACCELERATOR))
                        .build();

        craftingStorage1k = craftingBlock("1k_crafting_storage",
                () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_1K))
                        .item(CraftingStorageItem::new)
                        .build();
        craftingStorage4k = craftingBlock("4k_crafting_storage",
                () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_4K))
                        .item(CraftingStorageItem::new)
                        .build();
        craftingStorage16k = craftingBlock("16k_crafting_storage",
                () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_16K))
                        .item(CraftingStorageItem::new)
                        .build();
        craftingStorage64k = craftingBlock("64k_crafting_storage",
                () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_64K))
                        .item(CraftingStorageItem::new)
                        .build();
        craftingMonitor = craftingBlock("crafting_monitor", () -> new CraftingMonitorBlock(craftingBlockProps))
                .build();

        molecularAssembler = block("molecular_assembler",
                () -> new MolecularAssemblerBlock(defaultProps(Material.IRON).notSolid()))

                        .build();

        lightDetector = block("light_detector", LightDetectorBlock::new)

                .build();
        paint = block("paint", PaintSplotchesBlock::new)
                .build();

        skyStoneStairs = decoBlock("sky_stone_stairs",
                () -> new StairsBlock(skyStoneBlock.block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
        smoothSkyStoneStairs = decoBlock("smooth_sky_stone_stairs",
                () -> new StairsBlock(smoothSkyStoneBlock.block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
        skyStoneBrickStairs = decoBlock("sky_stone_brick_stairs",
                () -> new StairsBlock(skyStoneBrick.block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
        skyStoneSmallBrickStairs = decoBlock("sky_stone_small_brick_stairs",
                () -> new StairsBlock(skyStoneSmallBrick.block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
        fluixStairs = decoBlock("fluix_stairs",
                () -> new StairsBlock(fluixBlock.block()::getDefaultState, QUARTZ_PROPERTIES)).build();
        quartzStairs = decoBlock("quartz_stairs",
                () -> new StairsBlock(quartzBlock.block()::getDefaultState, QUARTZ_PROPERTIES)).build();
        chiseledQuartzStairs = decoBlock("chiseled_quartz_stairs",
                () -> new StairsBlock(chiseledQuartzBlock.block()::getDefaultState, QUARTZ_PROPERTIES)).build();
        quartzPillarStairs = decoBlock("quartz_pillar_stairs",
                () -> new StairsBlock(quartzPillar.block()::getDefaultState, QUARTZ_PROPERTIES)).build();

        skyStoneWall = decoBlock("sky_stone_wall",
                () -> new WallBlock(SKYSTONE_PROPERTIES)).build();
        smoothSkyStoneWall = decoBlock("smooth_sky_stone_wall",
                () -> new WallBlock(SKYSTONE_PROPERTIES)).build();
        skyStoneBrickWall = decoBlock("sky_stone_brick_wall",
                () -> new WallBlock(SKYSTONE_PROPERTIES)).build();
        skyStoneSmallBrickWall = decoBlock("sky_stone_small_brick_wall",
                () -> new WallBlock(SKYSTONE_PROPERTIES)).build();
        fluixWall = decoBlock("fluix_wall",
                () -> new WallBlock(QUARTZ_PROPERTIES)).build();
        quartzWall = decoBlock("quartz_wall",
                () -> new WallBlock(QUARTZ_PROPERTIES)).build();
        chiseledQuartzWall = decoBlock("chiseled_quartz_wall",
                () -> new WallBlock(QUARTZ_PROPERTIES)).build();
        quartzPillarWall = decoBlock("quartz_pillar_wall",
                () -> new WallBlock(QUARTZ_PROPERTIES)).build();

        multiPart = block("cable_bus", CableBusBlock::new)
                .build();

        skyStoneSlab = decoBlock("sky_stone_slab", () -> new SlabBlock(SKYSTONE_PROPERTIES)).build();
        smoothSkyStoneSlab = decoBlock("smooth_sky_stone_slab", () -> new SlabBlock(SKYSTONE_PROPERTIES)).build();
        skyStoneBrickSlab = decoBlock("sky_stone_brick_slab", () -> new SlabBlock(SKYSTONE_PROPERTIES)).build();
        skyStoneSmallBrickSlab = decoBlock("sky_stone_small_brick_slab", () -> new SlabBlock(SKYSTONE_PROPERTIES))
                .build();

        fluixSlab = decoBlock("fluix_slab", () -> new SlabBlock(QUARTZ_PROPERTIES)).build();
        quartzSlab = decoBlock("quartz_slab", () -> new SlabBlock(QUARTZ_PROPERTIES)).build();
        chiseledQuartzSlab = decoBlock("chiseled_quartz_slab", () -> new SlabBlock(QUARTZ_PROPERTIES)).build();
        quartzPillarSlab = decoBlock("quartz_pillar_slab", () -> new SlabBlock(QUARTZ_PROPERTIES)).build();

        spatialAnchor = block("spatial_anchor", SpatialAnchorBlock::new)

                .build();

        // Debug blocks
        itemGen = block("debug_item_gen", ItemGenBlock::new)

                .build();
        chunkLoader = block("debug_chunk_loader", ChunkLoaderBlock::new)

                .build();
        phantomNode = block("debug_phantom_node", PhantomNodeBlock::new)

                .build();
        cubeGenerator = block("debug_cube_gen", CubeGeneratorBlock::new)

                .build();
        energyGenerator = block("debug_energy_gen", EnergyGeneratorBlock::new)

                .build();
    }

    public static List<BlockDefinition> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    private static Builder block(ResourceLocation id, Supplier<Block> blockSupplier) {
        return new Builder(id, blockSupplier);
    }

    private static Builder block(String id, Supplier<Block> blockSupplier) {
        return new Builder(AppEng.makeId(id), blockSupplier);
    }

    private static Builder decoBlock(String id, Supplier<Block> blockSupplier) {
        return new Builder(AppEng.makeId(id), blockSupplier);
    }

    private static Builder craftingBlock(String id, Supplier<Block> blockSupplier) {
        return new Builder(AppEng.makeId(id), blockSupplier);
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
            // FIXME: Allow more/all item properties

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
