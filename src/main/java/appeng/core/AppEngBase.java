package appeng.core;

import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridCacheRegistry;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.security.ISecurityGrid;
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
import appeng.hooks.ToolItemHook;
import appeng.me.cache.*;
import appeng.mixins.CriteriaRegisterMixin;
import appeng.recipes.handlers.*;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

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
// FIXME FABRIC        gcr.registerGridCache(P2PCache.class, P2PCache.class);
// FIXME FABRIC        gcr.registerGridCache(ISpatialCache.class, SpatialPylonCache.class);
        gcr.registerGridCache(ISecurityGrid.class, SecurityCache::new);
// FIXME FABRIC        gcr.registerGridCache(ICraftingGrid.class, CraftingGridCache.class);

        registries.cell().addCellHandler(new BasicCellHandler());
        registries.cell().addCellHandler(new CreativeCellHandler());
        registries.cell().addCellGuiHandler(new BasicItemCellGuiHandler());
// FIXME FABRIC        registries.cell().addCellGuiHandler(new BasicFluidCellGuiHandler());

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

               // FIXME FABRIC if (!is.isEmpty() && is.getItem() instanceof NetworkToolItem) {
               // FIXME FABRIC     final CompoundTag c = is.getTag();
               // FIXME FABRIC     if (c != null && c.getBoolean("hideFacades")) {
               // FIXME FABRIC         return CableRenderMode.CABLE_VIEW;
               // FIXME FABRIC     }
               // FIXME FABRIC }
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

// FIXME FABRIC  FluidFormationPlaneContainer.TYPE = registerScreenHandler("fluid_formation_plane",
// FIXME FABRIC          FluidFormationPlaneContainer::fromNetwork, FluidFormationPlaneContainer::open);
// FIXME FABRIC  FluidIOContainer.TYPE = registerScreenHandler("fluid_io", FluidIOContainer::fromNetwork,
// FIXME FABRIC          FluidIOContainer::open);
// FIXME FABRIC  FluidInterfaceContainer.TYPE = registerScreenHandler("fluid_interface",
// FIXME FABRIC          FluidInterfaceContainer::fromNetwork, FluidInterfaceContainer::open);
// FIXME FABRIC  FluidLevelEmitterContainer.TYPE = registerScreenHandler("fluid_level_emitter",
// FIXME FABRIC          FluidLevelEmitterContainer::fromNetwork, FluidLevelEmitterContainer::open);
// FIXME FABRIC  FluidStorageBusContainer.TYPE = registerScreenHandler("fluid_storage_bus",
// FIXME FABRIC          FluidStorageBusContainer::fromNetwork, FluidStorageBusContainer::open);
// FIXME FABRIC  FluidTerminalContainer.TYPE = registerScreenHandler("fluid_terminal", FluidTerminalContainer::fromNetwork,
// FIXME FABRIC          FluidTerminalContainer::open);

    }

    private <T extends AEBaseContainer> ScreenHandlerType<T> registerScreenHandler(String id, ScreenHandlerRegistry.ExtendedClientHandlerFactory<T> factory, ContainerOpener.Opener<T> opener) {
        ScreenHandlerType<T> type = ScreenHandlerRegistry.registerExtended(AppEng.makeId(id), factory);
        ContainerOpener.addOpener(type, opener);
        return type;
    }

}
