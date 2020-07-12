package appeng.core;

import appeng.api.AEApi;
import appeng.api.features.AEFeature;
import appeng.api.features.IRegistryContainer;
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
import appeng.container.implementations.*;
import appeng.core.features.registries.cell.BasicCellHandler;
import appeng.core.features.registries.cell.BasicItemCellGuiHandler;
import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.AeStats;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.network.TargetPoint;
import appeng.fluids.container.*;
import appeng.fluids.registries.BasicFluidCellGuiHandler;
import appeng.forge.data.AE2DataGenerators;
import appeng.hooks.ToolItemHook;
import appeng.items.tools.NetworkToolItem;
import appeng.me.cache.*;
import appeng.mixins.CriteriaRegisterMixin;
import appeng.recipes.handlers.*;
import appeng.server.AECommand;
import appeng.worldgen.ChargedQuartzOreConfig;
import appeng.worldgen.ChargedQuartzOreFeature;
import appeng.worldgen.meteorite.MeteoriteStructure;
import net.earthcomputer.libstructure.LibStructure;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.NopeDecoratorConfig;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;

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
        // FIXME FABRIC new FacadeItemGroup(); // This call has a side-effect (adding it to the creative screen)

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

        setupInternalRegistries();

        if (System.getProperty("appeng2.generatedataendexit", "false").equals("true")) {
            AE2DataGenerators.dump();
            System.exit(0);
        }

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
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("quartz_knife"), QuartzKnifeRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("grinder"), GrinderRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, AppEng.makeId("inscriber"), InscriberRecipeSerializer.INSTANCE);

        // FIXME FABRIC FacadeItem facadeItem = (FacadeItem) Api.INSTANCE.definitions().items().facade().item();
        // FIXME FABRIC r.registerAll(DisassembleRecipe.SERIALIZER,
        // FIXME FABRIC         FacadeRecipe.getSerializer(facadeItem));
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

        NetworkHandler.instance().sendToAllAround(packet, new TargetPoint(
                x, y, z, dist, w
        ));
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

    protected final <T extends IBootstrapComponent> void callDeferredBootstrapComponents(Class<T> componentClass, Consumer<T> invoker) {
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(componentClass)
                .forEachRemaining(invoker);
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
        CraftingStatusContainer.TYPE = registerScreenHandler("craftingstatus",
                CraftingStatusContainer::fromNetwork, CraftingStatusContainer::open);
        CraftingTermContainer.TYPE = registerScreenHandler("craftingterm", CraftingTermContainer::fromNetwork,
                CraftingTermContainer::open);
        DriveContainer.TYPE = registerScreenHandler("drive", DriveContainer::fromNetwork, DriveContainer::open);
        FormationPlaneContainer.TYPE = registerScreenHandler("formationplane",
                FormationPlaneContainer::fromNetwork, FormationPlaneContainer::open);
        GrinderContainer.TYPE = registerScreenHandler("grinder", GrinderContainer::fromNetwork,
                GrinderContainer::open);
        InscriberContainer.TYPE = registerScreenHandler("inscriber", InscriberContainer::fromNetwork,
                InscriberContainer::open);
        InterfaceContainer.TYPE = registerScreenHandler("interface", InterfaceContainer::fromNetwork,
                InterfaceContainer::open);
        InterfaceTerminalContainer.TYPE = registerScreenHandler("interfaceterminal",
                InterfaceTerminalContainer::fromNetwork, InterfaceTerminalContainer::open);
        IOPortContainer.TYPE = registerScreenHandler("ioport", IOPortContainer::fromNetwork,
                IOPortContainer::open);
        LevelEmitterContainer.TYPE = registerScreenHandler("levelemitter", LevelEmitterContainer::fromNetwork,
                LevelEmitterContainer::open);
        MolecularAssemblerContainer.TYPE = registerScreenHandler("molecular_assembler",
                MolecularAssemblerContainer::fromNetwork, MolecularAssemblerContainer::open);
        MEMonitorableContainer.TYPE = registerScreenHandler("memonitorable", MEMonitorableContainer::fromNetwork,
                MEMonitorableContainer::open);
        MEPortableCellContainer.TYPE = registerScreenHandler("meportablecell",
                MEPortableCellContainer::fromNetwork, MEPortableCellContainer::open);
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
        SecurityStationContainer.TYPE = registerScreenHandler("securitystation",
                SecurityStationContainer::fromNetwork, SecurityStationContainer::open);
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
        FluidInterfaceContainer.TYPE = registerScreenHandler("fluid_interface",
                FluidInterfaceContainer::fromNetwork, FluidInterfaceContainer::open);
        FluidLevelEmitterContainer.TYPE = registerScreenHandler("fluid_level_emitter",
                FluidLevelEmitterContainer::fromNetwork, FluidLevelEmitterContainer::open);
        FluidStorageBusContainer.TYPE = registerScreenHandler("fluid_storage_bus",
                FluidStorageBusContainer::fromNetwork, FluidStorageBusContainer::open);
        FluidTerminalContainer.TYPE = registerScreenHandler("fluid_terminal", FluidTerminalContainer::fromNetwork,
                FluidTerminalContainer::open);

    }

    private <T extends AEBaseContainer> ScreenHandlerType<T> registerScreenHandler(String id, ScreenHandlerRegistry.ExtendedClientHandlerFactory<T> factory, ContainerOpener.Opener<T> opener) {
        ScreenHandlerType<T> type = ScreenHandlerRegistry.registerExtended(AppEng.makeId(id), factory);
        ContainerOpener.addOpener(type, opener);
        return type;
    }

    private void registerWorldGen() {
        LibStructure.registerStructure(
                MeteoriteStructure.ID,
                MeteoriteStructure.INSTANCE,
                GenerationStep.Feature.TOP_LAYER_MODIFICATION,
                new StructureConfig(32, 8, 124895654),
                new MeteoriteStructure(DefaultFeatureConfig.CODEC).configure(DefaultFeatureConfig.INSTANCE)
        );
        Registry.register(Registry.FEATURE, AppEng.makeId("charged_quartz_ore"), new ChargedQuartzOreFeature(ChargedQuartzOreConfig.CODEC));

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

        b.addStructureFeature(MeteoriteStructure.INSTANCE.configure(FeatureConfig.DEFAULT));
    }

    private static void addQuartzWorldGen(Biome b) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.CERTUS_QUARTZ_WORLD_GEN)) {
            return;
        }

        BlockState quartzOre = AEApi.instance().definitions().blocks().quartzOre().block().getDefaultState();
        b.addFeature(GenerationStep.Feature.UNDERGROUND_ORES,
                Feature.ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Target.NATURAL_STONE,
                                quartzOre, AEConfig.instance().getQuartzOresPerCluster()))
                        .createDecoratedFeature(Decorator.COUNT_RANGE.configure(
                                new RangeDecoratorConfig(AEConfig.instance().getQuartzOresClusterAmount(), 12, 12, 72))));

        if (AEConfig.instance().isFeatureEnabled(AEFeature.CHARGED_CERTUS_ORE)) {

            BlockState chargedQuartzOre = AEApi.instance().definitions().blocks().quartzOreCharged().block()
                    .getDefaultState();
            b.addFeature(GenerationStep.Feature.UNDERGROUND_DECORATION,
                    ChargedQuartzOreFeature.INSTANCE
                            .configure(new ChargedQuartzOreConfig(quartzOre, chargedQuartzOre,
                                    AEConfig.instance().getSpawnChargedChance()))
                            .createDecoratedFeature(Decorator.NOPE.configure(NopeDecoratorConfig.field_24892)));

        }
    }

    private void registerServerCommands() {
        // The server commands need to know what the current minecraft server is.
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            new AECommand().register(dispatcher);
        });
    }

}
