/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import appeng.block.AEBaseTileBlock;
import appeng.core.AppEng;
import appeng.debug.ChunkLoaderTileEntity;
import appeng.debug.CubeGeneratorTileEntity;
import appeng.debug.EnergyGeneratorTileEntity;
import appeng.debug.ItemGenTileEntity;
import appeng.debug.PhantomNodeTileEntity;
import appeng.fluids.tile.FluidInterfaceTileEntity;
import appeng.tile.AEBaseTileEntity;
import appeng.tile.crafting.CraftingMonitorTileEntity;
import appeng.tile.crafting.CraftingStorageTileEntity;
import appeng.tile.crafting.CraftingTileEntity;
import appeng.tile.crafting.MolecularAssemblerTileEntity;
import appeng.tile.grindstone.CrankTileEntity;
import appeng.tile.grindstone.GrinderTileEntity;
import appeng.tile.misc.CellWorkbenchTileEntity;
import appeng.tile.misc.ChargerTileEntity;
import appeng.tile.misc.CondenserTileEntity;
import appeng.tile.misc.InscriberTileEntity;
import appeng.tile.misc.InterfaceTileEntity;
import appeng.tile.misc.LightDetectorTileEntity;
import appeng.tile.misc.PaintSplotchesTileEntity;
import appeng.tile.misc.QuartzGrowthAcceleratorTileEntity;
import appeng.tile.misc.SecurityStationTileEntity;
import appeng.tile.misc.SkyCompassTileEntity;
import appeng.tile.misc.VibrationChamberTileEntity;
import appeng.tile.networking.CableBusTileEntity;
import appeng.tile.networking.ControllerTileEntity;
import appeng.tile.networking.CreativeEnergyCellTileEntity;
import appeng.tile.networking.DenseEnergyCellTileEntity;
import appeng.tile.networking.EnergyAcceptorTileEntity;
import appeng.tile.networking.EnergyCellTileEntity;
import appeng.tile.networking.WirelessTileEntity;
import appeng.tile.qnb.QuantumBridgeTileEntity;
import appeng.tile.spatial.SpatialAnchorTileEntity;
import appeng.tile.spatial.SpatialIOPortTileEntity;
import appeng.tile.spatial.SpatialPylonTileEntity;
import appeng.tile.storage.ChestTileEntity;
import appeng.tile.storage.DriveTileEntity;
import appeng.tile.storage.IOPortTileEntity;
import appeng.tile.storage.SkyChestTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;

@SuppressWarnings("unused")
public final class AEBlockEntities {

    private static final Map<net.minecraft.resources.ResourceLocation, net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashMap<>();

    public static final net.minecraft.world.level.block.entity.BlockEntityType<GrinderTileEntity> GRINDSTONE = create("grindstone", GrinderTileEntity.class,
            GrinderTileEntity::new, AEBlocks.GRINDSTONE);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<CrankTileEntity> CRANK = create("crank", CrankTileEntity.class,
            CrankTileEntity::new, AEBlocks.CRANK);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<InscriberTileEntity> INSCRIBER = create("inscriber", InscriberTileEntity.class,
            InscriberTileEntity::new, AEBlocks.INSCRIBER);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<WirelessTileEntity> WIRELESS_ACCESS_POINT = create("wireless_access_point",
            WirelessTileEntity.class, WirelessTileEntity::new, AEBlocks.WIRELESS_ACCESS_POINT);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<ChargerTileEntity> CHARGER = create("charger", ChargerTileEntity.class,
            ChargerTileEntity::new, AEBlocks.CHARGER);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<SecurityStationTileEntity> SECURITY_STATION = create("security_station",
            SecurityStationTileEntity.class, SecurityStationTileEntity::new, AEBlocks.SECURITY_STATION);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<QuantumBridgeTileEntity> QUANTUM_BRIDGE = create("quantum_ring",
            QuantumBridgeTileEntity.class, QuantumBridgeTileEntity::new, AEBlocks.QUANTUM_RING,
            AEBlocks.QUANTUM_LINK);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<SpatialPylonTileEntity> SPATIAL_PYLON = create("spatial_pylon",
            SpatialPylonTileEntity.class, SpatialPylonTileEntity::new, AEBlocks.SPATIAL_PYLON);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<SpatialIOPortTileEntity> SPATIAL_IO_PORT = create("spatial_io_port",
            SpatialIOPortTileEntity.class, SpatialIOPortTileEntity::new, AEBlocks.SPATIAL_IO_PORT);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<SpatialAnchorTileEntity> SPATIAL_ANCHOR = create("spatial_anchor",
            SpatialAnchorTileEntity.class, SpatialAnchorTileEntity::new, AEBlocks.SPATIAL_ANCHOR);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<CableBusTileEntity> CABLE_BUS = create("cable_bus", CableBusTileEntity.class,
            CableBusTileEntity::new, AEBlocks.MULTI_PART);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<ControllerTileEntity> CONTROLLER = create("controller",
            ControllerTileEntity.class, ControllerTileEntity::new, AEBlocks.CONTROLLER);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<DriveTileEntity> DRIVE = create("drive", DriveTileEntity.class,
            DriveTileEntity::new, AEBlocks.DRIVE);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<ChestTileEntity> CHEST = create("chest", ChestTileEntity.class,
            ChestTileEntity::new, AEBlocks.CHEST);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<InterfaceTileEntity> INTERFACE = create("interface", InterfaceTileEntity.class,
            InterfaceTileEntity::new, AEBlocks.INTERFACE);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<FluidInterfaceTileEntity> FLUID_INTERFACE = create("fluid_interface",
            FluidInterfaceTileEntity.class, FluidInterfaceTileEntity::new, AEBlocks.FLUID_INTERFACE);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<CellWorkbenchTileEntity> CELL_WORKBENCH = create("cell_workbench",
            CellWorkbenchTileEntity.class, CellWorkbenchTileEntity::new, AEBlocks.CELL_WORKBENCH);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<IOPortTileEntity> IO_PORT = create("io_port", IOPortTileEntity.class,
            IOPortTileEntity::new, AEBlocks.IO_PORT);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<CondenserTileEntity> CONDENSER = create("condenser", CondenserTileEntity.class,
            CondenserTileEntity::new, AEBlocks.CONDENSER);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<EnergyAcceptorTileEntity> ENERGY_ACCEPTOR = create("energy_acceptor",
            EnergyAcceptorTileEntity.class, EnergyAcceptorTileEntity::new, AEBlocks.ENERGY_ACCEPTOR);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<VibrationChamberTileEntity> VIBRATION_CHAMBER = create("vibration_chamber",
            VibrationChamberTileEntity.class, VibrationChamberTileEntity::new, AEBlocks.VIBRATION_CHAMBER);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<QuartzGrowthAcceleratorTileEntity> QUARTZ_GROWTH_ACCELERATOR = create(
            "quartz_growth_accelerator", QuartzGrowthAcceleratorTileEntity.class,
            QuartzGrowthAcceleratorTileEntity::new, AEBlocks.QUARTZ_GROWTH_ACCELERATOR);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<EnergyCellTileEntity> ENERGY_CELL = create("energy_cell",
            EnergyCellTileEntity.class, EnergyCellTileEntity::new, AEBlocks.ENERGY_CELL);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<DenseEnergyCellTileEntity> DENSE_ENERGY_CELL = create("dense_energy_cell",
            DenseEnergyCellTileEntity.class, DenseEnergyCellTileEntity::new, AEBlocks.DENSE_ENERGY_CELL);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<CreativeEnergyCellTileEntity> CREATIVE_ENERGY_CELL = create(
            "creative_energy_cell",
            CreativeEnergyCellTileEntity.class, CreativeEnergyCellTileEntity::new, AEBlocks.CREATIVE_ENERGY_CELL);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<CraftingTileEntity> CRAFTING_UNIT = create("crafting_unit",
            CraftingTileEntity.class, CraftingTileEntity::new, AEBlocks.CRAFTING_UNIT,
            AEBlocks.CRAFTING_ACCELERATOR);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<CraftingStorageTileEntity> CRAFTING_STORAGE = create("crafting_storage",
            CraftingStorageTileEntity.class, CraftingStorageTileEntity::new, AEBlocks.CRAFTING_STORAGE_1K,
            AEBlocks.CRAFTING_STORAGE_4K, AEBlocks.CRAFTING_STORAGE_16K, AEBlocks.CRAFTING_STORAGE_64K);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<CraftingMonitorTileEntity> CRAFTING_MONITOR = create("crafting_monitor",
            CraftingMonitorTileEntity.class, CraftingMonitorTileEntity::new, AEBlocks.CRAFTING_MONITOR);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<MolecularAssemblerTileEntity> MOLECULAR_ASSEMBLER = create("molecular_assembler",
            MolecularAssemblerTileEntity.class, MolecularAssemblerTileEntity::new, AEBlocks.MOLECULAR_ASSEMBLER);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<LightDetectorTileEntity> LIGHT_DETECTOR = create("light_detector",
            LightDetectorTileEntity.class, LightDetectorTileEntity::new, AEBlocks.LIGHT_DETECTOR);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<PaintSplotchesTileEntity> PAINT = create("paint", PaintSplotchesTileEntity.class,
            PaintSplotchesTileEntity::new, AEBlocks.PAINT);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<SkyChestTileEntity> SKY_CHEST = create("sky_chest", SkyChestTileEntity.class,
            SkyChestTileEntity::new, AEBlocks.SKY_STONE_CHEST, AEBlocks.SMOOTH_SKY_STONE_CHEST);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<SkyCompassTileEntity> SKY_COMPASS = create("sky_compass",
            SkyCompassTileEntity.class, SkyCompassTileEntity::new, AEBlocks.SKY_COMPASS);

    public static final net.minecraft.world.level.block.entity.BlockEntityType<ItemGenTileEntity> DEBUG_ITEM_GEN = create("debug_item_gen",
            ItemGenTileEntity.class, ItemGenTileEntity::new, AEBlocks.DEBUG_ITEM_GEN);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<ChunkLoaderTileEntity> DEBUG_CHUNK_LOADER = create("debug_chunk_loader",
            ChunkLoaderTileEntity.class, ChunkLoaderTileEntity::new, AEBlocks.DEBUG_CHUNK_LOADER);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<PhantomNodeTileEntity> DEBUG_PHANTOM_NODE = create("debug_phantom_node",
            PhantomNodeTileEntity.class, PhantomNodeTileEntity::new, AEBlocks.DEBUG_PHANTOM_NODE);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<CubeGeneratorTileEntity> DEBUG_CUBE_GEN = create("debug_cube_gen",
            CubeGeneratorTileEntity.class, CubeGeneratorTileEntity::new, AEBlocks.DEBUG_CUBE_GEN);
    public static final net.minecraft.world.level.block.entity.BlockEntityType<EnergyGeneratorTileEntity> DEBUG_ENERGY_GEN = create("debug_energy_gen",
            EnergyGeneratorTileEntity.class, EnergyGeneratorTileEntity::new, AEBlocks.DEBUG_ENERGY_GEN);

    private AEBlockEntities() {
    }

    public static Map<ResourceLocation, net.minecraft.world.level.block.entity.BlockEntityType<?>> getBlockEntityTypes() {
        return ImmutableMap.copyOf(BLOCK_ENTITY_TYPES);
    }

    @SuppressWarnings("unchecked")
    private static <T extends AEBaseTileEntity> net.minecraft.world.level.block.entity.BlockEntityType<T> create(String shortId,
                                                                                                                 Class<T> entityClass,
                                                                                                                 Function<net.minecraft.world.level.block.entity.BlockEntityType<T>, T> factory,
                                                                                                                 BlockDefinition... blockDefinitions) {
        Preconditions.checkArgument(blockDefinitions.length > 0);

        net.minecraft.resources.ResourceLocation id = AppEng.makeId(shortId);

        net.minecraft.world.level.block.Block[] blocks = Arrays.stream(blockDefinitions).map(BlockDefinition::block).toArray(Block[]::new);

        AtomicReference<net.minecraft.world.level.block.entity.BlockEntityType<T>> typeHolder = new AtomicReference<>();
        Supplier<T> supplier = () -> factory.apply(typeHolder.get());
        net.minecraft.world.level.block.entity.BlockEntityType<T> type = Builder.of(supplier, blocks).build(null);
        type.setRegistryName(id);
        typeHolder.set(type); // Makes it available to the supplier used above
        BLOCK_ENTITY_TYPES.put(id, type);

        AEBaseTileEntity.registerTileItem(type, blockDefinitions[0].asItem());

        for (net.minecraft.world.level.block.Block block : blocks) {
            if (block instanceof AEBaseTileBlock) {
                AEBaseTileBlock<T> baseTileBlock = (AEBaseTileBlock<T>) block;
                baseTileBlock.setTileEntity(entityClass, supplier);
            }
        }

        return type;
    }

}
