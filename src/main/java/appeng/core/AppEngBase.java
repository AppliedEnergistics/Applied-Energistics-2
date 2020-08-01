package appeng.core;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IParts;
import appeng.api.features.AEFeature;
import appeng.api.features.IRegistryContainer;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWorldGen;
import appeng.api.movable.IMovableRegistry;
import appeng.api.networking.IGridCacheRegistry;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.parts.CableRenderMode;
import appeng.bootstrap.IBootstrapComponent;
import appeng.bootstrap.components.ITileEntityRegistrationComponent;
import appeng.client.render.effects.ParticleTypes;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpener;
import appeng.container.implementations.CellWorkbenchContainer;
import appeng.container.implementations.ChestContainer;
import appeng.container.implementations.CondenserContainer;
import appeng.container.implementations.CraftAmountContainer;
import appeng.container.implementations.CraftConfirmContainer;
import appeng.container.implementations.CraftingCPUContainer;
import appeng.container.implementations.CraftingStatusContainer;
import appeng.container.implementations.CraftingTermContainer;
import appeng.container.implementations.DriveContainer;
import appeng.container.implementations.FormationPlaneContainer;
import appeng.container.implementations.GrinderContainer;
import appeng.container.implementations.IOPortContainer;
import appeng.container.implementations.InscriberContainer;
import appeng.container.implementations.InterfaceContainer;
import appeng.container.implementations.InterfaceTerminalContainer;
import appeng.container.implementations.LevelEmitterContainer;
import appeng.container.implementations.MEMonitorableContainer;
import appeng.container.implementations.MEPortableCellContainer;
import appeng.container.implementations.MolecularAssemblerContainer;
import appeng.container.implementations.NetworkStatusContainer;
import appeng.container.implementations.NetworkToolContainer;
import appeng.container.implementations.PatternTermContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.container.implementations.QNBContainer;
import appeng.container.implementations.QuartzKnifeContainer;
import appeng.container.implementations.SecurityStationContainer;
import appeng.container.implementations.SkyChestContainer;
import appeng.container.implementations.SpatialIOPortContainer;
import appeng.container.implementations.StorageBusContainer;
import appeng.container.implementations.UpgradeableContainer;
import appeng.container.implementations.VibrationChamberContainer;
import appeng.container.implementations.WirelessContainer;
import appeng.container.implementations.WirelessTermContainer;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.features.registries.cell.BasicCellHandler;
import appeng.core.features.registries.cell.BasicItemCellGuiHandler;
import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.core.localization.GuiText;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.AeStats;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.network.TargetPoint;
import appeng.fluids.container.FluidFormationPlaneContainer;
import appeng.fluids.container.FluidIOContainer;
import appeng.fluids.container.FluidInterfaceContainer;
import appeng.fluids.container.FluidLevelEmitterContainer;
import appeng.fluids.container.FluidStorageBusContainer;
import appeng.fluids.container.FluidTerminalContainer;
import appeng.fluids.registries.BasicFluidCellGuiHandler;
import appeng.hooks.RegisterDimensionTypeCallback;
import appeng.hooks.ToolItemHook;
import appeng.items.parts.FacadeItem;
import appeng.items.tools.NetworkToolItem;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cache.EnergyGridCache;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.P2PCache;
import appeng.me.cache.PathGridCache;
import appeng.me.cache.SecurityCache;
import appeng.me.cache.SpatialPylonCache;
import appeng.me.cache.TickManagerCache;
import appeng.mixins.CriteriaRegisterMixin;
import appeng.recipes.game.DisassembleRecipe;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.handlers.GrinderRecipe;
import appeng.recipes.handlers.GrinderRecipeSerializer;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.handlers.InscriberRecipeSerializer;
import appeng.recipes.handlers.QuartzKnifeRecipeSerializer;
import appeng.server.AECommand;
import appeng.spatial.SpatialDimensionManager;
import appeng.spatial.StorageCellBiome;
import appeng.spatial.StorageChunkGenerator;
import appeng.tile.AEBaseBlockEntity;
import appeng.worldgen.BiomeModifier;
import appeng.worldgen.ChargedQuartzOreConfig;
import appeng.worldgen.ChargedQuartzOreFeature;
import appeng.worldgen.meteorite.MeteoriteStructure;
import net.earthcomputer.libstructure.LibStructure;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.NopeDecoratorConfig;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;

import java.util.OptionalLong;
import java.util.function.Consumer;

public abstract class AppEngBase implements AppEng {

    protected AdvancementTriggers advancementTriggers;

    // WTF is this doing? Should this be a ThreadLocal???
    private PlayerEntity renderModeBased;

    public AppEngBase() {
        if (AppEng.instance() != null) {
            throw new IllegalStateException();
        }

        AEConfig.load(FabricLoader.getInstance().getConfigDirectory());

        CreativeTab.init();
        FacadeCreativeTab.init();// This call has a side-effect (adding it to the creative screen)

        AeStats.register();
        advancementTriggers = new AdvancementTriggers(CriteriaRegisterMixin::callRegister);

        ToolItemHook.install();

        Api.INSTANCE = new Api();
        registerBlockEntities();

        registerScreenHandlerTypes();
        registerParticleTypes();
        registerRecipeTypes();
        registerRecipeSerializers();
        registerWorldGen();
        registerServerCommands();
        registerDimension();

        setupInternalRegistries();

    }

    public static void setupInternalRegistries() {
        // TODO: Do not use the internal API
        final Api api = Api.INSTANCE;
        final IRegistryContainer registries = api.registries();

        final IGridCacheRegistry gcr = registries.gridCache();
        gcr.registerGridCache(ITickManager.class, TickManagerCache::new);
        gcr.registerGridCache(IEnergyGrid.class, EnergyGridCache::new);
        gcr.registerGridCache(IPathingGrid.class, PathGridCache::new);
        gcr.registerGridCache(IStorageGrid.class, GridStorageCache::new);
        gcr.registerGridCache(P2PCache.class, P2PCache::new);
        gcr.registerGridCache(ISpatialCache.class, SpatialPylonCache::new);
        gcr.registerGridCache(ISecurityGrid.class, SecurityCache::new);
        gcr.registerGridCache(ICraftingGrid.class, CraftingGridCache::new);

        registries.cell().addCellHandler(new BasicCellHandler());
        registries.cell().addCellHandler(new CreativeCellHandler());
        registries.cell().addCellGuiHandler(new BasicItemCellGuiHandler());
        registries.cell().addCellGuiHandler(new BasicFluidCellGuiHandler());

        registries.matterCannon().registerAmmoItem(api.definitions().materials().matterBall().item(), 32);

        // TODO: Do not use the internal API
        ApiDefinitions definitions = Api.INSTANCE.definitions();
        final IParts parts = definitions.parts();
        final IBlocks blocks = definitions.blocks();
        final IItems items = definitions.items();

        // Block and part interface have different translation keys, but support the
        // same upgrades
        String interfaceGroup = parts.iface().asItem().getTranslationKey();
        String itemIoBusGroup = GuiText.IOBuses.getTranslationKey();
        String fluidIoBusGroup = GuiText.IOBusesFluids.getTranslationKey();
        String storageCellGroup = GuiText.IOBusesFluids.getTranslationKey();

        // default settings..
        ((P2PTunnelRegistry) registries.p2pTunnel()).configure();

        // Interface
        Upgrades.CRAFTING.registerItem(parts.iface(), 1, interfaceGroup);
        Upgrades.CRAFTING.registerItem(blocks.iface(), 1, interfaceGroup);

        // IO Port!
        Upgrades.SPEED.registerItem(blocks.iOPort(), 3);
        Upgrades.REDSTONE.registerItem(blocks.iOPort(), 1);

        // Level Emitter!
        Upgrades.FUZZY.registerItem(parts.levelEmitter(), 1);
        Upgrades.CRAFTING.registerItem(parts.levelEmitter(), 1);

        // Import Bus
        Upgrades.FUZZY.registerItem(parts.importBus(), 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(parts.importBus(), 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(parts.importBus(), 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(parts.importBus(), 4, itemIoBusGroup);

        // Fluid Import Bus
        Upgrades.CAPACITY.registerItem(parts.fluidImportBus(), 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(parts.fluidImportBus(), 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(parts.fluidImportBus(), 4, fluidIoBusGroup);

        // Export Bus
        Upgrades.FUZZY.registerItem(parts.exportBus(), 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(parts.exportBus(), 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(parts.exportBus(), 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(parts.exportBus(), 4, itemIoBusGroup);
        Upgrades.CRAFTING.registerItem(parts.exportBus(), 1, itemIoBusGroup);

        // Fluid Export Bus
        Upgrades.CAPACITY.registerItem(parts.fluidExportBus(), 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(parts.fluidExportBus(), 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(parts.fluidExportBus(), 4, fluidIoBusGroup);

        // Storage Cells
        Upgrades.FUZZY.registerItem(items.cell1k(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.cell1k(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.cell4k(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.cell4k(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.cell16k(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.cell16k(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.cell64k(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.cell64k(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.portableCell(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.portableCell(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.viewCell(), 1);
        Upgrades.INVERTER.registerItem(items.viewCell(), 1);

        // Storage Bus
        Upgrades.FUZZY.registerItem(parts.storageBus(), 1);
        Upgrades.INVERTER.registerItem(parts.storageBus(), 1);
        Upgrades.CAPACITY.registerItem(parts.storageBus(), 5);

        // Storage Bus Fluids
        Upgrades.INVERTER.registerItem(parts.fluidStorageBus(), 1);
        Upgrades.CAPACITY.registerItem(parts.fluidStorageBus(), 5);

        // Formation Plane
        Upgrades.FUZZY.registerItem(parts.formationPlane(), 1);
        Upgrades.INVERTER.registerItem(parts.formationPlane(), 1);
        Upgrades.CAPACITY.registerItem(parts.formationPlane(), 5);

        // Matter Cannon
        Upgrades.FUZZY.registerItem(items.massCannon(), 1);
        Upgrades.INVERTER.registerItem(items.massCannon(), 1);
        Upgrades.SPEED.registerItem(items.massCannon(), 4);

        // Molecular Assembler
        Upgrades.SPEED.registerItem(blocks.molecularAssembler(), 5);

        // Inscriber
        Upgrades.SPEED.registerItem(blocks.inscriber(), 3);

        // Wireless Terminal Handler
        items.wirelessTerminal().maybeItem()
                .ifPresent(terminal -> registries.wireless().registerWirelessHandler((IWirelessTermHandler) terminal));

        // Charge Rates
        items.chargedStaff().maybeItem()
                .ifPresent(chargedStaff -> registries.charger().addChargeRate(chargedStaff, 320d));
        items.portableCell().maybeItem()
                .ifPresent(chargedStaff -> registries.charger().addChargeRate(chargedStaff, 800d));
        items.colorApplicator().maybeItem()
                .ifPresent(colorApplicator -> registries.charger().addChargeRate(colorApplicator, 800d));
        items.wirelessTerminal().maybeItem().ifPresent(terminal -> registries.charger().addChargeRate(terminal, 8000d));
        items.entropyManipulator().maybeItem()
                .ifPresent(entropyManipulator -> registries.charger().addChargeRate(entropyManipulator, 8000d));
        items.massCannon().maybeItem().ifPresent(massCannon -> registries.charger().addChargeRate(massCannon, 8000d));
        blocks.energyCell().maybeItem().ifPresent(cell -> registries.charger().addChargeRate(cell, 8000d));
        blocks.energyCellDense().maybeItem().ifPresent(cell -> registries.charger().addChargeRate(cell, 16000d));

// FIXME		// add villager trading to black smiths for a few basic materials
// FIXME		if( AEConfig.instance().isFeatureEnabled( AEFeature.VILLAGER_TRADING ) )
// FIXME		{
// FIXME			// TODO: VILLAGER TRADING
// FIXME			// VillagerRegistry.instance().getRegisteredVillagers().registerVillageTradeHandler( 3, new AETrading() );
// FIXME		}

        final IMovableRegistry mr = registries.movable();

        /*
         * You can't move bed rock.
         */
        mr.blacklistBlock(net.minecraft.block.Blocks.BEDROCK);

        /*
         * White List Vanilla...
         */
        mr.whiteListBlockEntity(BannerBlockEntity.class);
        mr.whiteListBlockEntity(BeaconBlockEntity.class);
        mr.whiteListBlockEntity(BrewingStandBlockEntity.class);
        mr.whiteListBlockEntity(ChestBlockEntity.class);
        mr.whiteListBlockEntity(CommandBlockBlockEntity.class);
        mr.whiteListBlockEntity(ComparatorBlockEntity.class);
        mr.whiteListBlockEntity(DaylightDetectorBlockEntity.class);
        mr.whiteListBlockEntity(DispenserBlockEntity.class);
        mr.whiteListBlockEntity(DropperBlockEntity.class);
        mr.whiteListBlockEntity(EnchantingTableBlockEntity.class);
        mr.whiteListBlockEntity(EnderChestBlockEntity.class);
        mr.whiteListBlockEntity(EndPortalBlockEntity.class);
        mr.whiteListBlockEntity(FurnaceBlockEntity.class);
        mr.whiteListBlockEntity(HopperBlockEntity.class);
        mr.whiteListBlockEntity(MobSpawnerBlockEntity.class);
        mr.whiteListBlockEntity(PistonBlockEntity.class);
        mr.whiteListBlockEntity(ShulkerBoxBlockEntity.class);
        mr.whiteListBlockEntity(SignBlockEntity.class);
        mr.whiteListBlockEntity(SkullBlockEntity.class);

        /*
         * Whitelist AE2
         */
        mr.whiteListBlockEntity(AEBaseBlockEntity.class);

        /*
         * world gen
         */
        for (final IWorldGen.WorldGenType type : IWorldGen.WorldGenType.values()) {
            // FIXME: registries.worldgen().disableWorldGenForProviderID( type,
            // StorageWorldProvider.class );

            registries.worldgen().disableWorldGenForDimension(type, DimensionType.THE_NETHER_REGISTRY_KEY.getValue());
        }

        // whitelist from config
        for (final String dimension : AEConfig.instance().getMeteoriteDimensionWhitelist()) {
            registries.worldgen().enableWorldGenForDimension(IWorldGen.WorldGenType.METEORITES,
                    new Identifier(dimension));
        }
    }

    protected void registerParticleTypes() {
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("charged_ore_fx"), ParticleTypes.CHARGED_ORE);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("crafting_fx"), ParticleTypes.CRAFTING);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("energy_fx"), ParticleTypes.ENERGY);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("lightning_arc_fx"), ParticleTypes.LIGHTNING_ARC);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("lightning_fx"), ParticleTypes.LIGHTNING);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("matter_cannon_fx"), ParticleTypes.MATTER_CANNON);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("vibrant_fx"), ParticleTypes.VIBRANT);
    }

    private void registerBlockEntities() {
        callDeferredBootstrapComponents(ITileEntityRegistrationComponent.class,
                ITileEntityRegistrationComponent::register);
    }

    private static <T extends Recipe<?>> RecipeType<T> registerRecipeType(String id) {
        Identifier fullId = AppEng.makeId(id);
        return Registry.register(Registry.RECIPE_TYPE, fullId, new AERecipeType<>(fullId));
    }

    private void registerRecipeTypes() {
        GrinderRecipe.TYPE = registerRecipeType("grinder");
        InscriberRecipe.TYPE = registerRecipeType("inscriber");
    }

    private void registerRecipeSerializers() {
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("quartz_knife"),
                QuartzKnifeRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("grinder"), GrinderRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("inscriber"), InscriberRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("disassemble"), DisassembleRecipe.SERIALIZER);
        FacadeItem facadeItem = (FacadeItem) Api.INSTANCE.definitions().items().facade().item();
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("facade"), FacadeRecipe.getSerializer(facadeItem));
    }

    @Override
    public AdvancementTriggers getAdvancementTriggers() {
        return advancementTriggers;
    }

    @Override
    public void sendToAllNearExcept(final PlayerEntity p, final double x, final double y, final double z,
                                    final double dist, final World w, final BasePacket packet) {
        if (w.isClient()) {
            return;
        }

        NetworkHandler.instance().sendToAllAround(packet, new TargetPoint(x, y, z, dist, w));
    }

    @Override
    public CableRenderMode getRenderMode() {
        if (this.renderModeBased == null) {
            return CableRenderMode.STANDARD;
        }

        return this.renderModeForPlayer(this.renderModeBased);
    }

    // FIXME this is some hot shit _FOR WHAT_?
    @Override
    public void updateRenderMode(final PlayerEntity player) {
        this.renderModeBased = player;
    }

    protected final <T extends IBootstrapComponent> void callDeferredBootstrapComponents(Class<T> componentClass,
                                                                                         Consumer<T> invoker) {
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(componentClass).forEachRemaining(invoker);
    }

    protected CableRenderMode renderModeForPlayer(final PlayerEntity player) {
        if (player != null) {
            for (int x = 0; x < PlayerInventory.getHotbarSize(); x++) {
                final ItemStack is = player.inventory.getStack(x);

                if (!is.isEmpty() && is.getItem() instanceof NetworkToolItem) {
                    final CompoundTag c = is.getTag();
                    if (c != null && c.getBoolean("hideFacades")) {
                        return CableRenderMode.CABLE_VIEW;
                    }
                }
            }
        }

        return CableRenderMode.STANDARD;
    }

    private void registerScreenHandlerTypes() {

        CellWorkbenchContainer.TYPE = registerScreenHandler("cellworkbench", CellWorkbenchContainer::fromNetwork,
                CellWorkbenchContainer::open);
        ChestContainer.TYPE = registerScreenHandler("chest", ChestContainer::fromNetwork, ChestContainer::open);
        CondenserContainer.TYPE = registerScreenHandler("condenser", CondenserContainer::fromNetwork,
                CondenserContainer::open);
        CraftAmountContainer.TYPE = registerScreenHandler("craftamount", CraftAmountContainer::fromNetwork,
                CraftAmountContainer::open);
        CraftConfirmContainer.TYPE = registerScreenHandler("craftconfirm", CraftConfirmContainer::fromNetwork,
                CraftConfirmContainer::open);
        CraftingCPUContainer.TYPE = registerScreenHandler("craftingcpu", CraftingCPUContainer::fromNetwork,
                CraftingCPUContainer::open);
        CraftingStatusContainer.TYPE = registerScreenHandler("craftingstatus", CraftingStatusContainer::fromNetwork,
                CraftingStatusContainer::open);
        CraftingTermContainer.TYPE = registerScreenHandler("craftingterm", CraftingTermContainer::fromNetwork,
                CraftingTermContainer::open);
        DriveContainer.TYPE = registerScreenHandler("drive", DriveContainer::fromNetwork, DriveContainer::open);
        FormationPlaneContainer.TYPE = registerScreenHandler("formationplane", FormationPlaneContainer::fromNetwork,
                FormationPlaneContainer::open);
        GrinderContainer.TYPE = registerScreenHandler("grinder", GrinderContainer::fromNetwork, GrinderContainer::open);
        InscriberContainer.TYPE = registerScreenHandler("inscriber", InscriberContainer::fromNetwork,
                InscriberContainer::open);
        InterfaceContainer.TYPE = registerScreenHandler("interface", InterfaceContainer::fromNetwork,
                InterfaceContainer::open);
        InterfaceTerminalContainer.TYPE = registerScreenHandler("interfaceterminal",
                InterfaceTerminalContainer::fromNetwork, InterfaceTerminalContainer::open);
        IOPortContainer.TYPE = registerScreenHandler("ioport", IOPortContainer::fromNetwork, IOPortContainer::open);
        LevelEmitterContainer.TYPE = registerScreenHandler("levelemitter", LevelEmitterContainer::fromNetwork,
                LevelEmitterContainer::open);
        MolecularAssemblerContainer.TYPE = registerScreenHandler("molecular_assembler",
                MolecularAssemblerContainer::fromNetwork, MolecularAssemblerContainer::open);
        MEMonitorableContainer.TYPE = registerScreenHandler("memonitorable", MEMonitorableContainer::fromNetwork,
                MEMonitorableContainer::open);
        MEPortableCellContainer.TYPE = registerScreenHandler("meportablecell", MEPortableCellContainer::fromNetwork,
                MEPortableCellContainer::open);
        NetworkStatusContainer.TYPE = registerScreenHandler("networkstatus", NetworkStatusContainer::fromNetwork,
                NetworkStatusContainer::open);
        NetworkToolContainer.TYPE = registerScreenHandler("networktool", NetworkToolContainer::fromNetwork,
                NetworkToolContainer::open);
        PatternTermContainer.TYPE = registerScreenHandler("patternterm", PatternTermContainer::fromNetwork,
                PatternTermContainer::open);
        PriorityContainer.TYPE = registerScreenHandler("priority", PriorityContainer::fromNetwork,
                PriorityContainer::open);
        QNBContainer.TYPE = registerScreenHandler("qnb", QNBContainer::fromNetwork, QNBContainer::open);
        QuartzKnifeContainer.TYPE = registerScreenHandler("quartzknife", QuartzKnifeContainer::fromNetwork,
                QuartzKnifeContainer::open);
        SecurityStationContainer.TYPE = registerScreenHandler("securitystation", SecurityStationContainer::fromNetwork,
                SecurityStationContainer::open);
        SkyChestContainer.TYPE = registerScreenHandler("skychest", SkyChestContainer::fromNetwork,
                SkyChestContainer::open);
        SpatialIOPortContainer.TYPE = registerScreenHandler("spatialioport", SpatialIOPortContainer::fromNetwork,
                SpatialIOPortContainer::open);
        StorageBusContainer.TYPE = registerScreenHandler("storagebus", StorageBusContainer::fromNetwork,
                StorageBusContainer::open);
        UpgradeableContainer.TYPE = registerScreenHandler("upgradeable", UpgradeableContainer::fromNetwork,
                UpgradeableContainer::open);
        VibrationChamberContainer.TYPE = registerScreenHandler("vibrationchamber",
                VibrationChamberContainer::fromNetwork, VibrationChamberContainer::open);
        WirelessContainer.TYPE = registerScreenHandler("wireless", WirelessContainer::fromNetwork,
                WirelessContainer::open);
        WirelessTermContainer.TYPE = registerScreenHandler("wirelessterm", WirelessTermContainer::fromNetwork,
                WirelessTermContainer::open);

        FluidFormationPlaneContainer.TYPE = registerScreenHandler("fluid_formation_plane",
                FluidFormationPlaneContainer::fromNetwork, FluidFormationPlaneContainer::open);
        FluidIOContainer.TYPE = registerScreenHandler("fluid_io", FluidIOContainer::fromNetwork,
                FluidIOContainer::open);
        FluidInterfaceContainer.TYPE = registerScreenHandler("fluid_interface", FluidInterfaceContainer::fromNetwork,
                FluidInterfaceContainer::open);
        FluidLevelEmitterContainer.TYPE = registerScreenHandler("fluid_level_emitter",
                FluidLevelEmitterContainer::fromNetwork, FluidLevelEmitterContainer::open);
        FluidStorageBusContainer.TYPE = registerScreenHandler("fluid_storage_bus",
                FluidStorageBusContainer::fromNetwork, FluidStorageBusContainer::open);
        FluidTerminalContainer.TYPE = registerScreenHandler("fluid_terminal", FluidTerminalContainer::fromNetwork,
                FluidTerminalContainer::open);

    }

    private <T extends AEBaseContainer> ScreenHandlerType<T> registerScreenHandler(String id,
                                                                                   ScreenHandlerRegistry.ExtendedClientHandlerFactory<T> factory, ContainerOpener.Opener<T> opener) {
        ScreenHandlerType<T> type = ScreenHandlerRegistry.registerExtended(AppEng.makeId(id), factory);
        ContainerOpener.addOpener(type, opener);
        return type;
    }

    private void registerWorldGen() {
        LibStructure.registerStructure(MeteoriteStructure.ID, MeteoriteStructure.INSTANCE,
                GenerationStep.Feature.TOP_LAYER_MODIFICATION, new StructureConfig(32, 8, 124895654),
                new MeteoriteStructure(DefaultFeatureConfig.CODEC).configure(DefaultFeatureConfig.INSTANCE));
        Registry.register(Registry.FEATURE, AppEng.makeId("charged_quartz_ore"), ChargedQuartzOreFeature.INSTANCE);

        // add to all standard biomes
        Biome.BIOMES.forEach(b -> {
            addMeteoriteWorldGen(b);
            addQuartzWorldGen(b);
        });
    }

    private static void addMeteoriteWorldGen(Biome b) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            return;
        }

        if (b.getCategory() == Biome.Category.THEEND || b.getCategory() == Biome.Category.NETHER) {
            return;
        }

        BiomeModifier modifier = new BiomeModifier(b);
        modifier.addStructureFeature(MeteoriteStructure.INSTANCE.configure(FeatureConfig.DEFAULT));
    }

    private static void addQuartzWorldGen(Biome b) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.CERTUS_QUARTZ_WORLD_GEN)) {
            return;
        }

        BiomeModifier modifier = new BiomeModifier(b);

        BlockState quartzOre = Api.instance().definitions().blocks().quartzOre().block().getDefaultState();
        modifier.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, Feature.ORE
                .configure(new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, quartzOre,
                        AEConfig.instance().getQuartzOresPerCluster()))
                .decorate(Decorator.RANGE.configure(new RangeDecoratorConfig(12, 12, 72))).spreadHorizontally().repeat(AEConfig.instance().getQuartzOresClusterAmount()));

        if (AEConfig.instance().isFeatureEnabled(AEFeature.CHARGED_CERTUS_ORE)) {

            BlockState chargedQuartzOre = Api.instance().definitions().blocks().quartzOreCharged().block()
                    .getDefaultState();
            modifier.addFeature(GenerationStep.Feature.UNDERGROUND_DECORATION,
                    ChargedQuartzOreFeature.INSTANCE
                            .configure(new ChargedQuartzOreConfig(quartzOre, chargedQuartzOre,
                                    AEConfig.instance().getSpawnChargedChance()))
                            .decorate(Decorator.NOPE.configure(NopeDecoratorConfig.INSTANCE)));

        }
    }

    private void registerServerCommands() {
        // The server commands need to know what the current minecraft server is.
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            new AECommand().register(dispatcher);
        });
    }

    private void registerDimension() {
        Registry.register(BuiltinRegistries.BIOME, AppEng.makeId("storage"), StorageCellBiome.INSTANCE);
        Registry.register(Registry.CHUNK_GENERATOR, AppEng.makeId("storage"), StorageChunkGenerator.CODEC);

        RegisterDimensionTypeCallback.EVENT.register(registryTracker -> {
            DimensionType dimensionType = new DimensionType(OptionalLong.of(12000), false, false, false, false, false, false, true, false,
                    false, 256, BlockTags.INFINIBURN_OVERWORLD.getId(), 1.0f);

            Registry.register(registryTracker.getDimensionTypes(), SpatialDimensionManager.STORAGE_DIMENSION_TYPE.getValue(), dimensionType);
        });
    }

}
