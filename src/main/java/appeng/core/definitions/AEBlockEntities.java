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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Preconditions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.misc.CellWorkbenchBlockEntity;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.blockentity.misc.GrowthAcceleratorBlockEntity;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.blockentity.misc.LightDetectorBlockEntity;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.blockentity.networking.CreativeEnergyCellBlockEntity;
import appeng.blockentity.networking.CrystalResonanceGeneratorBlockEntity;
import appeng.blockentity.networking.EnergyAcceptorBlockEntity;
import appeng.blockentity.networking.EnergyCellBlockEntity;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.blockentity.spatial.SpatialAnchorBlockEntity;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.blockentity.storage.SkyStoneTankBlockEntity;
import appeng.core.AppEng;
import appeng.debug.CubeGeneratorBlockEntity;
import appeng.debug.EnergyGeneratorBlockEntity;
import appeng.debug.ItemGenBlockEntity;
import appeng.debug.PhantomNodeBlockEntity;

public final class AEBlockEntities {
    private static final List<DeferredBlockEntityType<?>> BLOCK_ENTITY_TYPES = new ArrayList<>();

    static final DeferredRegister<BlockEntityType<?>> DR = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,
            AppEng.MOD_ID);

    public static final DeferredBlockEntityType<InscriberBlockEntity> INSCRIBER = create("inscriber",
            InscriberBlockEntity.class,
            InscriberBlockEntity::new, AEBlocks.INSCRIBER);
    public static final DeferredBlockEntityType<WirelessAccessPointBlockEntity> WIRELESS_ACCESS_POINT = create(
            "wireless_access_point",
            WirelessAccessPointBlockEntity.class, WirelessAccessPointBlockEntity::new, AEBlocks.WIRELESS_ACCESS_POINT);
    public static final DeferredBlockEntityType<ChargerBlockEntity> CHARGER = create("charger",
            ChargerBlockEntity.class,
            ChargerBlockEntity::new, AEBlocks.CHARGER);
    public static final DeferredBlockEntityType<QuantumBridgeBlockEntity> QUANTUM_BRIDGE = create("quantum_ring",
            QuantumBridgeBlockEntity.class, QuantumBridgeBlockEntity::new, AEBlocks.QUANTUM_RING,
            AEBlocks.QUANTUM_LINK);
    public static final DeferredBlockEntityType<SpatialPylonBlockEntity> SPATIAL_PYLON = create("spatial_pylon",
            SpatialPylonBlockEntity.class, SpatialPylonBlockEntity::new, AEBlocks.SPATIAL_PYLON);
    public static final DeferredBlockEntityType<SpatialIOPortBlockEntity> SPATIAL_IO_PORT = create("spatial_io_port",
            SpatialIOPortBlockEntity.class, SpatialIOPortBlockEntity::new, AEBlocks.SPATIAL_IO_PORT);
    public static final DeferredBlockEntityType<SpatialAnchorBlockEntity> SPATIAL_ANCHOR = create("spatial_anchor",
            SpatialAnchorBlockEntity.class, SpatialAnchorBlockEntity::new, AEBlocks.SPATIAL_ANCHOR);
    public static final DeferredBlockEntityType<CableBusBlockEntity> CABLE_BUS = create("cable_bus",
            CableBusBlockEntity.class,
            CableBusBlockEntity::new, AEBlocks.CABLE_BUS);
    public static final DeferredBlockEntityType<ControllerBlockEntity> CONTROLLER = create("controller",
            ControllerBlockEntity.class, ControllerBlockEntity::new, AEBlocks.CONTROLLER);
    public static final DeferredBlockEntityType<DriveBlockEntity> DRIVE = create("drive", DriveBlockEntity.class,
            DriveBlockEntity::new, AEBlocks.DRIVE);
    public static final DeferredBlockEntityType<MEChestBlockEntity> ME_CHEST = create("chest", MEChestBlockEntity.class,
            MEChestBlockEntity::new, AEBlocks.ME_CHEST);
    public static final DeferredBlockEntityType<InterfaceBlockEntity> INTERFACE = create("interface",
            InterfaceBlockEntity.class, InterfaceBlockEntity::new, AEBlocks.INTERFACE);
    public static final DeferredBlockEntityType<CellWorkbenchBlockEntity> CELL_WORKBENCH = create("cell_workbench",
            CellWorkbenchBlockEntity.class, CellWorkbenchBlockEntity::new, AEBlocks.CELL_WORKBENCH);
    public static final DeferredBlockEntityType<IOPortBlockEntity> IO_PORT = create("io_port", IOPortBlockEntity.class,
            IOPortBlockEntity::new, AEBlocks.IO_PORT);
    public static final DeferredBlockEntityType<CondenserBlockEntity> CONDENSER = create("condenser",
            CondenserBlockEntity.class,
            CondenserBlockEntity::new, AEBlocks.CONDENSER);
    public static final DeferredBlockEntityType<EnergyAcceptorBlockEntity> ENERGY_ACCEPTOR = create("energy_acceptor",
            EnergyAcceptorBlockEntity.class, EnergyAcceptorBlockEntity::new, AEBlocks.ENERGY_ACCEPTOR);
    public static final DeferredBlockEntityType<CrystalResonanceGeneratorBlockEntity> CRYSTAL_RESONANCE_GENERATOR = create(
            "crystal_resonance_generator",
            CrystalResonanceGeneratorBlockEntity.class, CrystalResonanceGeneratorBlockEntity::new,
            AEBlocks.CRYSTAL_RESONANCE_GENERATOR);
    public static final DeferredBlockEntityType<VibrationChamberBlockEntity> VIBRATION_CHAMBER = create(
            "vibration_chamber",
            VibrationChamberBlockEntity.class, VibrationChamberBlockEntity::new, AEBlocks.VIBRATION_CHAMBER);
    public static final DeferredBlockEntityType<GrowthAcceleratorBlockEntity> GROWTH_ACCELERATOR = create(
            "growth_accelerator", GrowthAcceleratorBlockEntity.class,
            GrowthAcceleratorBlockEntity::new, AEBlocks.GROWTH_ACCELERATOR);
    public static final DeferredBlockEntityType<EnergyCellBlockEntity> ENERGY_CELL = create("energy_cell",
            EnergyCellBlockEntity.class, EnergyCellBlockEntity::new, AEBlocks.ENERGY_CELL);
    public static final DeferredBlockEntityType<EnergyCellBlockEntity> DENSE_ENERGY_CELL = create("dense_energy_cell",
            EnergyCellBlockEntity.class, EnergyCellBlockEntity::new, AEBlocks.DENSE_ENERGY_CELL);
    public static final DeferredBlockEntityType<CreativeEnergyCellBlockEntity> CREATIVE_ENERGY_CELL = create(
            "creative_energy_cell",
            CreativeEnergyCellBlockEntity.class, CreativeEnergyCellBlockEntity::new, AEBlocks.CREATIVE_ENERGY_CELL);
    public static final DeferredBlockEntityType<CraftingBlockEntity> CRAFTING_UNIT = create("crafting_unit",
            CraftingBlockEntity.class, CraftingBlockEntity::new, AEBlocks.CRAFTING_UNIT,
            AEBlocks.CRAFTING_ACCELERATOR);
    public static final DeferredBlockEntityType<CraftingBlockEntity> CRAFTING_STORAGE = create("crafting_storage",
            CraftingBlockEntity.class, CraftingBlockEntity::new, AEBlocks.CRAFTING_STORAGE_1K,
            AEBlocks.CRAFTING_STORAGE_4K, AEBlocks.CRAFTING_STORAGE_16K, AEBlocks.CRAFTING_STORAGE_64K,
            AEBlocks.CRAFTING_STORAGE_256K);
    public static final DeferredBlockEntityType<CraftingMonitorBlockEntity> CRAFTING_MONITOR = create(
            "crafting_monitor",
            CraftingMonitorBlockEntity.class, CraftingMonitorBlockEntity::new, AEBlocks.CRAFTING_MONITOR);
    public static final DeferredBlockEntityType<PatternProviderBlockEntity> PATTERN_PROVIDER = create(
            "pattern_provider",
            PatternProviderBlockEntity.class, PatternProviderBlockEntity::new, AEBlocks.PATTERN_PROVIDER);
    public static final DeferredBlockEntityType<MolecularAssemblerBlockEntity> MOLECULAR_ASSEMBLER = create(
            "molecular_assembler",
            MolecularAssemblerBlockEntity.class, MolecularAssemblerBlockEntity::new, AEBlocks.MOLECULAR_ASSEMBLER);
    public static final DeferredBlockEntityType<LightDetectorBlockEntity> LIGHT_DETECTOR = create("light_detector",
            LightDetectorBlockEntity.class, LightDetectorBlockEntity::new, AEBlocks.LIGHT_DETECTOR);
    public static final DeferredBlockEntityType<PaintSplotchesBlockEntity> PAINT = create("paint",
            PaintSplotchesBlockEntity.class,
            PaintSplotchesBlockEntity::new, AEBlocks.PAINT);
    public static final DeferredBlockEntityType<SkyChestBlockEntity> SKY_CHEST = create("sky_chest",
            SkyChestBlockEntity.class,
            SkyChestBlockEntity::new, AEBlocks.SKY_STONE_CHEST, AEBlocks.SMOOTH_SKY_STONE_CHEST);

    public static final DeferredBlockEntityType<SkyStoneTankBlockEntity> SKY_STONE_TANK = create("sky_tank",
            SkyStoneTankBlockEntity.class,
            SkyStoneTankBlockEntity::new, AEBlocks.SKY_STONE_TANK);

    public static final DeferredBlockEntityType<ItemGenBlockEntity> DEBUG_ITEM_GEN = create("debug_item_gen",
            ItemGenBlockEntity.class, ItemGenBlockEntity::new, AEBlocks.DEBUG_ITEM_GEN);
    public static final DeferredBlockEntityType<PhantomNodeBlockEntity> DEBUG_PHANTOM_NODE = create(
            "debug_phantom_node",
            PhantomNodeBlockEntity.class, PhantomNodeBlockEntity::new, AEBlocks.DEBUG_PHANTOM_NODE);
    public static final DeferredBlockEntityType<CubeGeneratorBlockEntity> DEBUG_CUBE_GEN = create("debug_cube_gen",
            CubeGeneratorBlockEntity.class, CubeGeneratorBlockEntity::new, AEBlocks.DEBUG_CUBE_GEN);
    public static final DeferredBlockEntityType<EnergyGeneratorBlockEntity> DEBUG_ENERGY_GEN = create(
            "debug_energy_gen",
            EnergyGeneratorBlockEntity.class, EnergyGeneratorBlockEntity::new, AEBlocks.DEBUG_ENERGY_GEN);

    public static final DeferredBlockEntityType<CrankBlockEntity> CRANK = create("crank", CrankBlockEntity.class,
            CrankBlockEntity::new, AEBlocks.CRANK);

    private AEBlockEntities() {
    }

    /**
     * Get all block entity types whose implementations extends the given base class.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> List<BlockEntityType<? extends T>> getSubclassesOf(Class<T> baseClass) {
        var result = new ArrayList<BlockEntityType<? extends T>>();
        for (var type : BLOCK_ENTITY_TYPES) {
            if (baseClass.isAssignableFrom(type.getBlockEntityClass())) {
                result.add((BlockEntityType<? extends T>) type.get());
            }
        }
        return result;
    }

    /**
     * Get all block entity types whose implementations implement the given interface.
     */
    public static List<BlockEntityType<?>> getImplementorsOf(Class<?> iface) {
        var result = new ArrayList<BlockEntityType<?>>();
        for (var type : BLOCK_ENTITY_TYPES) {
            if (iface.isAssignableFrom(type.getBlockEntityClass())) {
                result.add(type.get());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    private static <T extends AEBaseBlockEntity> DeferredBlockEntityType<T> create(String shortId,
            Class<T> entityClass,
            BlockEntityFactory<T> factory,
            BlockDefinition<? extends AEBaseEntityBlock<?>>... blockDefinitions) {
        Preconditions.checkArgument(blockDefinitions.length > 0);

        ResourceLocation id = AppEng.makeId(shortId);

        var blocks = Arrays.stream(blockDefinitions)
                .map(BlockDefinition::block)
                .toArray(AEBaseEntityBlock[]::new);

        var deferred = DR.register(shortId, () -> {
            AtomicReference<BlockEntityType<T>> typeHolder = new AtomicReference<>();
            BlockEntityType.BlockEntitySupplier<T> supplier = (blockPos, blockState) -> factory.create(typeHolder.get(),
                    blockPos, blockState);
            var type = BlockEntityType.Builder.of(supplier, blocks).build(null);
            typeHolder.setPlain(type); // Makes it available to the supplier used above

            AEBaseBlockEntity.registerBlockEntityItem(type, blockDefinitions[0].asItem());

            // If the block entity classes implement specific interfaces, automatically register them
            // as tickers with the blocks that create that entity.
            BlockEntityTicker<T> serverTicker = null;
            if (ServerTickingBlockEntity.class.isAssignableFrom(entityClass)) {
                serverTicker = (level, pos, state, entity) -> {
                    ((ServerTickingBlockEntity) entity).serverTick();
                };
            }
            BlockEntityTicker<T> clientTicker = null;
            if (ClientTickingBlockEntity.class.isAssignableFrom(entityClass)) {
                clientTicker = (level, pos, state, entity) -> {
                    ((ClientTickingBlockEntity) entity).clientTick();
                };
            }

            for (var block : blocks) {
                AEBaseEntityBlock<T> baseBlock = (AEBaseEntityBlock<T>) block;
                baseBlock.setBlockEntity(entityClass, type, clientTicker, serverTicker);
            }

            return type;
        });

        var result = new DeferredBlockEntityType<>(entityClass, deferred);
        BLOCK_ENTITY_TYPES.add(result);
        return result;
    }

    @FunctionalInterface
    interface BlockEntityFactory<T extends AEBaseBlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
}
