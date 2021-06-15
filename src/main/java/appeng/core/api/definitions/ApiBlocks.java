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

import appeng.api.definitions.IBlockDefinition;
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

    private static final List<IBlockDefinition> BLOCKS = new ArrayList<>();

    private static final IBlockDefinition quartzOre;
    private static final IBlockDefinition quartzOreCharged;
    private static final IBlockDefinition matrixFrame;
    private static final IBlockDefinition quartzBlock;
    private static final IBlockDefinition quartzPillar;
    private static final IBlockDefinition chiseledQuartzBlock;
    private static final IBlockDefinition quartzGlass;
    private static final IBlockDefinition quartzVibrantGlass;
    private static final IBlockDefinition quartzFixture;
    private static final IBlockDefinition fluixBlock;
    private static final IBlockDefinition skyStoneBlock;
    private static final IBlockDefinition smoothSkyStoneBlock;
    private static final IBlockDefinition skyStoneBrick;
    private static final IBlockDefinition skyStoneSmallBrick;
    private static final IBlockDefinition skyStoneChest;
    private static final IBlockDefinition smoothSkyStoneChest;
    private static final IBlockDefinition skyCompass;
    private static final IBlockDefinition grindstone;
    private static final IBlockDefinition crank;
    private static final IBlockDefinition inscriber;
    private static final IBlockDefinition wirelessAccessPoint;
    private static final IBlockDefinition charger;
    private static final IBlockDefinition tinyTNT;
    private static final IBlockDefinition securityStation;
    private static final IBlockDefinition quantumRing;
    private static final IBlockDefinition quantumLink;
    private static final IBlockDefinition spatialPylon;
    private static final IBlockDefinition spatialIOPort;
    private static final IBlockDefinition spatialAnchor;
    private static final IBlockDefinition multiPart;
    private static final IBlockDefinition controller;
    private static final IBlockDefinition drive;
    private static final IBlockDefinition chest;
    private static final IBlockDefinition iface;
    private static final IBlockDefinition fluidIface;
    private static final IBlockDefinition cellWorkbench;
    private static final IBlockDefinition iOPort;
    private static final IBlockDefinition condenser;
    private static final IBlockDefinition energyAcceptor;
    private static final IBlockDefinition vibrationChamber;
    private static final IBlockDefinition quartzGrowthAccelerator;
    private static final IBlockDefinition energyCell;
    private static final IBlockDefinition energyCellDense;
    private static final IBlockDefinition energyCellCreative;
    private static final IBlockDefinition craftingUnit;
    private static final IBlockDefinition craftingAccelerator;
    private static final IBlockDefinition craftingStorage1k;
    private static final IBlockDefinition craftingStorage4k;
    private static final IBlockDefinition craftingStorage16k;
    private static final IBlockDefinition craftingStorage64k;
    private static final IBlockDefinition craftingMonitor;
    private static final IBlockDefinition molecularAssembler;
    private static final IBlockDefinition lightDetector;
    private static final IBlockDefinition paint;

    private static final IBlockDefinition skyStoneStairs;
    private static final IBlockDefinition smoothSkyStoneStairs;
    private static final IBlockDefinition skyStoneBrickStairs;
    private static final IBlockDefinition skyStoneSmallBrickStairs;
    private static final IBlockDefinition fluixStairs;
    private static final IBlockDefinition quartzStairs;
    private static final IBlockDefinition chiseledQuartzStairs;
    private static final IBlockDefinition quartzPillarStairs;

    private static final IBlockDefinition skyStoneWall;
    private static final IBlockDefinition smoothSkyStoneWall;
    private static final IBlockDefinition skyStoneBrickWall;
    private static final IBlockDefinition skyStoneSmallBrickWall;
    private static final IBlockDefinition fluixWall;
    private static final IBlockDefinition quartzWall;
    private static final IBlockDefinition chiseledQuartzWall;
    private static final IBlockDefinition quartzPillarWall;

    private static final IBlockDefinition skyStoneSlab;
    private static final IBlockDefinition smoothSkyStoneSlab;
    private static final IBlockDefinition skyStoneBrickSlab;
    private static final IBlockDefinition skyStoneSmallBrickSlab;
    private static final IBlockDefinition fluixSlab;
    private static final IBlockDefinition quartzSlab;
    private static final IBlockDefinition chiseledQuartzSlab;
    private static final IBlockDefinition quartzPillarSlab;

    private static final IBlockDefinition itemGen;
    private static final IBlockDefinition chunkLoader;
    private static final IBlockDefinition phantomNode;
    private static final IBlockDefinition cubeGenerator;
    private static final IBlockDefinition energyGenerator;

    public static List<IBlockDefinition> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

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
                () -> new StairsBlock(skyStoneBlock().block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
        smoothSkyStoneStairs = decoBlock("smooth_sky_stone_stairs",
                () -> new StairsBlock(smoothSkyStoneBlock().block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
        skyStoneBrickStairs = decoBlock("sky_stone_brick_stairs",
                () -> new StairsBlock(skyStoneBrick().block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
        skyStoneSmallBrickStairs = decoBlock("sky_stone_small_brick_stairs",
                () -> new StairsBlock(skyStoneSmallBrick().block()::getDefaultState, SKYSTONE_PROPERTIES)).build();
        fluixStairs = decoBlock("fluix_stairs",
                () -> new StairsBlock(fluixBlock().block()::getDefaultState, QUARTZ_PROPERTIES)).build();
        quartzStairs = decoBlock("quartz_stairs",
                () -> new StairsBlock(quartzBlock().block()::getDefaultState, QUARTZ_PROPERTIES)).build();
        chiseledQuartzStairs = decoBlock("chiseled_quartz_stairs",
                () -> new StairsBlock(chiseledQuartzBlock().block()::getDefaultState, QUARTZ_PROPERTIES)).build();
        quartzPillarStairs = decoBlock("quartz_pillar_stairs",
                () -> new StairsBlock(quartzPillar().block()::getDefaultState, QUARTZ_PROPERTIES)).build();

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

    public static IBlockDefinition quartzOre() {
        return quartzOre;
    }

    public static IBlockDefinition quartzOreCharged() {
        return quartzOreCharged;
    }

    public static IBlockDefinition matrixFrame() {
        return matrixFrame;
    }

    public static IBlockDefinition quartzBlock() {
        return quartzBlock;
    }

    public static IBlockDefinition quartzPillar() {
        return quartzPillar;
    }

    public static IBlockDefinition chiseledQuartzBlock() {
        return chiseledQuartzBlock;
    }

    public static IBlockDefinition quartzGlass() {
        return quartzGlass;
    }

    public static IBlockDefinition quartzVibrantGlass() {
        return quartzVibrantGlass;
    }

    public static IBlockDefinition quartzFixture() {
        return quartzFixture;
    }

    public static IBlockDefinition fluixBlock() {
        return fluixBlock;
    }

    public static IBlockDefinition skyStoneBlock() {
        return skyStoneBlock;
    }

    public static IBlockDefinition smoothSkyStoneBlock() {
        return smoothSkyStoneBlock;
    }

    public static IBlockDefinition skyStoneBrick() {
        return skyStoneBrick;
    }

    public static IBlockDefinition skyStoneSmallBrick() {
        return skyStoneSmallBrick;
    }

    public static IBlockDefinition skyStoneChest() {
        return skyStoneChest;
    }

    public static IBlockDefinition smoothSkyStoneChest() {
        return smoothSkyStoneChest;
    }

    public static IBlockDefinition skyCompass() {
        return skyCompass;
    }

    public static IBlockDefinition skyStoneStairs() {
        return skyStoneStairs;
    }

    public static IBlockDefinition smoothSkyStoneStairs() {
        return smoothSkyStoneStairs;
    }

    public static IBlockDefinition skyStoneBrickStairs() {
        return skyStoneBrickStairs;
    }

    public static IBlockDefinition skyStoneSmallBrickStairs() {
        return skyStoneSmallBrickStairs;
    }

    public static IBlockDefinition fluixStairs() {
        return fluixStairs;
    }

    public static IBlockDefinition quartzStairs() {
        return quartzStairs;
    }

    public static IBlockDefinition chiseledQuartzStairs() {
        return chiseledQuartzStairs;
    }

    public static IBlockDefinition quartzPillarStairs() {
        return quartzPillarStairs;
    }

    public static IBlockDefinition skyStoneWall() {
        return skyStoneWall;
    }

    public static IBlockDefinition smoothSkyStoneWall() {
        return smoothSkyStoneWall;
    }

    public static IBlockDefinition skyStoneBrickWall() {
        return skyStoneBrickWall;
    }

    public static IBlockDefinition skyStoneSmallBrickWall() {
        return skyStoneSmallBrickWall;
    }

    public static IBlockDefinition fluixWall() {
        return fluixWall;
    }

    public static IBlockDefinition quartzWall() {
        return quartzWall;
    }

    public static IBlockDefinition chiseledQuartzWall() {
        return chiseledQuartzWall;
    }

    public static IBlockDefinition quartzPillarWall() {
        return quartzPillarWall;
    }

    public static IBlockDefinition skyStoneSlab() {
        return skyStoneSlab;
    }

    public static IBlockDefinition smoothSkyStoneSlab() {
        return smoothSkyStoneSlab;
    }

    public static IBlockDefinition skyStoneBrickSlab() {
        return skyStoneBrickSlab;
    }

    public static IBlockDefinition skyStoneSmallBrickSlab() {
        return skyStoneSmallBrickSlab;
    }

    public static IBlockDefinition fluixSlab() {
        return fluixSlab;
    }

    public static IBlockDefinition quartzSlab() {
        return quartzSlab;
    }

    public static IBlockDefinition chiseledQuartzSlab() {
        return chiseledQuartzSlab;
    }

    public static IBlockDefinition quartzPillarSlab() {
        return quartzPillarSlab;
    }

    public static IBlockDefinition grindstone() {
        return grindstone;
    }

    public static IBlockDefinition crank() {
        return crank;
    }

    public static IBlockDefinition inscriber() {
        return inscriber;
    }

    public static IBlockDefinition wirelessAccessPoint() {
        return wirelessAccessPoint;
    }

    public static IBlockDefinition charger() {
        return charger;
    }

    public static IBlockDefinition tinyTNT() {
        return tinyTNT;
    }

    public static IBlockDefinition securityStation() {
        return securityStation;
    }

    public static IBlockDefinition quantumRing() {
        return quantumRing;
    }

    public static IBlockDefinition quantumLink() {
        return quantumLink;
    }

    public static IBlockDefinition spatialPylon() {
        return spatialPylon;
    }

    public static IBlockDefinition spatialIOPort() {
        return spatialIOPort;
    }

    public static IBlockDefinition spatialAnchor() {
        return spatialAnchor;
    }

    public static IBlockDefinition multiPart() {
        return multiPart;
    }

    public static IBlockDefinition controller() {
        return controller;
    }

    public static IBlockDefinition drive() {
        return drive;
    }

    public static IBlockDefinition chest() {
        return chest;
    }

    public static IBlockDefinition iface() {
        return iface;
    }

    public static IBlockDefinition fluidIface() {
        return fluidIface;
    }

    public static IBlockDefinition cellWorkbench() {
        return cellWorkbench;
    }

    public static IBlockDefinition iOPort() {
        return iOPort;
    }

    public static IBlockDefinition condenser() {
        return condenser;
    }

    public static IBlockDefinition energyAcceptor() {
        return energyAcceptor;
    }

    public static IBlockDefinition vibrationChamber() {
        return vibrationChamber;
    }

    public static IBlockDefinition quartzGrowthAccelerator() {
        return quartzGrowthAccelerator;
    }

    public static IBlockDefinition energyCell() {
        return energyCell;
    }

    public static IBlockDefinition energyCellDense() {
        return energyCellDense;
    }

    public static IBlockDefinition energyCellCreative() {
        return energyCellCreative;
    }

    public static IBlockDefinition craftingUnit() {
        return craftingUnit;
    }

    public static IBlockDefinition craftingAccelerator() {
        return craftingAccelerator;
    }

    public static IBlockDefinition craftingStorage1k() {
        return craftingStorage1k;
    }

    public static IBlockDefinition craftingStorage4k() {
        return craftingStorage4k;
    }

    public static IBlockDefinition craftingStorage16k() {
        return craftingStorage16k;
    }

    public static IBlockDefinition craftingStorage64k() {
        return craftingStorage64k;
    }

    public static IBlockDefinition craftingMonitor() {
        return craftingMonitor;
    }

    public static IBlockDefinition molecularAssembler() {
        return molecularAssembler;
    }

    public static IBlockDefinition lightDetector() {
        return lightDetector;
    }

    public static IBlockDefinition paint() {
        return paint;
    }

    public static IBlockDefinition chunkLoader() {
        return chunkLoader;
    }

    public static IBlockDefinition itemGen() {
        return itemGen;
    }

    public static IBlockDefinition phantomNode() {
        return phantomNode;
    }

    public static IBlockDefinition cubeGenerator() {
        return cubeGenerator;
    }

    public static IBlockDefinition energyGenerator() {
        return energyGenerator;
    }

    private static Builder block(String id, Supplier<Block> blockSupplier) {
        return new Builder(id, blockSupplier);
    }

    private static Builder decoBlock(String id, Supplier<Block> blockSupplier) {
        return new Builder(id, blockSupplier);
    }

    private static Builder craftingBlock(String id, Supplier<Block> blockSupplier) {
        return new Builder(id, blockSupplier);
    }

    private static class Builder {

        private final ResourceLocation id;

        private final Supplier<? extends Block> blockSupplier;

        private final ItemGroup itemGroup = CreativeTab.INSTANCE;

        private BiFunction<Block, Item.Properties, BlockItem> itemFactory;

        Builder(String id, Supplier<? extends Block> blockSupplier) {
            this.id = AppEng.makeId(id);
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

    // Used to control in which order static constructors are called
    public static void init() {
    }

}
