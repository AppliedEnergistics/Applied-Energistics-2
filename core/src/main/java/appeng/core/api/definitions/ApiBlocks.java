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
import appeng.block.misc.*;
import appeng.block.storage.SkyChestBlock;
import appeng.bootstrap.*;
import appeng.bootstrap.components.IInitComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.decorative.AEDecorativeBlock;
import appeng.decorative.solid.*;
import appeng.decorative.solid.SkyStoneBlock.SkystoneType;
import appeng.entity.TinyTNTPrimedEntity;
import appeng.hooks.TinyTNTDispenseItemBehavior;
import appeng.tile.misc.LightDetectorBlockEntity;
import appeng.tile.misc.SkyCompassBlockEntity;
import appeng.tile.storage.SkyChestBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.sound.BlockSoundGroup;

import static appeng.block.AEBaseBlock.defaultProps;

/**
 * Internal implementation for the API blocks
 */
public final class ApiBlocks implements IBlocks {
    private IBlockDefinition quartzOre;
    private IBlockDefinition quartzOreCharged;
    private IBlockDefinition matrixFrame;
    private IBlockDefinition quartzBlock;
    private IBlockDefinition quartzPillar;
    private IBlockDefinition chiseledQuartzBlock;
    private IBlockDefinition quartzGlass;
    private IBlockDefinition quartzVibrantGlass;
    private IBlockDefinition quartzFixture;
    private IBlockDefinition fluixBlock;
    private IBlockDefinition skyStoneBlock;
    private IBlockDefinition smoothSkyStoneBlock;
    private IBlockDefinition skyStoneBrick;
    private IBlockDefinition skyStoneSmallBrick;
    private IBlockDefinition skyStoneChest;
    private IBlockDefinition smoothSkyStoneChest;
    private IBlockDefinition skyCompass;
    private ITileDefinition grindstone;
    private ITileDefinition crank;
    private ITileDefinition inscriber;
    private ITileDefinition wirelessAccessPoint;
    private ITileDefinition charger;
    private IBlockDefinition tinyTNT;
    private ITileDefinition securityStation;
    private ITileDefinition quantumRing;
    private ITileDefinition quantumLink;
    private ITileDefinition spatialPylon;
    private ITileDefinition spatialIOPort;
    private ITileDefinition multiPart;
    private ITileDefinition controller;
    private ITileDefinition drive;
    private ITileDefinition chest;
    private ITileDefinition iface;
    private ITileDefinition fluidIface;
    private ITileDefinition cellWorkbench;
    private ITileDefinition iOPort;
    private ITileDefinition condenser;
    private ITileDefinition energyAcceptor;
    private ITileDefinition vibrationChamber;
    private ITileDefinition quartzGrowthAccelerator;
    private ITileDefinition energyCell;
    private ITileDefinition energyCellDense;
    private ITileDefinition energyCellCreative;
    private ITileDefinition craftingUnit;
    private ITileDefinition craftingAccelerator;
    private ITileDefinition craftingStorage1k;
    private ITileDefinition craftingStorage4k;
    private ITileDefinition craftingStorage16k;
    private ITileDefinition craftingStorage64k;
    private ITileDefinition craftingMonitor;
    private ITileDefinition molecularAssembler;
    private ITileDefinition lightDetector;
    private ITileDefinition paint;
    private IBlockDefinition skyStoneStairs;
    private IBlockDefinition smoothSkyStoneStairs;
    private IBlockDefinition skyStoneBrickStairs;
    private IBlockDefinition skyStoneSmallBrickStairs;
    private IBlockDefinition fluixStairs;
    private IBlockDefinition quartzStairs;
    private IBlockDefinition chiseledQuartzStairs;
    private IBlockDefinition quartzPillarStairs;

    private IBlockDefinition skyStoneSlab;
    private IBlockDefinition smoothSkyStoneSlab;
    private IBlockDefinition skyStoneBrickSlab;
    private IBlockDefinition skyStoneSmallBrickSlab;
    private IBlockDefinition fluixSlab;
    private IBlockDefinition quartzSlab;
    private IBlockDefinition chiseledQuartzSlab;
    private IBlockDefinition quartzPillarSlab;

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
// FIXME FABRIC        this.matrixFrame = registry.block("matrix_frame", MatrixFrameBlock::new).features(AEFeature.SPATIAL_IO).build();

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
// FIXME FABRIC        this.grindstone = registry
// FIXME FABRIC                .block("grindstone", () -> new GrinderBlock(defaultProps(Material.STONE).strength(3.2f)))
// FIXME FABRIC                .features(AEFeature.GRIND_STONE)
// FIXME FABRIC                .tileEntity(registry.tileEntity("grindstone", GrinderBlockEntity.class, GrinderBlockEntity::new).build())
// FIXME FABRIC                .build();
// FIXME FABRIC        this.crank = registry
// FIXME FABRIC                .block("crank",
// FIXME FABRIC                        () -> new CrankBlock(
// FIXME FABRIC                                defaultProps(Material.WOOD).breakByTool(FabricToolTags.AXES, 0).notSolid()))
// FIXME FABRIC                .features(AEFeature.GRIND_STONE)
// FIXME FABRIC                .tileEntity(registry.tileEntity("crank", CrankBlockEntity.class, CrankBlockEntity::new)
// FIXME FABRIC                        .rendering(new TileEntityRenderingCustomizer<CrankBlockEntity>() {
// FIXME FABRIC                            @Override
// FIXME FABRIC                            @Environment(EnvType.CLIENT)
// FIXME FABRIC                            public void customize(TileEntityRendering<CrankBlockEntity> rendering) {
// FIXME FABRIC                                rendering.tileEntityRenderer(CrankTESR::new);
// FIXME FABRIC                            }
// FIXME FABRIC                        }).build())
// FIXME FABRIC                .build();
// FIXME FABRIC        this.inscriber = registry.block("inscriber", () -> new InscriberBlock(defaultProps(Material.METAL).notSolid()))
// FIXME FABRIC                .features(AEFeature.INSCRIBER)
// FIXME FABRIC                .tileEntity(registry.tileEntity("inscriber", InscriberBlockEntity.class, InscriberBlockEntity::new)
// FIXME FABRIC                        .rendering(new InscriberRendering()).build())
// FIXME FABRIC                .build();
// FIXME FABRIC        this.wirelessAccessPoint = registry.block("wireless_access_point", WirelessBlock::new)
// FIXME FABRIC                .features(AEFeature.WIRELESS_ACCESS_TERMINAL)
// FIXME FABRIC                .tileEntity(registry
// FIXME FABRIC                        .tileEntity("wireless_access_point", WirelessBlockEntity.class, WirelessBlockEntity::new).build())
// FIXME FABRIC                .rendering(new WirelessRendering()).build();
// FIXME FABRIC        this.charger = registry.block("charger", ChargerBlock::new).features(AEFeature.CHARGER)
// FIXME FABRIC                .tileEntity(registry.tileEntity("charger", ChargerBlockEntity.class, ChargerBlockEntity::new)
// FIXME FABRIC                        .rendering(new TileEntityRenderingCustomizer<ChargerBlockEntity>() {
// FIXME FABRIC                            @Override
// FIXME FABRIC                            @Environment(EnvType.CLIENT)
// FIXME FABRIC                            public void customize(TileEntityRendering<ChargerBlockEntity> rendering) {
// FIXME FABRIC                                rendering.tileEntityRenderer(ChargerBlock.createTesr());
// FIXME FABRIC                            }
// FIXME FABRIC                        }).build())
// FIXME FABRIC                .build();

        TinyTNTPrimedEntity.TYPE = registry
                .<TinyTNTPrimedEntity>entity("tiny_tnt_primed", TinyTNTPrimedEntity::new, SpawnGroup.MISC)
                .customize(p -> p.trackable(16, 4, true))
                .build();

        this.tinyTNT = registry
                .block("tiny_tnt",
                        () -> new TinyTNTBlock(
                                defaultProps(Material.TNT).sounds(BlockSoundGroup.GRASS).breakInstantly()))
                .features(AEFeature.TINY_TNT).bootstrap((block, item) -> (IInitComponent) () -> DispenserBlock
                        .registerBehavior(item, new TinyTNTDispenseItemBehavior()))
                .build();
// FIXME       this.securityStation = registry.block("security_station", SecurityStationBlock::new)
// FIXME               .features(AEFeature.SECURITY)
// FIXME               .tileEntity(registry
// FIXME                       .tileEntity("security_station", SecurityStationBlockEntity.class, SecurityStationBlockEntity::new)
// FIXME                       .build())
// FIXME               .rendering(new SecurityStationRendering()).build();
// FIXME
// FIXME       TileEntityDefinition quantumRingTile = registry
// FIXME               .tileEntity("quantum_ring", QuantumBridgeBlockEntity.class, QuantumBridgeBlockEntity::new).build();
// FIXME       this.quantumRing = registry.block("quantum_ring", QuantumRingBlock::new)
// FIXME               .features(AEFeature.QUANTUM_NETWORK_BRIDGE).tileEntity(quantumRingTile)
// FIXME               .rendering(new QuantumBridgeRendering()).build();
// FIXME       this.quantumLink = registry.block("quantum_link", QuantumLinkChamberBlock::new)
// FIXME               .features(AEFeature.QUANTUM_NETWORK_BRIDGE).tileEntity(quantumRingTile)
// FIXME               .rendering(new QuantumBridgeRendering()).build();
// FIXME       this.spatialPylon = registry.block("spatial_pylon", SpatialPylonBlock::new).features(AEFeature.SPATIAL_IO)
// FIXME               .tileEntity(registry
// FIXME                       .tileEntity("spatial_pylon", SpatialPylonBlockEntity.class, SpatialPylonBlockEntity::new).build())
// FIXME               .rendering(new SpatialPylonRendering()).build();
// FIXME       this.spatialIOPort = registry.block("spatial_io_port", SpatialIOPortBlock::new).features(AEFeature.SPATIAL_IO)
// FIXME               .tileEntity(registry
// FIXME                       .tileEntity("spatial_io_port", SpatialIOPortBlockEntity.class, SpatialIOPortBlockEntity::new)
// FIXME                       .build())
// FIXME               .build();
// FIXME       this.controller = registry
// FIXME               .block("controller", ControllerBlock::new).features(AEFeature.CHANNELS).tileEntity(registry
// FIXME                       .tileEntity("controller", ControllerBlockEntity.class, ControllerBlockEntity::new).build())
// FIXME               .rendering(new ControllerRendering()).build();
// FIXME       this.drive = registry.block("drive", DriveBlock::new).features(AEFeature.STORAGE_CELLS, AEFeature.ME_DRIVE)
// FIXME               .tileEntity(registry.tileEntity("drive", DriveBlockEntity.class, DriveBlockEntity::new)
// FIXME                       .rendering(new TileEntityRenderingCustomizer<DriveBlockEntity>() {
// FIXME                           @Override
// FIXME                           @Environment(EnvType.CLIENT)
// FIXME                           public void customize(TileEntityRendering<DriveBlockEntity> rendering) {
// FIXME                               rendering.tileEntityRenderer(DriveLedTileEntityRenderer::new);
// FIXME                           }
// FIXME                       }).build())
// FIXME               .rendering(new DriveRendering()).build();
// FIXME       this.chest = registry.block("chest", ChestBlock::new).features(AEFeature.STORAGE_CELLS, AEFeature.ME_CHEST)
// FIXME               .tileEntity(registry.tileEntity("chest", ChestBlockEntity.class, ChestBlockEntity::new).build())
// FIXME               .rendering(new ChestRendering()).build();
// FIXME       this.iface = registry.block("interface", InterfaceBlock::new).features(AEFeature.INTERFACE)
// FIXME               .tileEntity(
// FIXME                       registry.tileEntity("interface", InterfaceBlockEntity.class, InterfaceBlockEntity::new).build())
// FIXME               .build();
// FIXME       this.fluidIface = registry.block("fluid_interface", FluidInterfaceBlock::new)
// FIXME               .features(AEFeature.FLUID_INTERFACE)
// FIXME               .tileEntity(registry
// FIXME                       .tileEntity("fluid_interface", FluidInterfaceBlockEntity.class, FluidInterfaceBlockEntity::new)
// FIXME                       .build())
// FIXME               .build();
// FIXME       this.cellWorkbench = registry.block("cell_workbench", CellWorkbenchBlock::new).features(AEFeature.STORAGE_CELLS)
// FIXME               .tileEntity(registry
// FIXME                       .tileEntity("cell_workbench", CellWorkbenchBlockEntity.class, CellWorkbenchBlockEntity::new)
// FIXME                       .build())
// FIXME               .build();
// FIXME       this.iOPort = registry.block("io_port", IOPortBlock::new).features(AEFeature.STORAGE_CELLS, AEFeature.IO_PORT)
// FIXME               .tileEntity(registry.tileEntity("io_port", IOPortBlockEntity.class, IOPortBlockEntity::new).build())
// FIXME               .build();
// FIXME       this.condenser = registry.block("condenser", CondenserBlock::new).features(AEFeature.CONDENSER)
// FIXME               .tileEntity(
// FIXME                       registry.tileEntity("condenser", CondenserBlockEntity.class, CondenserBlockEntity::new).build())
// FIXME               .build();
// FIXME       this.energyAcceptor = registry.block("energy_acceptor", EnergyAcceptorBlock::new)
// FIXME               .features(AEFeature.ENERGY_ACCEPTOR)
// FIXME               .tileEntity(registry
// FIXME                       .tileEntity("energy_acceptor", EnergyAcceptorBlockEntity.class, EnergyAcceptorBlockEntity::new)
// FIXME                       .build())
// FIXME               .build();
// FIXME       this.vibrationChamber = registry.block("vibration_chamber", VibrationChamberBlock::new)
// FIXME               .features(AEFeature.POWER_GEN).tileEntity(registry.tileEntity("vibration_chamber",
// FIXME                       VibrationChamberBlockEntity.class, VibrationChamberBlockEntity::new).build())
// FIXME               .build();
// FIXME       this.quartzGrowthAccelerator = registry.block("quartz_growth_accelerator", QuartzGrowthAcceleratorBlock::new)
// FIXME               .tileEntity(registry.tileEntity("quartz_growth_accelerator", QuartzGrowthAcceleratorBlockEntity.class,
// FIXME                       QuartzGrowthAcceleratorBlockEntity::new).build())
// FIXME               .features(AEFeature.CRYSTAL_GROWTH_ACCELERATOR).build();
// FIXME       this.energyCell = registry.block("energy_cell", EnergyCellBlock::new).features(AEFeature.ENERGY_CELLS)
// FIXME               .item(AEBaseBlockItemChargeable::new).tileEntity(registry
// FIXME                       .tileEntity("energy_cell", EnergyCellBlockEntity.class, EnergyCellBlockEntity::new).build())
// FIXME               .build();
// FIXME       this.energyCellDense = registry.block("dense_energy_cell", DenseEnergyCellBlock::new)
// FIXME               .features(AEFeature.ENERGY_CELLS, AEFeature.DENSE_ENERGY_CELLS).item(AEBaseBlockItemChargeable::new)
// FIXME               .tileEntity(registry.tileEntity("dense_energy_cell", DenseEnergyCellBlockEntity.class,
// FIXME                       DenseEnergyCellBlockEntity::new).build())
// FIXME               .build();
// FIXME       this.energyCellCreative = registry.block("creative_energy_cell", CreativeEnergyCellBlock::new)
// FIXME               .features(AEFeature.CREATIVE).tileEntity(registry.tileEntity("creative_energy_cell",
// FIXME                       CreativeEnergyCellBlockEntity.class, CreativeEnergyCellBlockEntity::new).build())
// FIXME               .build();
// FIXME
// FIXME       TileEntityDefinition craftingUnit = registry
// FIXME               .tileEntity("crafting_unit", CraftingBlockEntity.class, CraftingBlockEntity::new).build();
// FIXME
// FIXME       FeatureFactory crafting = registry.features(AEFeature.CRAFTING_CPU);
// FIXME       AbstractBlock.Settings craftingBlockProps = defaultProps(Material.METAL);
// FIXME       this.craftingUnit = crafting
// FIXME               .block("crafting_unit", () -> new CraftingUnitBlock(craftingBlockProps, CraftingUnitType.UNIT))
// FIXME               .rendering(new CraftingCubeRendering()).tileEntity(craftingUnit).build();
// FIXME       this.craftingAccelerator = crafting
// FIXME               .block("crafting_accelerator",
// FIXME                       () -> new CraftingUnitBlock(craftingBlockProps, CraftingUnitType.ACCELERATOR))
// FIXME               .rendering(new CraftingCubeRendering()).tileEntity(craftingUnit).build();
// FIXME
// FIXME       TileEntityDefinition craftingStorage = registry
// FIXME               .tileEntity("crafting_storage", CraftingStorageBlockEntity.class, CraftingStorageBlockEntity::new)
// FIXME               .build();
// FIXME       this.craftingStorage1k = crafting
// FIXME               .block("1k_crafting_storage",
// FIXME                       () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_1K))
// FIXME               .item(CraftingStorageItem::new).tileEntity(craftingStorage).rendering(new CraftingCubeRendering())
// FIXME               .build();
// FIXME       this.craftingStorage4k = crafting
// FIXME               .block("4k_crafting_storage",
// FIXME                       () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_4K))
// FIXME               .item(CraftingStorageItem::new).tileEntity(craftingStorage).rendering(new CraftingCubeRendering())
// FIXME               .build();
// FIXME       this.craftingStorage16k = crafting
// FIXME               .block("16k_crafting_storage",
// FIXME                       () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_16K))
// FIXME               .item(CraftingStorageItem::new).tileEntity(craftingStorage).rendering(new CraftingCubeRendering())
// FIXME               .build();
// FIXME       this.craftingStorage64k = crafting
// FIXME               .block("64k_crafting_storage",
// FIXME                       () -> new CraftingStorageBlock(craftingBlockProps, CraftingUnitType.STORAGE_64K))
// FIXME               .item(CraftingStorageItem::new).tileEntity(craftingStorage).rendering(new CraftingCubeRendering())
// FIXME               .build();
// FIXME       this.craftingMonitor = crafting.block("crafting_monitor", () -> new CraftingMonitorBlock(craftingBlockProps))
// FIXME               .tileEntity(registry
// FIXME                       .tileEntity("crafting_monitor", CraftingMonitorBlockEntity.class, CraftingMonitorBlockEntity::new)
// FIXME                       .rendering(new TileEntityRenderingCustomizer<CraftingMonitorBlockEntity>() {
// FIXME                           @Environment(EnvType.CLIENT)
// FIXME                           @Override
// FIXME                           public void customize(TileEntityRendering<CraftingMonitorBlockEntity> rendering) {
// FIXME                               rendering.tileEntityRenderer(CraftingMonitorTESR::new);
// FIXME                           }
// FIXME                       }).build())
// FIXME               .rendering(new BlockRenderingCustomizer() {
// FIXME                   @Override
// FIXME                   @Environment(EnvType.CLIENT)
// FIXME                   public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
// FIXME                       rendering.renderType(RenderLayer.getCutout());
// FIXME                       rendering.modelCustomizer((path, model) -> {
// FIXME                           // The formed model handles rotations itself, the unformed one does not
// FIXME                           if (model instanceof MonitorBakedModel) {
// FIXME                               return model;
// FIXME                           }
// FIXME                           return new AutoRotatingBakedModel(model);
// FIXME                       });
// FIXME                   }
// FIXME               }).build();
// FIXME
// FIXME       this.molecularAssembler = registry
// FIXME               .block("molecular_assembler", () -> new MolecularAssemblerBlock(defaultProps(Material.METAL).notSolid()))
// FIXME               .features(AEFeature.MOLECULAR_ASSEMBLER).rendering(new BlockRenderingCustomizer() {
// FIXME                   @Environment(EnvType.CLIENT)
// FIXME                   @Override
// FIXME                   public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
// FIXME                       rendering.renderType(RenderLayer.getCutout());
// FIXME                   }
// FIXME               })
// FIXME               .tileEntity(registry
// FIXME                       .tileEntity("molecular_assembler", MolecularAssemblerBlockEntity.class,
// FIXME                               MolecularAssemblerBlockEntity::new)
// FIXME                       .rendering(new TileEntityRenderingCustomizer<MolecularAssemblerBlockEntity>() {
// FIXME                           @Override
// FIXME                           @Environment(EnvType.CLIENT)
// FIXME                           public void customize(TileEntityRendering<MolecularAssemblerBlockEntity> rendering) {
// FIXME                               rendering.tileEntityRenderer(MolecularAssemblerRenderer::new);
// FIXME                           }
// FIXME                       }).build())
// FIXME               .build();

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
// FIXME        this.paint = registry
// FIXME                .block("paint", PaintSplotchesBlock::new).features(AEFeature.PAINT_BALLS).tileEntity(registry
// FIXME                        .tileEntity("paint", PaintSplotchesBlockEntity.class, PaintSplotchesBlockEntity::new).build())
// FIXME                .rendering(new PaintSplotchesRendering()).build();

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

// FIXME        this.multiPart = registry.block("cable_bus", CableBusBlock::new).rendering(new CableBusRendering())
// FIXME                .tileEntity(registry.tileEntity("cable_bus", CableBusBlockEntity.class, CableBusBlockEntity::new)
// FIXME                        .rendering(new TileEntityRenderingCustomizer<CableBusBlockEntity>() {
// FIXME                            @Override
// FIXME                            @Environment(EnvType.CLIENT)
// FIXME                            public void customize(TileEntityRendering<CableBusBlockEntity> rendering) {
// FIXME                                rendering.tileEntityRenderer(CableBusTESR::new);
// FIXME                            }
// FIXME                        }).build())
// FIXME                .build();

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
