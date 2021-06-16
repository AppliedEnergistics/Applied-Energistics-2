package appeng.core.api.definitions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;

import appeng.block.AEBaseTileBlock;
import appeng.core.AppEng;
import appeng.core.features.BlockDefinition;
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

public final class ApiBlockEntities {

    public static final Map<ResourceLocation, TileEntityType<?>> TILE_ENTITY_TYPES = new HashMap<>();
    public static final TileEntityType<GrinderTileEntity> grindstone = create("grindstone", GrinderTileEntity.class,
            GrinderTileEntity::new, ApiBlocks.GRINDSTONE);
    public static final TileEntityType<CrankTileEntity> crank = create("crank", CrankTileEntity.class,
            CrankTileEntity::new, ApiBlocks.CRANK);
    public static final TileEntityType<InscriberTileEntity> inscriber = create("inscriber", InscriberTileEntity.class,
            InscriberTileEntity::new, ApiBlocks.INSCRIBER);
    public static final TileEntityType<WirelessTileEntity> wirelessAccessPoint = create("wireless_access_point",
            WirelessTileEntity.class, WirelessTileEntity::new, ApiBlocks.WIRELESS_ACCESS_POINT);
    public static final TileEntityType<ChargerTileEntity> charger = create("charger", ChargerTileEntity.class,
            ChargerTileEntity::new, ApiBlocks.CHARGER);
    public static final TileEntityType<SecurityStationTileEntity> securityStation = create("security_station",
            SecurityStationTileEntity.class, SecurityStationTileEntity::new, ApiBlocks.SECURITY_STATION);
    public static final TileEntityType<QuantumBridgeTileEntity> QUANTUM_BRIDGE = create("quantum_ring",
            QuantumBridgeTileEntity.class, QuantumBridgeTileEntity::new, ApiBlocks.QUANTUM_RING,
            ApiBlocks.QUANTUM_LINK);
    public static final TileEntityType<SpatialPylonTileEntity> spatialPylon = create("spatial_pylon",
            SpatialPylonTileEntity.class, SpatialPylonTileEntity::new, ApiBlocks.SPATIAL_PYLON);
    public static final TileEntityType<SpatialIOPortTileEntity> spatialIOPort = create("spatial_io_port",
            SpatialIOPortTileEntity.class, SpatialIOPortTileEntity::new, ApiBlocks.SPATIAL_IO_PORT);
    public static final TileEntityType<SpatialAnchorTileEntity> spatialAnchor = create("spatial_anchor",
            SpatialAnchorTileEntity.class, SpatialAnchorTileEntity::new, ApiBlocks.SPATIAL_ANCHOR);
    public static final TileEntityType<CableBusTileEntity> multiPart = create("cable_bus", CableBusTileEntity.class,
            CableBusTileEntity::new, ApiBlocks.MULTI_PART);
    public static final TileEntityType<ControllerTileEntity> controller = create("controller",
            ControllerTileEntity.class, ControllerTileEntity::new, ApiBlocks.CONTROLLER);
    public static final TileEntityType<DriveTileEntity> drive = create("drive", DriveTileEntity.class,
            DriveTileEntity::new, ApiBlocks.DRIVE);
    public static final TileEntityType<ChestTileEntity> chest = create("chest", ChestTileEntity.class,
            ChestTileEntity::new, ApiBlocks.CHEST);
    public static final TileEntityType<InterfaceTileEntity> iface = create("interface", InterfaceTileEntity.class,
            InterfaceTileEntity::new, ApiBlocks.INTERFACE);
    public static final TileEntityType<FluidInterfaceTileEntity> fluidIface = create("fluid_interface",
            FluidInterfaceTileEntity.class, FluidInterfaceTileEntity::new, ApiBlocks.FLUID_INTERFACE);
    public static final TileEntityType<CellWorkbenchTileEntity> cellWorkbench = create("cell_workbench",
            CellWorkbenchTileEntity.class, CellWorkbenchTileEntity::new, ApiBlocks.CELL_WORKBENCH);
    public static final TileEntityType<IOPortTileEntity> iOPort = create("io_port", IOPortTileEntity.class,
            IOPortTileEntity::new, ApiBlocks.IO_PORT);
    public static final TileEntityType<CondenserTileEntity> condenser = create("condenser", CondenserTileEntity.class,
            CondenserTileEntity::new, ApiBlocks.CONDENSER);
    public static final TileEntityType<EnergyAcceptorTileEntity> energyAcceptor = create("energy_acceptor",
            EnergyAcceptorTileEntity.class, EnergyAcceptorTileEntity::new, ApiBlocks.ENERGY_ACCEPTOR);
    public static final TileEntityType<VibrationChamberTileEntity> vibrationChamber = create("vibration_chamber",
            VibrationChamberTileEntity.class, VibrationChamberTileEntity::new, ApiBlocks.VIBRATION_CHAMBER);
    public static final TileEntityType<QuartzGrowthAcceleratorTileEntity> quartzGrowthAccelerator = create(
            "quartz_growth_accelerator", QuartzGrowthAcceleratorTileEntity.class,
            QuartzGrowthAcceleratorTileEntity::new, ApiBlocks.QUARTZ_GROWTH_ACCELERATOR);
    public static final TileEntityType<EnergyCellTileEntity> energyCell = create("energy_cell",
            EnergyCellTileEntity.class, EnergyCellTileEntity::new, ApiBlocks.ENERGY_CELL);
    public static final TileEntityType<DenseEnergyCellTileEntity> energyCellDense = create("dense_energy_cell",
            DenseEnergyCellTileEntity.class, DenseEnergyCellTileEntity::new, ApiBlocks.DENSE_ENERGY_CELL);
    public static final TileEntityType<CreativeEnergyCellTileEntity> energyCellCreative = create("creative_energy_cell",
            CreativeEnergyCellTileEntity.class, CreativeEnergyCellTileEntity::new, ApiBlocks.CREATIVE_ENERGY_CELL);
    public static final TileEntityType<CraftingTileEntity> craftingUnit = create("crafting_unit",
            CraftingTileEntity.class, CraftingTileEntity::new, ApiBlocks.CRAFTING_UNIT,
            ApiBlocks.CRAFTING_ACCELERATOR);
    public static final TileEntityType<CraftingStorageTileEntity> craftingStorage = create("crafting_storage",
            CraftingStorageTileEntity.class, CraftingStorageTileEntity::new, ApiBlocks.CRAFTING_STORAGE_1K,
            ApiBlocks.CRAFTING_STORAGE_4K, ApiBlocks.CRAFTING_STORAGE_16K, ApiBlocks.CRAFTING_STORAGE_64K);
    public static final TileEntityType<CraftingMonitorTileEntity> craftingMonitor = create("crafting_monitor",
            CraftingMonitorTileEntity.class, CraftingMonitorTileEntity::new, ApiBlocks.CRAFTING_MONITOR);
    public static final TileEntityType<MolecularAssemblerTileEntity> molecularAssembler = create("molecular_assembler",
            MolecularAssemblerTileEntity.class, MolecularAssemblerTileEntity::new, ApiBlocks.MOLECULAR_ASSEMBLER);
    public static final TileEntityType<LightDetectorTileEntity> lightDetector = create("light_detector",
            LightDetectorTileEntity.class, LightDetectorTileEntity::new, ApiBlocks.LIGHT_DETECTOR);
    public static final TileEntityType<PaintSplotchesTileEntity> paint = create("paint", PaintSplotchesTileEntity.class,
            PaintSplotchesTileEntity::new, ApiBlocks.PAINT);
    public static final TileEntityType<SkyChestTileEntity> SKY_CHEST = create("sky_chest", SkyChestTileEntity.class,
            SkyChestTileEntity::new, ApiBlocks.SKY_STONE_CHEST, ApiBlocks.SMOOTH_SKY_STONE_CHEST);
    public static final TileEntityType<SkyCompassTileEntity> SKY_COMPASS = create("sky_compass",
            SkyCompassTileEntity.class, SkyCompassTileEntity::new, ApiBlocks.SKY_COMPASS);

    public static final TileEntityType<ItemGenTileEntity> DEBUG_ITEM_GEN = create("debug_item_gen",
            ItemGenTileEntity.class, ItemGenTileEntity::new, ApiBlocks.DEBUG_ITEM_GEN);
    public static final TileEntityType<ChunkLoaderTileEntity> DEBUG_CHUNK_LOADER = create("debug_chunk_loader",
            ChunkLoaderTileEntity.class, ChunkLoaderTileEntity::new, ApiBlocks.DEBUG_CHUNK_LOADER);
    public static final TileEntityType<PhantomNodeTileEntity> DEBUG_PHANTOM_NODE = create("debug_phantom_node",
            PhantomNodeTileEntity.class, PhantomNodeTileEntity::new, ApiBlocks.DEBUG_PHANTOM_NODE);
    public static final TileEntityType<CubeGeneratorTileEntity> DEBUG_CUBE_GEN = create("debug_cube_gen",
            CubeGeneratorTileEntity.class, CubeGeneratorTileEntity::new, ApiBlocks.DEBUG_CUBE_GEN);
    public static final TileEntityType<EnergyGeneratorTileEntity> DEBUG_ENERGY_GEN = create("debug_energy_gen",
            EnergyGeneratorTileEntity.class, EnergyGeneratorTileEntity::new, ApiBlocks.DEBUG_ENERGY_GEN);

    private ApiBlockEntities() {
    }

    public static Map<ResourceLocation, TileEntityType<?>> getTileEntityTypes() {
        return ImmutableMap.copyOf(TILE_ENTITY_TYPES);
    }

    @SuppressWarnings("unchecked")
    private static <T extends AEBaseTileEntity> TileEntityType<T> create(String shortId,
            Class<T> entityClass,
            Function<TileEntityType<T>, T> factory,
            BlockDefinition... blockDefinitions) {
        Preconditions.checkArgument(blockDefinitions.length > 0);

        ResourceLocation id = AppEng.makeId(shortId);

        Block[] blocks = Arrays.stream(blockDefinitions).map(BlockDefinition::block).toArray(Block[]::new);

        AtomicReference<TileEntityType<T>> typeHolder = new AtomicReference<>();
        Supplier<T> supplier = () -> factory.apply(typeHolder.get());
        TileEntityType<T> type = TileEntityType.Builder.create(supplier, blocks).build(null);
        type.setRegistryName(id);
        typeHolder.set(type); // Makes it available to the supplier used above
        TILE_ENTITY_TYPES.put(id, type);

        AEBaseTileEntity.registerTileItem(type, blockDefinitions[0].blockItem());

        for (Block block : blocks) {
            if (block instanceof AEBaseTileBlock) {
                AEBaseTileBlock<T> baseTileBlock = (AEBaseTileBlock<T>) block;
                baseTileBlock.setTileEntity(entityClass, supplier);
            }
        }

        return type;
    }

}
