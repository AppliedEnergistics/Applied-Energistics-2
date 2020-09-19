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

package appeng.core;

import java.util.function.Supplier;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IParts;
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
import appeng.block.paint.PaintSplotchesModel;
import appeng.block.qnb.QnbFormedModel;
import appeng.bootstrap.components.IBlockRegistrationComponent;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IEntityRegistrationComponent;
import appeng.bootstrap.components.IItemColorRegistrationComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.components.IModelBakeComponent;
import appeng.bootstrap.components.ITileEntityRegistrationComponent;
import appeng.client.gui.implementations.CellWorkbenchScreen;
import appeng.client.gui.implementations.ChestScreen;
import appeng.client.gui.implementations.CondenserScreen;
import appeng.client.gui.implementations.CraftAmountScreen;
import appeng.client.gui.implementations.CraftConfirmScreen;
import appeng.client.gui.implementations.CraftingCPUScreen;
import appeng.client.gui.implementations.CraftingStatusScreen;
import appeng.client.gui.implementations.CraftingTermScreen;
import appeng.client.gui.implementations.DriveScreen;
import appeng.client.gui.implementations.FormationPlaneScreen;
import appeng.client.gui.implementations.GrinderScreen;
import appeng.client.gui.implementations.IOPortScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.implementations.InterfaceTerminalScreen;
import appeng.client.gui.implementations.LevelEmitterScreen;
import appeng.client.gui.implementations.MEMonitorableScreen;
import appeng.client.gui.implementations.MEPortableCellScreen;
import appeng.client.gui.implementations.MolecularAssemblerScreen;
import appeng.client.gui.implementations.NetworkStatusScreen;
import appeng.client.gui.implementations.NetworkToolScreen;
import appeng.client.gui.implementations.PatternTermScreen;
import appeng.client.gui.implementations.PriorityScreen;
import appeng.client.gui.implementations.QNBScreen;
import appeng.client.gui.implementations.QuartzKnifeScreen;
import appeng.client.gui.implementations.SecurityStationScreen;
import appeng.client.gui.implementations.SkyChestScreen;
import appeng.client.gui.implementations.SpatialIOPortScreen;
import appeng.client.gui.implementations.StorageBusScreen;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.implementations.VibrationChamberScreen;
import appeng.client.gui.implementations.WirelessScreen;
import appeng.client.gui.implementations.WirelessTermScreen;
import appeng.client.render.DummyFluidItemModel;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.SimpleModelLoader;
import appeng.client.render.cablebus.CableBusModelLoader;
import appeng.client.render.cablebus.P2PTunnelFrequencyModel;
import appeng.client.render.crafting.CraftingCubeModelLoader;
import appeng.client.render.crafting.EncodedPatternModelLoader;
import appeng.client.render.effects.ChargedOreFX;
import appeng.client.render.effects.CraftingFx;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.effects.VibrantFX;
import appeng.client.render.model.BiometricCardModel;
import appeng.client.render.model.ColorApplicatorModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.GlassModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.client.render.model.SkyCompassModel;
import appeng.client.render.spatial.SpatialPylonModel;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
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
import appeng.core.features.registries.PartModels;
import appeng.core.features.registries.cell.BasicCellHandler;
import appeng.core.features.registries.cell.BasicItemCellGuiHandler;
import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.core.localization.GuiText;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.AeStats;
import appeng.fluids.client.gui.FluidFormationPlaneScreen;
import appeng.fluids.client.gui.FluidIOScreen;
import appeng.fluids.client.gui.FluidInterfaceScreen;
import appeng.fluids.client.gui.FluidLevelEmitterScreen;
import appeng.fluids.client.gui.FluidStorageBusScreen;
import appeng.fluids.client.gui.FluidTerminalScreen;
import appeng.fluids.container.FluidFormationPlaneContainer;
import appeng.fluids.container.FluidIOContainer;
import appeng.fluids.container.FluidInterfaceContainer;
import appeng.fluids.container.FluidLevelEmitterContainer;
import appeng.fluids.container.FluidStorageBusContainer;
import appeng.fluids.container.FluidTerminalContainer;
import appeng.fluids.registries.BasicFluidCellGuiHandler;
import appeng.items.parts.FacadeItem;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cache.EnergyGridCache;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.P2PCache;
import appeng.me.cache.PathGridCache;
import appeng.me.cache.SecurityCache;
import appeng.me.cache.SpatialPylonCache;
import appeng.me.cache.TickManagerCache;
import appeng.parts.automation.PlaneModelLoader;
import appeng.recipes.game.DisassembleRecipe;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.handlers.GrinderRecipeSerializer;
import appeng.recipes.handlers.InscriberRecipeSerializer;
import appeng.server.AECommand;
import appeng.tile.AEBaseTileEntity;
import appeng.tile.crafting.MolecularAssemblerRenderer;

final class Registration {

    static AdvancementTriggers advancementTriggers;

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

    @OnlyIn(Dist.CLIENT)
    public void modelRegistryEvent(ModelRegistryEvent event) {
        registerSpecialModels();

        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IClientSetupComponent.class)
                .forEachRemaining(IClientSetupComponent::setup);

        addBuiltInModel("glass", GlassModel::new);
        addBuiltInModel("sky_compass", SkyCompassModel::new);
        addBuiltInModel("dummy_fluid_item", DummyFluidItemModel::new);
        addBuiltInModel("memory_card", MemoryCardModel::new);
        addBuiltInModel("biometric_card", BiometricCardModel::new);
        addBuiltInModel("drive", DriveModel::new);
        addBuiltInModel("color_applicator", ColorApplicatorModel::new);
        addBuiltInModel("spatial_pylon", SpatialPylonModel::new);
        addBuiltInModel("paint_splotches", PaintSplotchesModel::new);
        addBuiltInModel("quantum_bridge_formed", QnbFormedModel::new);
        addBuiltInModel("p2p_tunnel_frequency", P2PTunnelFrequencyModel::new);
        addBuiltInModel("facade", FacadeItemModel::new);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "encoded_pattern"),
                EncodedPatternModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "part_plane"),
                PlaneModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "crafting_cube"),
                CraftingCubeModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "cable_bus"),
                new CableBusModelLoader((PartModels) Api.INSTANCE.registries().partModels()));

    }

    @OnlyIn(Dist.CLIENT)
    private static <T extends IModelGeometry<T>> void addBuiltInModel(String id, Supplier<T> modelFactory) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, id),
                new SimpleModelLoader<>(modelFactory));
    }

    /**
     * Registers any JSON model files with Minecraft that are not referenced via
     * blockstates or item IDs
     */
    @OnlyIn(Dist.CLIENT)
    private void registerSpecialModels() {
        ModelLoader.addSpecialModel(MolecularAssemblerRenderer.LIGHTS_MODEL);

        PartModels partModels = (PartModels) Api.INSTANCE.registries().partModels();
        partModels.getModels().forEach(ModelLoader::addSpecialModel);
        partModels.setInitialized(true);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        final Dist dist = FMLEnvironment.dist;
        definitions.getRegistry().getBootstrapComponents(IBlockRegistrationComponent.class)
                .forEachRemaining(b -> b.blockRegistration(dist, registry));
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        final Dist dist = FMLEnvironment.dist;
        definitions.getRegistry().getBootstrapComponents(IItemRegistrationComponent.class)
                .forEachRemaining(b -> b.itemRegistration(dist, registry));
    }

    public void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        final IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(ITileEntityRegistrationComponent.class)
                .forEachRemaining(b -> b.register(registry));
    }

    public void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> event) {
        final IForgeRegistry<ContainerType<?>> registry = event.getRegistry();

        CellWorkbenchContainer.TYPE = registerContainer(registry, "cellworkbench", CellWorkbenchContainer::fromNetwork,
                CellWorkbenchContainer::open);
        ChestContainer.TYPE = registerContainer(registry, "chest", ChestContainer::fromNetwork, ChestContainer::open);
        CondenserContainer.TYPE = registerContainer(registry, "condenser", CondenserContainer::fromNetwork,
                CondenserContainer::open);
        CraftAmountContainer.TYPE = registerContainer(registry, "craftamount", CraftAmountContainer::fromNetwork,
                CraftAmountContainer::open);
        CraftConfirmContainer.TYPE = registerContainer(registry, "craftconfirm", CraftConfirmContainer::fromNetwork,
                CraftConfirmContainer::open);
        CraftingCPUContainer.TYPE = registerContainer(registry, "craftingcpu", CraftingCPUContainer::fromNetwork,
                CraftingCPUContainer::open);
        CraftingStatusContainer.TYPE = registerContainer(registry, "craftingstatus",
                CraftingStatusContainer::fromNetwork, CraftingStatusContainer::open);
        CraftingTermContainer.TYPE = registerContainer(registry, "craftingterm", CraftingTermContainer::fromNetwork,
                CraftingTermContainer::open);
        DriveContainer.TYPE = registerContainer(registry, "drive", DriveContainer::fromNetwork, DriveContainer::open);
        FormationPlaneContainer.TYPE = registerContainer(registry, "formationplane",
                FormationPlaneContainer::fromNetwork, FormationPlaneContainer::open);
        GrinderContainer.TYPE = registerContainer(registry, "grinder", GrinderContainer::fromNetwork,
                GrinderContainer::open);
        InscriberContainer.TYPE = registerContainer(registry, "inscriber", InscriberContainer::fromNetwork,
                InscriberContainer::open);
        InterfaceContainer.TYPE = registerContainer(registry, "interface", InterfaceContainer::fromNetwork,
                InterfaceContainer::open);
        InterfaceTerminalContainer.TYPE = registerContainer(registry, "interfaceterminal",
                InterfaceTerminalContainer::fromNetwork, InterfaceTerminalContainer::open);
        IOPortContainer.TYPE = registerContainer(registry, "ioport", IOPortContainer::fromNetwork,
                IOPortContainer::open);
        LevelEmitterContainer.TYPE = registerContainer(registry, "levelemitter", LevelEmitterContainer::fromNetwork,
                LevelEmitterContainer::open);
        MolecularAssemblerContainer.TYPE = registerContainer(registry, "molecular_assembler",
                MolecularAssemblerContainer::fromNetwork, MolecularAssemblerContainer::open);
        MEMonitorableContainer.TYPE = registerContainer(registry, "memonitorable", MEMonitorableContainer::fromNetwork,
                MEMonitorableContainer::open);
        MEPortableCellContainer.TYPE = registerContainer(registry, "meportablecell",
                MEPortableCellContainer::fromNetwork, MEPortableCellContainer::open);
        NetworkStatusContainer.TYPE = registerContainer(registry, "networkstatus", NetworkStatusContainer::fromNetwork,
                NetworkStatusContainer::open);
        NetworkToolContainer.TYPE = registerContainer(registry, "networktool", NetworkToolContainer::fromNetwork,
                NetworkToolContainer::open);
        PatternTermContainer.TYPE = registerContainer(registry, "patternterm", PatternTermContainer::fromNetwork,
                PatternTermContainer::open);
        PriorityContainer.TYPE = registerContainer(registry, "priority", PriorityContainer::fromNetwork,
                PriorityContainer::open);
        QNBContainer.TYPE = registerContainer(registry, "qnb", QNBContainer::fromNetwork, QNBContainer::open);
        QuartzKnifeContainer.TYPE = registerContainer(registry, "quartzknife", QuartzKnifeContainer::fromNetwork,
                QuartzKnifeContainer::open);
        SecurityStationContainer.TYPE = registerContainer(registry, "securitystation",
                SecurityStationContainer::fromNetwork, SecurityStationContainer::open);
        SkyChestContainer.TYPE = registerContainer(registry, "skychest", SkyChestContainer::fromNetwork,
                SkyChestContainer::open);
        SpatialIOPortContainer.TYPE = registerContainer(registry, "spatialioport", SpatialIOPortContainer::fromNetwork,
                SpatialIOPortContainer::open);
        StorageBusContainer.TYPE = registerContainer(registry, "storagebus", StorageBusContainer::fromNetwork,
                StorageBusContainer::open);
        UpgradeableContainer.TYPE = registerContainer(registry, "upgradeable", UpgradeableContainer::fromNetwork,
                UpgradeableContainer::open);
        VibrationChamberContainer.TYPE = registerContainer(registry, "vibrationchamber",
                VibrationChamberContainer::fromNetwork, VibrationChamberContainer::open);
        WirelessContainer.TYPE = registerContainer(registry, "wireless", WirelessContainer::fromNetwork,
                WirelessContainer::open);
        WirelessTermContainer.TYPE = registerContainer(registry, "wirelessterm", WirelessTermContainer::fromNetwork,
                WirelessTermContainer::open);

        FluidFormationPlaneContainer.TYPE = registerContainer(registry, "fluid_formation_plane",
                FluidFormationPlaneContainer::fromNetwork, FluidFormationPlaneContainer::open);
        FluidIOContainer.TYPE = registerContainer(registry, "fluid_io", FluidIOContainer::fromNetwork,
                FluidIOContainer::open);
        FluidInterfaceContainer.TYPE = registerContainer(registry, "fluid_interface",
                FluidInterfaceContainer::fromNetwork, FluidInterfaceContainer::open);
        FluidLevelEmitterContainer.TYPE = registerContainer(registry, "fluid_level_emitter",
                FluidLevelEmitterContainer::fromNetwork, FluidLevelEmitterContainer::open);
        FluidStorageBusContainer.TYPE = registerContainer(registry, "fluid_storage_bus",
                FluidStorageBusContainer::fromNetwork, FluidStorageBusContainer::open);
        FluidTerminalContainer.TYPE = registerContainer(registry, "fluid_terminal", FluidTerminalContainer::fromNetwork,
                FluidTerminalContainer::open);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            ScreenManager.registerFactory(GrinderContainer.TYPE, GrinderScreen::new);
            ScreenManager.registerFactory(QNBContainer.TYPE, QNBScreen::new);
            ScreenManager.registerFactory(SkyChestContainer.TYPE, SkyChestScreen::new);
            ScreenManager.registerFactory(ChestContainer.TYPE, ChestScreen::new);
            ScreenManager.registerFactory(WirelessContainer.TYPE, WirelessScreen::new);
            ScreenManager.<MEMonitorableContainer, MEMonitorableScreen<MEMonitorableContainer>>registerFactory(
                    MEMonitorableContainer.TYPE, MEMonitorableScreen::new);
            ScreenManager.registerFactory(MEPortableCellContainer.TYPE, MEPortableCellScreen::new);
            ScreenManager.registerFactory(WirelessTermContainer.TYPE, WirelessTermScreen::new);
            ScreenManager.registerFactory(NetworkStatusContainer.TYPE, NetworkStatusScreen::new);
            ScreenManager.<CraftingCPUContainer, CraftingCPUScreen<CraftingCPUContainer>>registerFactory(
                    CraftingCPUContainer.TYPE, CraftingCPUScreen::new);
            ScreenManager.registerFactory(NetworkToolContainer.TYPE, NetworkToolScreen::new);
            ScreenManager.registerFactory(QuartzKnifeContainer.TYPE, QuartzKnifeScreen::new);
            ScreenManager.registerFactory(DriveContainer.TYPE, DriveScreen::new);
            ScreenManager.registerFactory(VibrationChamberContainer.TYPE, VibrationChamberScreen::new);
            ScreenManager.registerFactory(CondenserContainer.TYPE, CondenserScreen::new);
            ScreenManager.registerFactory(InterfaceContainer.TYPE, InterfaceScreen::new);
            ScreenManager.registerFactory(FluidInterfaceContainer.TYPE, FluidInterfaceScreen::new);
            ScreenManager.<UpgradeableContainer, UpgradeableScreen<UpgradeableContainer>>registerFactory(
                    UpgradeableContainer.TYPE, UpgradeableScreen::new);
            ScreenManager.registerFactory(FluidIOContainer.TYPE, FluidIOScreen::new);
            ScreenManager.registerFactory(IOPortContainer.TYPE, IOPortScreen::new);
            ScreenManager.registerFactory(StorageBusContainer.TYPE, StorageBusScreen::new);
            ScreenManager.registerFactory(FluidStorageBusContainer.TYPE, FluidStorageBusScreen::new);
            ScreenManager.registerFactory(FormationPlaneContainer.TYPE, FormationPlaneScreen::new);
            ScreenManager.registerFactory(FluidFormationPlaneContainer.TYPE, FluidFormationPlaneScreen::new);
            ScreenManager.registerFactory(PriorityContainer.TYPE, PriorityScreen::new);
            ScreenManager.registerFactory(SecurityStationContainer.TYPE, SecurityStationScreen::new);
            ScreenManager.registerFactory(CraftingTermContainer.TYPE, CraftingTermScreen::new);
            ScreenManager.registerFactory(PatternTermContainer.TYPE, PatternTermScreen::new);
            ScreenManager.registerFactory(FluidTerminalContainer.TYPE, FluidTerminalScreen::new);
            ScreenManager.registerFactory(LevelEmitterContainer.TYPE, LevelEmitterScreen::new);
            ScreenManager.registerFactory(FluidLevelEmitterContainer.TYPE, FluidLevelEmitterScreen::new);
            ScreenManager.registerFactory(SpatialIOPortContainer.TYPE, SpatialIOPortScreen::new);
            ScreenManager.registerFactory(InscriberContainer.TYPE, InscriberScreen::new);
            ScreenManager.registerFactory(CellWorkbenchContainer.TYPE, CellWorkbenchScreen::new);
            ScreenManager.registerFactory(MolecularAssemblerContainer.TYPE, MolecularAssemblerScreen::new);
            ScreenManager.registerFactory(CraftAmountContainer.TYPE, CraftAmountScreen::new);
            ScreenManager.registerFactory(CraftConfirmContainer.TYPE, CraftConfirmScreen::new);
            ScreenManager.registerFactory(InterfaceTerminalContainer.TYPE, InterfaceTerminalScreen::new);
            ScreenManager.registerFactory(CraftingStatusContainer.TYPE, CraftingStatusScreen::new);
        });
    }

    private <T extends AEBaseContainer> ContainerType<T> registerContainer(IForgeRegistry<ContainerType<?>> registry,
            String id, IContainerFactory<T> factory, ContainerOpener.Opener<T> opener) {
        ContainerType<T> type = IForgeContainerType.create(factory);
        type.setRegistryName(AppEng.MOD_ID, id);
        registry.register(type);
        ContainerOpener.addOpener(type, opener);
        return type;
    }

    public void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        IForgeRegistry<IRecipeSerializer<?>> r = event.getRegistry();

        FacadeItem facadeItem = (FacadeItem) Api.INSTANCE.definitions().items().facade().item();
        r.registerAll(DisassembleRecipe.SERIALIZER, GrinderRecipeSerializer.INSTANCE,
                InscriberRecipeSerializer.INSTANCE, FacadeRecipe.getSerializer(facadeItem));
    }

    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        final IForgeRegistry<EntityType<?>> registry = event.getRegistry();
        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IEntityRegistrationComponent.class)
                .forEachRemaining(b -> b.entityRegistration(registry));
    }

    public void registerParticleTypes(RegistryEvent.Register<ParticleType<?>> event) {
        final IForgeRegistry<ParticleType<?>> registry = event.getRegistry();
        registry.register(ParticleTypes.CHARGED_ORE);
        registry.register(ParticleTypes.CRAFTING);
        registry.register(ParticleTypes.ENERGY);
        registry.register(ParticleTypes.LIGHTNING_ARC);
        registry.register(ParticleTypes.LIGHTNING);
        registry.register(ParticleTypes.MATTER_CANNON);
        registry.register(ParticleTypes.VIBRANT);
    }

    @OnlyIn(Dist.CLIENT)
    public void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        ParticleManager particles = Minecraft.getInstance().particles;
        particles.registerFactory(ParticleTypes.CHARGED_ORE, ChargedOreFX.Factory::new);
        particles.registerFactory(ParticleTypes.CRAFTING, CraftingFx.Factory::new);
        particles.registerFactory(ParticleTypes.ENERGY, EnergyFx.Factory::new);
        particles.registerFactory(ParticleTypes.LIGHTNING_ARC, LightningArcFX.Factory::new);
        particles.registerFactory(ParticleTypes.LIGHTNING, LightningFX.Factory::new);
        particles.registerFactory(ParticleTypes.MATTER_CANNON, MatterCannonFX.Factory::new);
        particles.registerFactory(ParticleTypes.VIBRANT, VibrantFX.Factory::new);
    }

    // FIXME LATER
    public static void postInit() {
        AeStats.register();
        advancementTriggers = new AdvancementTriggers(CriteriaTriggers::register);

        final IRegistryContainer registries = Api.instance().registries();
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
        mr.whiteListTileEntity(net.minecraft.tileentity.BannerTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.BeaconTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.BrewingStandTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.ChestTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.CommandBlockTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.ComparatorTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.DaylightDetectorTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.DispenserTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.DropperTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.EnchantingTableTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.EnderChestTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.EndPortalTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.FurnaceTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.HopperTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.MobSpawnerTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.PistonTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.ShulkerBoxTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.SignTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.SkullTileEntity.class);

        /*
         * Whitelist AE2
         */
        mr.whiteListTileEntity(AEBaseTileEntity.class);

        /*
         * world gen
         */
        for (final IWorldGen.WorldGenType type : IWorldGen.WorldGenType.values()) {
            registries.worldgen().disableWorldGenForDimension(type, World.THE_NETHER.getRegistryName());
        }

        // whitelist from config
        for (final String dimension : AEConfig.instance().getMeteoriteDimensionWhitelist()) {
            registries.worldgen().enableWorldGenForDimension(IWorldGen.WorldGenType.METEORITES,
                    new ResourceLocation(dimension));
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void registerTextures(TextureStitchEvent.Pre event) {
        SkyChestTESR.registerTextures(event);
        InscriberTESR.registerTexture(event);
    }

    public void registerCommands(final FMLServerStartingEvent evt) {
        new AECommand().register(evt.getServer().getCommandManager().getDispatcher());
    }

    @OnlyIn(Dist.CLIENT)
    public void registerItemColors(ColorHandlerEvent.Item event) {
        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IItemColorRegistrationComponent.class)
                .forEachRemaining(c -> c.register(event.getItemColors(), event.getBlockColors()));
    }

    @OnlyIn(Dist.CLIENT)
    public void handleModelBake(ModelBakeEvent event) {
        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IModelBakeComponent.class)
                .forEachRemaining(c -> c.onModelBakeEvent(event));
    }

    @OnlyIn(Dist.CLIENT)
    public void registerClientEvents() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::registerParticleFactories);
        modEventBus.addListener(this::registerTextures);
        modEventBus.addListener(this::modelRegistryEvent);
        modEventBus.addListener(this::registerItemColors);
        modEventBus.addListener(this::handleModelBake);
    }
}
