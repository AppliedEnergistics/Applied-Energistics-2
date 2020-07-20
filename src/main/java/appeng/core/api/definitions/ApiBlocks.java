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

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.ITileDefinition;
import appeng.api.features.AEFeature;
import appeng.block.AEBaseBlockItemChargeable;
import appeng.block.crafting.*;
import appeng.block.grindstone.CrankBlock;
import appeng.block.grindstone.GrinderBlock;
import appeng.block.misc.*;
import appeng.block.networking.*;
import appeng.block.paint.PaintSplotchesBlock;
import appeng.block.paint.PaintSplotchesRendering;
import appeng.block.qnb.QuantumBridgeRendering;
import appeng.block.qnb.QuantumLinkChamberBlock;
import appeng.block.qnb.QuantumRingBlock;
import appeng.block.spatial.MatrixFrameBlock;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.block.spatial.SpatialPylonBlock;
import appeng.block.storage.*;
import appeng.block.storage.ChestBlock;
import appeng.bootstrap.*;
import static appeng.block.crafting.AbstractCraftingUnitBlock.CraftingUnitType;
import appeng.bootstrap.components.IInitComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.client.render.crafting.CraftingCubeRendering;
import appeng.client.render.crafting.CraftingMonitorTESR;
import appeng.client.render.crafting.MonitorBakedModel;
import appeng.client.render.model.AutoRotatingBakedModel;
import appeng.client.render.spatial.SpatialPylonRendering;
import appeng.client.render.tesr.ChestTileEntityRenderer;
import appeng.client.render.tesr.CrankTESR;
import appeng.client.render.tesr.DriveLedTileEntityRenderer;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.decorative.AEDecorativeBlock;
import appeng.decorative.solid.*;
import appeng.decorative.solid.SkyStoneBlock.SkystoneType;
import appeng.entity.TinyTNTPrimedEntity;
import appeng.fluids.block.FluidInterfaceBlock;
import appeng.fluids.tile.FluidInterfaceBlockEntity;
import appeng.hooks.TinyTNTDispenseItemBehavior;
import appeng.tile.crafting.*;
import appeng.tile.grindstone.CrankBlockEntity;
import appeng.tile.grindstone.GrinderBlockEntity;
import appeng.tile.misc.*;
import appeng.tile.networking.*;
import appeng.tile.qnb.QuantumBridgeBlockEntity;
import appeng.tile.spatial.SpatialIOPortBlockEntity;
import appeng.tile.spatial.SpatialPylonBlockEntity;
import appeng.tile.storage.ChestBlockEntity;
import appeng.tile.storage.DriveBlockEntity;
import appeng.tile.storage.IOPortBlockEntity;
import appeng.tile.storage.SkyChestBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.sound.BlockSoundGroup;

import static appeng.block.AEBaseBlock.defaultProps;

/**
 * Internal implementation for the API blocks
 */
public final class ApiBlocks implements IBlocks {
    private final IBlockDefinition quartzOre;
    private final IBlockDefinition quartzOreCharged;
    private final IBlockDefinition matrixFrame;
    private final IBlockDefinition quartzBlock;
    private final IBlockDefinition quartzPillar;
    private final IBlockDefinition chiseledQuartzBlock;
    private final IBlockDefinition quartzGlass;
    private final IBlockDefinition quartzVibrantGlass;
    private final IBlockDefinition quartzFixture;
    private final IBlockDefinition fluixBlock;
    private final IBlockDefinition skyStoneBlock;
    private final IBlockDefinition smoothSkyStoneBlock;
    private final IBlockDefinition skyStoneBrick;
    private final IBlockDefinition skyStoneSmallBrick;
    private final IBlockDefinition skyStoneChest;
    private final IBlockDefinition smoothSkyStoneChest;
    private final IBlockDefinition skyCompass;
    private final ITileDefinition grindstone;
    private final ITileDefinition crank;
    private final ITileDefinition inscriber;
    private final ITileDefinition wirelessAccessPoint;
    private final ITileDefinition charger;
    private final IBlockDefinition tinyTNT;
    private final ITileDefinition securityStation;
    private final ITileDefinition quantumRing;
    private final ITileDefinition quantumLink;
    private final ITileDefinition spatialPylon;
    private final ITileDefinition spatialIOPort;
    private final ITileDefinition multiPart;
    private final ITileDefinition controller;
    private final ITileDefinition drive;
    private final ITileDefinition chest;
    private final ITileDefinition iface;
    private final ITileDefinition fluidIface;
    private final ITileDefinition cellWorkbench;
    private final ITileDefinition iOPort;
    private final ITileDefinition condenser;
    private final ITileDefinition energyAcceptor;
    private final ITileDefinition vibrationChamber;
    private final ITileDefinition quartzGrowthAccelerator;
    private final ITileDefinition energyCell;
    private final ITileDefinition energyCellDense;
    private final ITileDefinition energyCellCreative;
    private final ITileDefinition craftingUnit;
    private final ITileDefinition craftingAccelerator;
    private final ITileDefinition craftingStorage1k;
    private final ITileDefinition craftingStorage4k;
    private final ITileDefinition craftingStorage16k;
    private final ITileDefinition craftingStorage64k;
    private final ITileDefinition craftingMonitor;
    private final ITileDefinition molecularAssembler;
    private final ITileDefinition lightDetector;
    private final ITileDefinition paint;
    private final IBlockDefinition skyStoneStairs;
    private final IBlockDefinition smoothSkyStoneStairs;
    private final IBlockDefinition skyStoneBrickStairs;
    private final IBlockDefinition skyStoneSmallBrickStairs;
    private final IBlockDefinition fluixStairs;
    private final IBlockDefinition quartzStairs;
    private final IBlockDefinition chiseledQuartzStairs;
    private final IBlockDefinition quartzPillarStairs;

    private final IBlockDefinition skyStoneSlab;
    private final IBlockDefinition smoothSkyStoneSlab;
    private final IBlockDefinition skyStoneBrickSlab;
    private final IBlockDefinition skyStoneSmallBrickSlab;
    private final IBlockDefinition fluixSlab;
    private final IBlockDefinition quartzSlab;
    private final IBlockDefinition chiseledQuartzSlab;
    private final IBlockDefinition quartzPillarSlab;

    private IBlockDefinition itemGen;
    private IBlockDefinition chunkLoader;
    private IBlockDefinition phantomNode;
    private IBlockDefinition cubeGenerator;
    private IBlockDefinition energyGenerator;

    private static final FabricBlockSettings QUARTZ_PROPERTIES = defaultProps(Material.STONE).strength(3, 5);

    private static final FabricBlockSettings SKYSTONE_PROPERTIES = defaultProps(Material.STONE).strength(50,
            150);

    private static FabricBlockSettings glassProps() {
        return defaultProps(Material.GLASS)
                .sounds(BlockSoundGroup.GLASS)
                .nonOpaque()
                .allowsSpawning((state, world, pos, type) -> false)
                .solidBlock((state, world, pos) -> false)
                .suffocates((state, world, pos) -> false)
                .blockVision((state, world, pos) -> false);
    }

    public ApiBlocks(FeatureFactory registry) {
        this.quartzOre = registry.block("quartz_ore", () -> new QuartzOreBlock(QUARTZ_PROPERTIES))
                .features(AEFeature.CERTUS_ORE).build();
        this.quartzOreCharged = registry.block("charged_quartz_ore", () -> new ChargedQuartzOreBlock(QUARTZ_PROPERTIES))
                .features(AEFeature.CERTUS_ORE, AEFeature.CHARGED_CERTUS_ORE).rendering(new BlockRenderingCustomizer() {
                    @Override
                    @Environment(EnvType.CLIENT)
                    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
                        rendering.renderType(RenderLayer.getCutout());
                    }
                }).build();
        this.matrixFrame = registry.block("matrix_frame", MatrixFrameBlock::new).features(AEFeature.SPATIAL_IO).build();

        FeatureFactory deco = registry.features(AEFeature.DECORATIVE_BLOCKS);
        this.quartzBlock = deco.block("quartz_block", () -> new AEDecorativeBlock(QUARTZ_PROPERTIES)).build();
        this.quartzPillar = deco.block("quartz_pillar", () -> new QuartzPillarBlock(QUARTZ_PROPERTIES)).build();
        this.chiseledQuartzBlock = deco.block("chiseled_quartz_block", () -> new AEDecorativeBlock(QUARTZ_PROPERTIES))
                .build();

        this.quartzGlass = registry.features(AEFeature.QUARTZ_GLASS)
                .block("quartz_glass", () -> new QuartzGlassBlock(glassProps()))
                .rendering(new BlockRenderingCustomizer() {
                    @Override
                    @Environment(EnvType.CLIENT)
                    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
                        rendering.renderType(RenderLayer.getCutout());
                    }
                }).build();
        this.quartzVibrantGlass = deco
                .block("quartz_vibrant_glass",
                        () -> new QuartzLampBlock(glassProps().lightLevel(15)))
                .addFeatures(AEFeature.DECORATIVE_LIGHTS, AEFeature.QUARTZ_GLASS)
                .rendering(new BlockRenderingCustomizer() {
                    @Override
                    @Environment(EnvType.CLIENT)
                    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
                        rendering.renderType(RenderLayer.getCutout());
                    }
                }).build();

        this.quartzFixture = registry.block("quartz_fixture", QuartzFixtureBlock::new)
                .features(AEFeature.DECORATIVE_LIGHTS).rendering(new BlockRenderingCustomizer() {
                    @Override
                    @Environment(EnvType.CLIENT)
                    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
                        rendering.renderType(RenderLayer.getCutout());
                    }
                }).build();

        this.fluixBlock = registry.features(AEFeature.FLUIX)
                .block("fluix_block", () -> new AEDecorativeBlock(QUARTZ_PROPERTIES)).build();

        this.skyStoneBlock = registry
                .features(
                        AEFeature.SKY_STONE)
                .block("sky_stone_block", () -> new SkyStoneBlock(SkystoneType.STONE,
                        defaultProps(Material.STONE).strength(50, 150).breakByTool(FabricToolTags.PICKAXES, 3)))
                .build();

        this.smoothSkyStoneBlock = registry.features(AEFeature.SKY_STONE)
                .block("smooth_sky_stone_block", () -> new SkyStoneBlock(SkystoneType.BLOCK, SKYSTONE_PROPERTIES))
                .build();
        this.skyStoneBrick = deco
                .block("sky_stone_brick", () -> new SkyStoneBlock(SkystoneType.BRICK, SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();
        this.skyStoneSmallBrick = deco
                .block("sky_stone_small_brick", () -> new SkyStoneBlock(SkystoneType.SMALL_BRICK, SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();

        AbstractBlock.Settings skyStoneChestProps = defaultProps(Material.STONE)
                .strength(50, 150)
                .solidBlock((state, world, pos) -> false);

        TileEntityDefinition skyChestTile = registry
                .tileEntity("sky_chest", SkyChestBlockEntity.class, SkyChestBlockEntity::new)
                .rendering(new TileEntityRenderingCustomizer<SkyChestBlockEntity>() {
                    @Override
                    @Environment(EnvType.CLIENT)
                    public void customize(TileEntityRendering<SkyChestBlockEntity> rendering) {
                        rendering.tileEntityRenderer(SkyChestTESR::new);
                    }
                }).build();
        this.skyStoneChest = registry
                .block("sky_stone_chest", () -> new SkyChestBlock(SkyChestBlock.SkyChestType.STONE, skyStoneChestProps))
                .features(AEFeature.SKY_STONE, AEFeature.SKY_STONE_CHESTS).tileEntity(skyChestTile).build();
        this.smoothSkyStoneChest = registry
                .block("smooth_sky_stone_chest",
                        () -> new SkyChestBlock(SkyChestBlock.SkyChestType.BLOCK, skyStoneChestProps))
                .features(AEFeature.SKY_STONE, AEFeature.SKY_STONE_CHESTS).tileEntity(skyChestTile).build();

        this.skyCompass = registry.block("sky_compass", () -> new SkyCompassBlock(defaultProps(Material.SUPPORTED)))
                .features(AEFeature.METEORITE_COMPASS)
                .tileEntity(registry.tileEntity("sky_compass", SkyCompassBlockEntity.class, SkyCompassBlockEntity::new)
                        .rendering(new SkyCompassRendering()).build())
                .build();
        this.grindstone = registry
                .block("grindstone", () -> new GrinderBlock(defaultProps(Material.STONE).strength(3.2f)))
                .features(AEFeature.GRIND_STONE)
                .tileEntity(registry.tileEntity("grindstone", GrinderBlockEntity.class, GrinderBlockEntity::new).build())
                .build();
        this.crank = registry
                .block("crank",
                        () -> new CrankBlock(
                                defaultProps(Material.WOOD).breakByTool(FabricToolTags.AXES, 0).solidBlock((state, world, pos) -> false)))
                .features(AEFeature.GRIND_STONE)
                .tileEntity(registry.tileEntity("crank", CrankBlockEntity.class, CrankBlockEntity::new)
                        .rendering(new TileEntityRenderingCustomizer<CrankBlockEntity>() {
                            @Override
                            @Environment(EnvType.CLIENT)
                            public void customize(TileEntityRendering<CrankBlockEntity> rendering) {
                                rendering.tileEntityRenderer(CrankTESR::new);
                            }
                        }).build())
                .build();
        this.inscriber = registry.block("inscriber", () -> new InscriberBlock(defaultProps(Material.METAL).solidBlock((state, world, pos) -> false)))
                .features(AEFeature.INSCRIBER)
                .tileEntity(registry.tileEntity("inscriber", InscriberBlockEntity.class, InscriberBlockEntity::new)
                        .rendering(new InscriberRendering()).build())
                .build();
            this.wirelessAccessPoint = registry.block("wireless_access_point", WirelessBlock::new)
                    .features(AEFeature.WIRELESS_ACCESS_TERMINAL)
                    .tileEntity(registry
                            .tileEntity("wireless_access_point", WirelessBlockEntity.class, WirelessBlockEntity::new).build())
                    .rendering(new WirelessRendering()).build();
        this.charger = registry.block("charger", ChargerBlock::new).features(AEFeature.CHARGER)
                .tileEntity(registry.tileEntity("charger", ChargerBlockEntity.class, ChargerBlockEntity::new)
                        .rendering(new TileEntityRenderingCustomizer<ChargerBlockEntity>() {
                            @Override
                            @Environment(EnvType.CLIENT)
                            public void customize(TileEntityRendering<ChargerBlockEntity> rendering) {
                                rendering.tileEntityRenderer(ChargerBlock.createTesr());
                            }
                        }).build())
                .build();

        TinyTNTPrimedEntity.TYPE = registry
                .<TinyTNTPrimedEntity>entity("tiny_tnt_primed", TinyTNTPrimedEntity::new, SpawnGroup.MISC)
                // Entity properties are a copy of the standard TNT entity
                .customize(p -> p.trackable(10, 10, true).dimensions(EntityDimensions.fixed(0.98f, 0.98f)))
                .build();

        this.tinyTNT = registry
                .block("tiny_tnt",
                        () -> new TinyTNTBlock(
                                defaultProps(Material.TNT).sounds(BlockSoundGroup.GRASS).breakInstantly()))
                .features(AEFeature.TINY_TNT).bootstrap((block, item) -> (IInitComponent) () -> DispenserBlock
                        .registerBehavior(item, new TinyTNTDispenseItemBehavior()))
                .build();
       this.securityStation = registry.block("security_station", SecurityStationBlock::new)
               .features(AEFeature.SECURITY)
               .tileEntity(registry
                       .tileEntity("security_station", SecurityStationBlockEntity.class, SecurityStationBlockEntity::new)
                       .build())
               .rendering(new SecurityStationRendering()).build();

       TileEntityDefinition quantumRingTile = registry
               .tileEntity("quantum_ring", QuantumBridgeBlockEntity.class, QuantumBridgeBlockEntity::new).build();
       this.quantumRing = registry.block("quantum_ring", QuantumRingBlock::new)
               .features(AEFeature.QUANTUM_NETWORK_BRIDGE).tileEntity(quantumRingTile)
               .rendering(new QuantumBridgeRendering()).build();
       this.quantumLink = registry.block("quantum_link", QuantumLinkChamberBlock::new)
               .features(AEFeature.QUANTUM_NETWORK_BRIDGE).tileEntity(quantumRingTile)
               .rendering(new QuantumBridgeRendering()).build();
       this.spatialPylon = registry.block("spatial_pylon", SpatialPylonBlock::new).features(AEFeature.SPATIAL_IO)
               .tileEntity(registry
                       .tileEntity("spatial_pylon", SpatialPylonBlockEntity.class, SpatialPylonBlockEntity::new).build())
               .rendering(new SpatialPylonRendering()).build();
       this.spatialIOPort = registry.block("spatial_io_port", SpatialIOPortBlock::new).features(AEFeature.SPATIAL_IO)
               .tileEntity(registry
                       .tileEntity("spatial_io_port", SpatialIOPortBlockEntity.class, SpatialIOPortBlockEntity::new)
                       .build())
               .build();
       this.controller = registry
               .block("controller", ControllerBlock::new).features(AEFeature.CHANNELS).tileEntity(registry
                       .tileEntity("controller", ControllerBlockEntity.class, ControllerBlockEntity::new).build())
               .rendering(new ControllerRendering()).build();
       this.drive = registry.block("drive", DriveBlock::new).features(AEFeature.STORAGE_CELLS, AEFeature.ME_DRIVE)
               .tileEntity(registry.tileEntity("drive", DriveBlockEntity.class, DriveBlockEntity::new)
                       .rendering(new TileEntityRenderingCustomizer<DriveBlockEntity>() {
                           @Override
                           @Environment(EnvType.CLIENT)
                           public void customize(TileEntityRendering<DriveBlockEntity> rendering) {
                               rendering.tileEntityRenderer(DriveLedTileEntityRenderer::new);
                           }
                       }).build())
               .rendering(new DriveRendering()).build();
       this.chest = registry.block("chest", ChestBlock::new).features(AEFeature.STORAGE_CELLS, AEFeature.ME_CHEST)
               .tileEntity(registry.tileEntity("chest", ChestBlockEntity.class, ChestBlockEntity::new)
                        .rendering(new TileEntityRenderingCustomizer<ChestBlockEntity>() {
                            @Override
                            @Environment(EnvType.CLIENT)
                            public void customize(TileEntityRendering<ChestBlockEntity> rendering) {
                                rendering.tileEntityRenderer(ChestTileEntityRenderer::new);
                            }
                        }).build())
               .rendering(new ChestRendering()).build();
       this.iface = registry.block("interface", InterfaceBlock::new).features(AEFeature.INTERFACE)
               .tileEntity(
                       registry.tileEntity("interface", InterfaceBlockEntity.class, InterfaceBlockEntity::new).build())
               .build();
       this.fluidIface = registry.block("fluid_interface", FluidInterfaceBlock::new)
               .features(AEFeature.FLUID_INTERFACE)
               .tileEntity(registry
                       .tileEntity("fluid_interface", FluidInterfaceBlockEntity.class, FluidInterfaceBlockEntity::new)
                       .build())
               .build();
       this.cellWorkbench = registry.block("cell_workbench", CellWorkbenchBlock::new).features(AEFeature.STORAGE_CELLS)
               .tileEntity(registry
                       .tileEntity("cell_workbench", CellWorkbenchBlockEntity.class, CellWorkbenchBlockEntity::new)
                       .build())
               .build();
       this.iOPort = registry.block("io_port", IOPortBlock::new).features(AEFeature.STORAGE_CELLS, AEFeature.IO_PORT)
               .tileEntity(registry.tileEntity("io_port", IOPortBlockEntity.class, IOPortBlockEntity::new).build())
               .build();
       this.condenser = registry.block("condenser", CondenserBlock::new).features(AEFeature.CONDENSER)
               .tileEntity(
                       registry.tileEntity("condenser", CondenserBlockEntity.class, CondenserBlockEntity::new).build())
               .build();
       this.energyAcceptor = registry.block("energy_acceptor", EnergyAcceptorBlock::new)
               .features(AEFeature.ENERGY_ACCEPTOR)
               .tileEntity(registry
                       .tileEntity("energy_acceptor", EnergyAcceptorBlockEntity.class, EnergyAcceptorBlockEntity::new)
                       .build())
               .build();
       this.vibrationChamber = registry.block("vibration_chamber", VibrationChamberBlock::new)
               .features(AEFeature.POWER_GEN).tileEntity(registry.tileEntity("vibration_chamber",
                       VibrationChamberBlockEntity.class, VibrationChamberBlockEntity::new).build())
               .build();
       this.quartzGrowthAccelerator = registry.block("quartz_growth_accelerator", QuartzGrowthAcceleratorBlock::new)
               .tileEntity(registry.tileEntity("quartz_growth_accelerator", QuartzGrowthAcceleratorBlockEntity.class,
                       QuartzGrowthAcceleratorBlockEntity::new).build())
               .features(AEFeature.CRYSTAL_GROWTH_ACCELERATOR).build();
       this.energyCell = registry.block("energy_cell", EnergyCellBlock::new).features(AEFeature.ENERGY_CELLS)
               .item(AEBaseBlockItemChargeable::new).tileEntity(registry
                       .tileEntity("energy_cell", EnergyCellBlockEntity.class, EnergyCellBlockEntity::new).build())
               .build();
       this.energyCellDense = registry.block("dense_energy_cell", DenseEnergyCellBlock::new)
               .features(AEFeature.ENERGY_CELLS, AEFeature.DENSE_ENERGY_CELLS).item(AEBaseBlockItemChargeable::new)
               .tileEntity(registry.tileEntity("dense_energy_cell", DenseEnergyCellBlockEntity.class,
                       DenseEnergyCellBlockEntity::new).build())
               .build();
       this.energyCellCreative = registry.block("creative_energy_cell", CreativeEnergyCellBlock::new)
               .features(AEFeature.CREATIVE).tileEntity(registry.tileEntity("creative_energy_cell",
                       CreativeEnergyCellBlockEntity.class, CreativeEnergyCellBlockEntity::new).build())
               .build();

       TileEntityDefinition craftingUnit = registry
               .tileEntity("crafting_unit", CraftingBlockEntity.class, CraftingBlockEntity::new).build();

       FeatureFactory crafting = registry.features(AEFeature.CRAFTING_CPU);
       AbstractBlock.Settings craftingBlockProps = defaultProps(Material.METAL);
       this.craftingUnit = crafting
               .block("crafting_unit", () -> new CraftingUnitBlock(craftingBlockProps, CraftingUnitType.UNIT))
               .rendering(new CraftingCubeRendering()).tileEntity(craftingUnit).build();
       this.craftingAccelerator = crafting
               .block("crafting_accelerator",
                       () -> new CraftingUnitBlock(craftingBlockProps, CraftingUnitType.ACCELERATOR))
               .rendering(new CraftingCubeRendering()).tileEntity(craftingUnit).build();

       TileEntityDefinition craftingStorage = registry
               .tileEntity("crafting_storage", CraftingStorageBlockEntity.class, CraftingStorageBlockEntity::new)
               .build();
       this.craftingStorage1k = crafting
               .block("1k_crafting_storage",
                       () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_1K))
               .item(CraftingStorageItem::new).tileEntity(craftingStorage).rendering(new CraftingCubeRendering())
               .build();
       this.craftingStorage4k = crafting
               .block("4k_crafting_storage",
                       () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_4K))
               .item(CraftingStorageItem::new).tileEntity(craftingStorage).rendering(new CraftingCubeRendering())
               .build();
       this.craftingStorage16k = crafting
               .block("16k_crafting_storage",
                       () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_16K))
               .item(CraftingStorageItem::new).tileEntity(craftingStorage).rendering(new CraftingCubeRendering())
               .build();
       this.craftingStorage64k = crafting
               .block("64k_crafting_storage",
                       () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_64K))
               .item(CraftingStorageItem::new).tileEntity(craftingStorage).rendering(new CraftingCubeRendering())
               .build();
       this.craftingMonitor = crafting.block("crafting_monitor", () -> new CraftingMonitorBlock(craftingBlockProps))
               .tileEntity(registry
                       .tileEntity("crafting_monitor", CraftingMonitorBlockEntity.class, CraftingMonitorBlockEntity::new)
                       .rendering(new TileEntityRenderingCustomizer<CraftingMonitorBlockEntity>() {
                           @Environment(EnvType.CLIENT)
                           @Override
                           public void customize(TileEntityRendering<CraftingMonitorBlockEntity> rendering) {
                               rendering.tileEntityRenderer(CraftingMonitorTESR::new);
                           }
                       }).build())
               .rendering(new BlockRenderingCustomizer() {
                   @Override
                   @Environment(EnvType.CLIENT)
                   public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
                       rendering.renderType(RenderLayer.getCutout());
                       rendering.modelCustomizer((path, model) -> {
                           // The formed model handles rotations itself, the unformed one does not
                           if (model instanceof MonitorBakedModel) {
                               return model;
                           }
                           return new AutoRotatingBakedModel(model);
                       });
                   }
               }).build();

       this.molecularAssembler = registry
               .block("molecular_assembler", () -> new MolecularAssemblerBlock(defaultProps(Material.METAL).solidBlock((state, world, pos) -> false)))
               .features(AEFeature.MOLECULAR_ASSEMBLER).rendering(new BlockRenderingCustomizer() {
                   @Environment(EnvType.CLIENT)
                   @Override
                   public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
                       rendering.renderType(RenderLayer.getCutout());
                   }
               })
               .tileEntity(registry
                       .tileEntity("molecular_assembler", MolecularAssemblerBlockEntity.class,
                               MolecularAssemblerBlockEntity::new)
                       .rendering(new TileEntityRenderingCustomizer<MolecularAssemblerBlockEntity>() {
                           @Override
                           @Environment(EnvType.CLIENT)
                           public void customize(TileEntityRendering<MolecularAssemblerBlockEntity> rendering) {
                               rendering.tileEntityRenderer(MolecularAssemblerRenderer::new);
                           }
                       }).build())
               .build();

        this.lightDetector = registry.block("light_detector", LightDetectorBlock::new)
                .features(AEFeature.LIGHT_DETECTOR)
                .tileEntity(registry
                        .tileEntity("light_detector", LightDetectorBlockEntity.class, LightDetectorBlockEntity::new)
                        .build())
                .rendering(new BlockRenderingCustomizer() {
                    @Override
                    @Environment(EnvType.CLIENT)
                    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
                        rendering.renderType(RenderLayer.getCutout());
                    }
                }).build();
        this.paint = registry
                .block("paint", PaintSplotchesBlock::new).features(AEFeature.PAINT_BALLS).tileEntity(registry
                        .tileEntity("paint", PaintSplotchesBlockEntity.class, PaintSplotchesBlockEntity::new).build())
                .rendering(new PaintSplotchesRendering()).build();

        this.skyStoneStairs = deco
                .block("sky_stone_stairs",
                        () -> new AEStairsBlock(this.skyStoneBlock().block().getDefaultState(), SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();
        this.smoothSkyStoneStairs = deco
                .block("smooth_sky_stone_stairs",
                        () -> new AEStairsBlock(this.smoothSkyStoneBlock().block().getDefaultState(), SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();
        this.skyStoneBrickStairs = deco
                .block("sky_stone_brick_stairs",
                        () -> new AEStairsBlock(this.skyStoneBrick().block().getDefaultState(), SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();
        this.skyStoneSmallBrickStairs = deco
                .block("sky_stone_small_brick_stairs",
                        () -> new AEStairsBlock(this.skyStoneSmallBrick().block().getDefaultState(), SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();

        this.fluixStairs = deco
                .block("fluix_stairs",
                        () -> new AEStairsBlock(this.fluixBlock().block().getDefaultState(), QUARTZ_PROPERTIES))
                .addFeatures(AEFeature.FLUIX).build();
        this.quartzStairs = deco
                .block("quartz_stairs",
                        () -> new AEStairsBlock(this.quartzBlock().block().getDefaultState(), QUARTZ_PROPERTIES))
                .addFeatures(AEFeature.CERTUS).build();
        this.chiseledQuartzStairs = deco
                .block("chiseled_quartz_stairs",
                        () -> new AEStairsBlock(this.chiseledQuartzBlock().block().getDefaultState(), QUARTZ_PROPERTIES))
                .addFeatures(AEFeature.CERTUS).build();
        this.quartzPillarStairs = deco
                .block("quartz_pillar_stairs",
                        () -> new AEStairsBlock(this.quartzPillar().block().getDefaultState(), QUARTZ_PROPERTIES))
                .addFeatures(AEFeature.CERTUS).build();

        this.multiPart = registry.block("cable_bus", CableBusBlock::new).rendering(new CableBusRendering())
                .tileEntity(registry.tileEntity("cable_bus", CableBusBlockEntity.class, CableBusBlockEntity::new)
                        .rendering(new TileEntityRenderingCustomizer<CableBusBlockEntity>() {
                            @Override
                            @Environment(EnvType.CLIENT)
                            public void customize(TileEntityRendering<CableBusBlockEntity> rendering) {
                                rendering.tileEntityRenderer(CableBusTESR::new);
                            }
                        }).build())
                .build();

        this.skyStoneSlab = deco.block("sky_stone_slab", () -> new SlabBlock(SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();
        this.smoothSkyStoneSlab = deco.block("smooth_sky_stone_slab", () -> new SlabBlock(SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();
        this.skyStoneBrickSlab = deco.block("sky_stone_brick_slab", () -> new SlabBlock(SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();
        this.skyStoneSmallBrickSlab = deco.block("sky_stone_small_brick_slab", () -> new SlabBlock(SKYSTONE_PROPERTIES))
                .addFeatures(AEFeature.SKY_STONE).build();

        this.fluixSlab = deco.block("fluix_slab", () -> new SlabBlock(QUARTZ_PROPERTIES)).addFeatures(AEFeature.FLUIX)
                .build();
        this.quartzSlab = deco.block("quartz_slab", () -> new SlabBlock(QUARTZ_PROPERTIES))
                .addFeatures(AEFeature.CERTUS).build();
        this.chiseledQuartzSlab = deco.block("chiseled_quartz_slab", () -> new SlabBlock(QUARTZ_PROPERTIES))
                .addFeatures(AEFeature.CERTUS).build();
        this.quartzPillarSlab = deco.block("quartz_pillar_slab", () -> new SlabBlock(QUARTZ_PROPERTIES))
                .addFeatures(AEFeature.CERTUS).build();

// FIXME        this.itemGen = registry.block("debug_item_gen", ItemGenBlock::new)
// FIXME                .features(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE)
// FIXME                .tileEntity(
// FIXME                        registry.tileEntity("debug_item_gen", ItemGenBlockEntity.class, ItemGenBlockEntity::new).build())
// FIXME                .build();
// FIXME        this.chunkLoader = registry.block("debug_chunk_loader", ChunkLoaderBlock::new)
// FIXME                .features(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE)
// FIXME                .tileEntity(registry
// FIXME                        .tileEntity("debug_chunk_loader", ChunkLoaderBlockEntity.class, ChunkLoaderBlockEntity::new)
// FIXME                        .build())
// FIXME                .build();
// FIXME        this.phantomNode = registry.block("debug_phantom_node", PhantomNodeBlock::new)
// FIXME                .features(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE)
// FIXME                .tileEntity(registry
// FIXME                        .tileEntity("debug_phantom_node", PhantomNodeBlockEntity.class, PhantomNodeBlockEntity::new)
// FIXME                        .build())
// FIXME                .build();
// FIXME        this.cubeGenerator = registry.block("debug_cube_gen", CubeGeneratorBlock::new)
// FIXME                .features(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE)
// FIXME                .tileEntity(registry
// FIXME                        .tileEntity("debug_cube_gen", CubeGeneratorBlockEntity.class, CubeGeneratorBlockEntity::new)
// FIXME                        .build())
// FIXME                .build();
// FIXME        this.energyGenerator = registry.block("debug_energy_gen", EnergyGeneratorBlock::new)
// FIXME                .features(AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE)
// FIXME                .tileEntity(registry
// FIXME                        .tileEntity("debug_energy_gen", EnergyGeneratorBlockEntity.class, EnergyGeneratorBlockEntity::new)
// FIXME                        .build())
// FIXME                .build();
    }

    @Override
    public IBlockDefinition quartzOre() {
        return this.quartzOre;
    }

    @Override
    public IBlockDefinition quartzOreCharged() {
        return this.quartzOreCharged;
    }

    @Override
    public IBlockDefinition matrixFrame() {
        return this.matrixFrame;
    }

    @Override
    public IBlockDefinition quartzBlock() {
        return this.quartzBlock;
    }

    @Override
    public IBlockDefinition quartzPillar() {
        return this.quartzPillar;
    }

    @Override
    public IBlockDefinition chiseledQuartzBlock() {
        return this.chiseledQuartzBlock;
    }

    @Override
    public IBlockDefinition quartzGlass() {
        return this.quartzGlass;
    }

    @Override
    public IBlockDefinition quartzVibrantGlass() {
        return this.quartzVibrantGlass;
    }

    @Override
    public IBlockDefinition quartzFixture() {
        return this.quartzFixture;
    }

    @Override
    public IBlockDefinition fluixBlock() {
        return this.fluixBlock;
    }

    @Override
    public IBlockDefinition skyStoneBlock() {
        return this.skyStoneBlock;
    }

    @Override
    public IBlockDefinition smoothSkyStoneBlock() {
        return this.smoothSkyStoneBlock;
    }

    @Override
    public IBlockDefinition skyStoneBrick() {
        return this.skyStoneBrick;
    }

    @Override
    public IBlockDefinition skyStoneSmallBrick() {
        return this.skyStoneSmallBrick;
    }

    @Override
    public IBlockDefinition skyStoneChest() {
        return this.skyStoneChest;
    }

    @Override
    public IBlockDefinition smoothSkyStoneChest() {
        return this.smoothSkyStoneChest;
    }

    @Override
    public IBlockDefinition skyCompass() {
        return this.skyCompass;
    }

    @Override
    public IBlockDefinition skyStoneStairs() {
        return this.skyStoneStairs;
    }

    @Override
    public IBlockDefinition smoothSkyStoneStairs() {
        return this.smoothSkyStoneStairs;
    }

    @Override
    public IBlockDefinition skyStoneBrickStairs() {
        return this.skyStoneBrickStairs;
    }

    @Override
    public IBlockDefinition skyStoneSmallBrickStairs() {
        return this.skyStoneSmallBrickStairs;
    }

    @Override
    public IBlockDefinition fluixStairs() {
        return this.fluixStairs;
    }

    @Override
    public IBlockDefinition quartzStairs() {
        return this.quartzStairs;
    }

    @Override
    public IBlockDefinition chiseledQuartzStairs() {
        return this.chiseledQuartzStairs;
    }

    @Override
    public IBlockDefinition quartzPillarStairs() {
        return this.quartzPillarStairs;
    }

    @Override
    public IBlockDefinition skyStoneSlab() {
        return this.skyStoneSlab;
    }

    @Override
    public IBlockDefinition smoothSkyStoneSlab() {
        return this.smoothSkyStoneSlab;
    }

    @Override
    public IBlockDefinition skyStoneBrickSlab() {
        return this.skyStoneBrickSlab;
    }

    @Override
    public IBlockDefinition skyStoneSmallBrickSlab() {
        return this.skyStoneSmallBrickSlab;
    }

    @Override
    public IBlockDefinition fluixSlab() {
        return this.fluixSlab;
    }

    @Override
    public IBlockDefinition quartzSlab() {
        return this.quartzSlab;
    }

    @Override
    public IBlockDefinition chiseledQuartzSlab() {
        return this.chiseledQuartzSlab;
    }

    @Override
    public IBlockDefinition quartzPillarSlab() {
        return this.quartzPillarSlab;
    }

    @Override
    public ITileDefinition grindstone() {
        return this.grindstone;
    }

    @Override
    public ITileDefinition crank() {
        return this.crank;
    }

    @Override
    public ITileDefinition inscriber() {
        return this.inscriber;
    }

    @Override
    public ITileDefinition wirelessAccessPoint() {
        return this.wirelessAccessPoint;
    }

    @Override
    public ITileDefinition charger() {
        return this.charger;
    }

    @Override
    public IBlockDefinition tinyTNT() {
        return this.tinyTNT;
    }

    @Override
    public ITileDefinition securityStation() {
        return this.securityStation;
    }

    @Override
    public ITileDefinition quantumRing() {
        return this.quantumRing;
    }

    @Override
    public ITileDefinition quantumLink() {
        return this.quantumLink;
    }

    @Override
    public ITileDefinition spatialPylon() {
        return this.spatialPylon;
    }

    @Override
    public ITileDefinition spatialIOPort() {
        return this.spatialIOPort;
    }

    @Override
    public ITileDefinition multiPart() {
        return this.multiPart;
    }

    @Override
    public ITileDefinition controller() {
        return this.controller;
    }

    @Override
    public ITileDefinition drive() {
        return this.drive;
    }

    @Override
    public ITileDefinition chest() {
        return this.chest;
    }

    @Override
    public ITileDefinition iface() {
        return this.iface;
    }

    @Override
    public ITileDefinition fluidIface() {
        return this.fluidIface;
    }

    @Override
    public ITileDefinition cellWorkbench() {
        return this.cellWorkbench;
    }

    @Override
    public ITileDefinition iOPort() {
        return this.iOPort;
    }

    @Override
    public ITileDefinition condenser() {
        return this.condenser;
    }

    @Override
    public ITileDefinition energyAcceptor() {
        return this.energyAcceptor;
    }

    @Override
    public ITileDefinition vibrationChamber() {
        return this.vibrationChamber;
    }

    @Override
    public ITileDefinition quartzGrowthAccelerator() {
        return this.quartzGrowthAccelerator;
    }

    @Override
    public ITileDefinition energyCell() {
        return this.energyCell;
    }

    @Override
    public ITileDefinition energyCellDense() {
        return this.energyCellDense;
    }

    @Override
    public ITileDefinition energyCellCreative() {
        return this.energyCellCreative;
    }

    @Override
    public ITileDefinition craftingUnit() {
        return this.craftingUnit;
    }

    @Override
    public ITileDefinition craftingAccelerator() {
        return this.craftingAccelerator;
    }

    @Override
    public ITileDefinition craftingStorage1k() {
        return this.craftingStorage1k;
    }

    @Override
    public ITileDefinition craftingStorage4k() {
        return this.craftingStorage4k;
    }

    @Override
    public ITileDefinition craftingStorage16k() {
        return this.craftingStorage16k;
    }

    @Override
    public ITileDefinition craftingStorage64k() {
        return this.craftingStorage64k;
    }

    @Override
    public ITileDefinition craftingMonitor() {
        return this.craftingMonitor;
    }

    @Override
    public ITileDefinition molecularAssembler() {
        return this.molecularAssembler;
    }

    @Override
    public ITileDefinition lightDetector() {
        return this.lightDetector;
    }

    @Override
    public ITileDefinition paint() {
        return this.paint;
    }

    public IBlockDefinition chunkLoader() {
        return this.chunkLoader;
    }

    public IBlockDefinition itemGen() {
        return this.itemGen;
    }

    public IBlockDefinition phantomNode() {
        return this.phantomNode;
    }

    public IBlockDefinition cubeGenerator() {
        return this.cubeGenerator;
    }

    public IBlockDefinition energyGenerator() {
        return this.energyGenerator;
    }
}
