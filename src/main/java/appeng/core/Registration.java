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

import appeng.items.parts.ItemFacade;
import appeng.recipes.game.FacadeRecipe;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.api.AEApi;
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
import appeng.bootstrap.components.IBlockRegistrationComponent;
import appeng.bootstrap.components.IEntityRegistrationComponent;
import appeng.bootstrap.components.IItemColorRegistrationComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.components.IModelBakeComponent;
import appeng.bootstrap.components.ITileEntityRegistrationComponent;
import appeng.client.gui.implementations.GuiCellWorkbench;
import appeng.client.gui.implementations.GuiChest;
import appeng.client.gui.implementations.GuiCondenser;
import appeng.client.gui.implementations.GuiCraftAmount;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingCPU;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.implementations.GuiCraftingTerm;
import appeng.client.gui.implementations.GuiDrive;
import appeng.client.gui.implementations.GuiFormationPlane;
import appeng.client.gui.implementations.GuiGrinder;
import appeng.client.gui.implementations.GuiIOPort;
import appeng.client.gui.implementations.GuiInscriber;
import appeng.client.gui.implementations.GuiInterface;
import appeng.client.gui.implementations.GuiInterfaceTerminal;
import appeng.client.gui.implementations.GuiLevelEmitter;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiMEPortableCell;
import appeng.client.gui.implementations.GuiMolecularAssembler;
import appeng.client.gui.implementations.GuiNetworkStatus;
import appeng.client.gui.implementations.GuiNetworkTool;
import appeng.client.gui.implementations.GuiPatternTerm;
import appeng.client.gui.implementations.GuiPriority;
import appeng.client.gui.implementations.GuiQNB;
import appeng.client.gui.implementations.GuiQuartzKnife;
import appeng.client.gui.implementations.GuiSecurityStation;
import appeng.client.gui.implementations.GuiSkyChest;
import appeng.client.gui.implementations.GuiSpatialIOPort;
import appeng.client.gui.implementations.GuiStorageBus;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.implementations.GuiVibrationChamber;
import appeng.client.gui.implementations.GuiWireless;
import appeng.client.gui.implementations.GuiWirelessTerm;
import appeng.client.render.effects.*;
import appeng.client.render.model.BiometricCardModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.client.render.model.SkyCompassModel;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.container.implementations.ContainerChest;
import appeng.container.implementations.ContainerCondenser;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerCraftingCPU;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerDrive;
import appeng.container.implementations.ContainerFormationPlane;
import appeng.container.implementations.ContainerGrinder;
import appeng.container.implementations.ContainerIOPort;
import appeng.container.implementations.ContainerInscriber;
import appeng.container.implementations.ContainerInterface;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.implementations.ContainerMEPortableCell;
import appeng.container.implementations.ContainerMolecularAssembler;
import appeng.container.implementations.ContainerNetworkStatus;
import appeng.container.implementations.ContainerNetworkTool;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPriority;
import appeng.container.implementations.ContainerQNB;
import appeng.container.implementations.ContainerQuartzKnife;
import appeng.container.implementations.ContainerSecurityStation;
import appeng.container.implementations.ContainerSkyChest;
import appeng.container.implementations.ContainerSpatialIOPort;
import appeng.container.implementations.ContainerStorageBus;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.implementations.ContainerVibrationChamber;
import appeng.container.implementations.ContainerWireless;
import appeng.container.implementations.ContainerWirelessTerm;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.features.registries.PartModels;
import appeng.core.features.registries.cell.BasicCellHandler;
import appeng.core.features.registries.cell.BasicItemCellGuiHandler;
import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.AeStats;
import appeng.core.stats.PartItemPredicate;
import appeng.fluids.client.gui.GuiFluidFormationPlane;
import appeng.fluids.client.gui.GuiFluidIO;
import appeng.fluids.client.gui.GuiFluidInterface;
import appeng.fluids.client.gui.GuiFluidLevelEmitter;
import appeng.fluids.client.gui.GuiFluidStorageBus;
import appeng.fluids.client.gui.GuiFluidTerminal;
import appeng.fluids.container.ContainerFluidFormationPlane;
import appeng.fluids.container.ContainerFluidIO;
import appeng.fluids.container.ContainerFluidInterface;
import appeng.fluids.container.ContainerFluidLevelEmitter;
import appeng.fluids.container.ContainerFluidStorageBus;
import appeng.fluids.container.ContainerFluidTerminal;
import appeng.fluids.registries.BasicFluidCellGuiHandler;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cache.EnergyGridCache;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.P2PCache;
import appeng.me.cache.PathGridCache;
import appeng.me.cache.SecurityCache;
import appeng.me.cache.SpatialPylonCache;
import appeng.me.cache.TickManagerCache;
import appeng.recipes.conditions.FeaturesEnabled;
import appeng.recipes.game.DisassembleRecipe;
import appeng.recipes.handlers.GrinderRecipe;
import appeng.recipes.handlers.GrinderRecipeSerializer;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.handlers.InscriberRecipeSerializer;
import appeng.server.AECommand;
import appeng.spatial.StorageCellBiome;
import appeng.spatial.StorageCellModDimension;
import appeng.tile.AEBaseTile;
import appeng.tile.crafting.MolecularAssemblerRenderer;
import appeng.worldgen.ChargedQuartzOreConfig;
import appeng.worldgen.ChargedQuartzOreFeature;
import appeng.worldgen.MeteoriteStructure;

final class Registration {

    public Registration() {
        AeStats.register();
        advancementTriggers = new AdvancementTriggers(CriteriaTriggers::register);
    }

    AdvancementTriggers advancementTriggers;

    public static void setupInternalRegistries() {
        // TODO: Do not use the internal API
        final Api api = Api.INSTANCE;
        final IRegistryContainer registries = api.registries();

        final IGridCacheRegistry gcr = registries.gridCache();
        gcr.registerGridCache(ITickManager.class, TickManagerCache.class);
        gcr.registerGridCache(IEnergyGrid.class, EnergyGridCache.class);
        gcr.registerGridCache(IPathingGrid.class, PathGridCache.class);
        gcr.registerGridCache(IStorageGrid.class, GridStorageCache.class);
        gcr.registerGridCache(P2PCache.class, P2PCache.class);
        gcr.registerGridCache(ISpatialCache.class, SpatialPylonCache.class);
        gcr.registerGridCache(ISecurityGrid.class, SecurityCache.class);
        gcr.registerGridCache(ICraftingGrid.class, CraftingGridCache.class);

        registries.cell().addCellHandler(new BasicCellHandler());
        registries.cell().addCellHandler(new CreativeCellHandler());
        registries.cell().addCellGuiHandler(new BasicItemCellGuiHandler());
        registries.cell().addCellGuiHandler(new BasicFluidCellGuiHandler());

        registries.matterCannon().registerAmmoItem(api.definitions().materials().matterBall().item(), 32);

        PartItemPredicate.register();
    }

    @OnlyIn(Dist.CLIENT)
    public void modelRegistryEvent(ModelRegistryEvent event) {
        registerSpecialModels();
    }

    /**
     * Registers any JSON model files with Minecraft that are not referenced via
     * blockstates or item IDs
     */
    @OnlyIn(Dist.CLIENT)
    private void registerSpecialModels() {
        SkyCompassModel.DEPENDENCIES.forEach(ModelLoader::addSpecialModel);
        ModelLoader.addSpecialModel(BiometricCardModel.MODEL_BASE);
        ModelLoader.addSpecialModel(MemoryCardModel.MODEL_BASE);
        DriveModel.DEPENDENCIES.forEach(ModelLoader::addSpecialModel);
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

        ContainerCellWorkbench.TYPE = registerContainer(registry, "cellworkbench", ContainerCellWorkbench::fromNetwork,
                ContainerCellWorkbench::open);
        ContainerChest.TYPE = registerContainer(registry, "chest", ContainerChest::fromNetwork, ContainerChest::open);
        ContainerCondenser.TYPE = registerContainer(registry, "condenser", ContainerCondenser::fromNetwork,
                ContainerCondenser::open);
        ContainerCraftAmount.TYPE = registerContainer(registry, "craftamount", ContainerCraftAmount::fromNetwork,
                ContainerCraftAmount::open);
        ContainerCraftConfirm.TYPE = registerContainer(registry, "craftconfirm", ContainerCraftConfirm::fromNetwork,
                ContainerCraftConfirm::open);
        ContainerCraftingCPU.TYPE = registerContainer(registry, "craftingcpu", ContainerCraftingCPU::fromNetwork,
                ContainerCraftingCPU::open);
        ContainerCraftingStatus.TYPE = registerContainer(registry, "craftingstatus",
                ContainerCraftingStatus::fromNetwork, ContainerCraftingStatus::open);
        ContainerCraftingTerm.TYPE = registerContainer(registry, "craftingterm", ContainerCraftingTerm::fromNetwork,
                ContainerCraftingTerm::open);
        ContainerDrive.TYPE = registerContainer(registry, "drive", ContainerDrive::fromNetwork, ContainerDrive::open);
        ContainerFormationPlane.TYPE = registerContainer(registry, "formationplane",
                ContainerFormationPlane::fromNetwork, ContainerFormationPlane::open);
        ContainerGrinder.TYPE = registerContainer(registry, "grinder", ContainerGrinder::fromNetwork,
                ContainerGrinder::open);
        ContainerInscriber.TYPE = registerContainer(registry, "inscriber", ContainerInscriber::fromNetwork,
                ContainerInscriber::open);
        ContainerInterface.TYPE = registerContainer(registry, "interface", ContainerInterface::fromNetwork,
                ContainerInterface::open);
        ContainerInterfaceTerminal.TYPE = registerContainer(registry, "interfaceterminal",
                ContainerInterfaceTerminal::fromNetwork, ContainerInterfaceTerminal::open);
        ContainerIOPort.TYPE = registerContainer(registry, "ioport", ContainerIOPort::fromNetwork,
                ContainerIOPort::open);
        ContainerLevelEmitter.TYPE = registerContainer(registry, "levelemitter", ContainerLevelEmitter::fromNetwork,
                ContainerLevelEmitter::open);
        ContainerMolecularAssembler.TYPE = registerContainer(registry, "molecular_assembler",
                ContainerMolecularAssembler::fromNetwork, ContainerMolecularAssembler::open);
        ContainerMEMonitorable.TYPE = registerContainer(registry, "memonitorable", ContainerMEMonitorable::fromNetwork,
                ContainerMEMonitorable::open);
        ContainerMEPortableCell.TYPE = registerContainer(registry, "meportablecell",
                ContainerMEPortableCell::fromNetwork, ContainerMEPortableCell::open);
        ContainerNetworkStatus.TYPE = registerContainer(registry, "networkstatus", ContainerNetworkStatus::fromNetwork,
                ContainerNetworkStatus::open);
        ContainerNetworkTool.TYPE = registerContainer(registry, "networktool", ContainerNetworkTool::fromNetwork,
                ContainerNetworkTool::open);
        ContainerPatternTerm.TYPE = registerContainer(registry, "patternterm", ContainerPatternTerm::fromNetwork,
                ContainerPatternTerm::open);
        ContainerPriority.TYPE = registerContainer(registry, "priority", ContainerPriority::fromNetwork,
                ContainerPriority::open);
        ContainerQNB.TYPE = registerContainer(registry, "qnb", ContainerQNB::fromNetwork, ContainerQNB::open);
        ContainerQuartzKnife.TYPE = registerContainer(registry, "quartzknife", ContainerQuartzKnife::fromNetwork,
                ContainerQuartzKnife::open);
        ContainerSecurityStation.TYPE = registerContainer(registry, "securitystation",
                ContainerSecurityStation::fromNetwork, ContainerSecurityStation::open);
        ContainerSkyChest.TYPE = registerContainer(registry, "skychest", ContainerSkyChest::fromNetwork,
                ContainerSkyChest::open);
        ContainerSpatialIOPort.TYPE = registerContainer(registry, "spatialioport", ContainerSpatialIOPort::fromNetwork,
                ContainerSpatialIOPort::open);
        ContainerStorageBus.TYPE = registerContainer(registry, "storagebus", ContainerStorageBus::fromNetwork,
                ContainerStorageBus::open);
        ContainerUpgradeable.TYPE = registerContainer(registry, "upgradeable", ContainerUpgradeable::fromNetwork,
                ContainerUpgradeable::open);
        ContainerVibrationChamber.TYPE = registerContainer(registry, "vibrationchamber",
                ContainerVibrationChamber::fromNetwork, ContainerVibrationChamber::open);
        ContainerWireless.TYPE = registerContainer(registry, "wireless", ContainerWireless::fromNetwork,
                ContainerWireless::open);
        ContainerWirelessTerm.TYPE = registerContainer(registry, "wirelessterm", ContainerWirelessTerm::fromNetwork,
                ContainerWirelessTerm::open);

        ContainerFluidFormationPlane.TYPE = registerContainer(registry, "fluid_formation_plane",
                ContainerFluidFormationPlane::fromNetwork, ContainerFluidFormationPlane::open);
        ContainerFluidIO.TYPE = registerContainer(registry, "fluid_io", ContainerFluidIO::fromNetwork,
                ContainerFluidIO::open);
        ContainerFluidInterface.TYPE = registerContainer(registry, "fluid_interface",
                ContainerFluidInterface::fromNetwork, ContainerFluidInterface::open);
        ContainerFluidLevelEmitter.TYPE = registerContainer(registry, "fluid_level_emitter",
                ContainerFluidLevelEmitter::fromNetwork, ContainerFluidLevelEmitter::open);
        ContainerFluidStorageBus.TYPE = registerContainer(registry, "fluid_storage_bus",
                ContainerFluidStorageBus::fromNetwork, ContainerFluidStorageBus::open);
        ContainerFluidTerminal.TYPE = registerContainer(registry, "fluid_terminal", ContainerFluidTerminal::fromNetwork,
                ContainerFluidTerminal::open);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            ScreenManager.registerFactory(ContainerGrinder.TYPE, GuiGrinder::new);
            ScreenManager.registerFactory(ContainerQNB.TYPE, GuiQNB::new);
            ScreenManager.registerFactory(ContainerSkyChest.TYPE, GuiSkyChest::new);
            ScreenManager.registerFactory(ContainerChest.TYPE, GuiChest::new);
            ScreenManager.registerFactory(ContainerWireless.TYPE, GuiWireless::new);
            ScreenManager.<ContainerMEMonitorable, GuiMEMonitorable<ContainerMEMonitorable>>registerFactory(
                    ContainerMEMonitorable.TYPE, GuiMEMonitorable::new);
            ScreenManager.registerFactory(ContainerMEPortableCell.TYPE, GuiMEPortableCell::new);
            ScreenManager.registerFactory(ContainerWirelessTerm.TYPE, GuiWirelessTerm::new);
            ScreenManager.registerFactory(ContainerNetworkStatus.TYPE, GuiNetworkStatus::new);
            ScreenManager.<ContainerCraftingCPU, GuiCraftingCPU<ContainerCraftingCPU>>registerFactory(
                    ContainerCraftingCPU.TYPE, GuiCraftingCPU::new);
            ScreenManager.registerFactory(ContainerNetworkTool.TYPE, GuiNetworkTool::new);
            ScreenManager.registerFactory(ContainerQuartzKnife.TYPE, GuiQuartzKnife::new);
            ScreenManager.registerFactory(ContainerDrive.TYPE, GuiDrive::new);
            ScreenManager.registerFactory(ContainerVibrationChamber.TYPE, GuiVibrationChamber::new);
            ScreenManager.registerFactory(ContainerCondenser.TYPE, GuiCondenser::new);
            ScreenManager.registerFactory(ContainerInterface.TYPE, GuiInterface::new);
            ScreenManager.registerFactory(ContainerFluidInterface.TYPE, GuiFluidInterface::new);
            ScreenManager.<ContainerUpgradeable, GuiUpgradeable<ContainerUpgradeable>>registerFactory(
                    ContainerUpgradeable.TYPE, GuiUpgradeable::new);
            ScreenManager.registerFactory(ContainerFluidIO.TYPE, GuiFluidIO::new);
            ScreenManager.registerFactory(ContainerIOPort.TYPE, GuiIOPort::new);
            ScreenManager.registerFactory(ContainerStorageBus.TYPE, GuiStorageBus::new);
            ScreenManager.registerFactory(ContainerFluidStorageBus.TYPE, GuiFluidStorageBus::new);
            ScreenManager.registerFactory(ContainerFormationPlane.TYPE, GuiFormationPlane::new);
            ScreenManager.registerFactory(ContainerFluidFormationPlane.TYPE, GuiFluidFormationPlane::new);
            ScreenManager.registerFactory(ContainerPriority.TYPE, GuiPriority::new);
            ScreenManager.registerFactory(ContainerSecurityStation.TYPE, GuiSecurityStation::new);
            ScreenManager.registerFactory(ContainerCraftingTerm.TYPE, GuiCraftingTerm::new);
            ScreenManager.registerFactory(ContainerPatternTerm.TYPE, GuiPatternTerm::new);
            ScreenManager.registerFactory(ContainerFluidTerminal.TYPE, GuiFluidTerminal::new);
            ScreenManager.registerFactory(ContainerLevelEmitter.TYPE, GuiLevelEmitter::new);
            ScreenManager.registerFactory(ContainerFluidLevelEmitter.TYPE, GuiFluidLevelEmitter::new);
            ScreenManager.registerFactory(ContainerSpatialIOPort.TYPE, GuiSpatialIOPort::new);
            ScreenManager.registerFactory(ContainerInscriber.TYPE, GuiInscriber::new);
            ScreenManager.registerFactory(ContainerCellWorkbench.TYPE, GuiCellWorkbench::new);
            ScreenManager.registerFactory(ContainerMolecularAssembler.TYPE, GuiMolecularAssembler::new);
            ScreenManager.registerFactory(ContainerCraftAmount.TYPE, GuiCraftAmount::new);
            ScreenManager.registerFactory(ContainerCraftConfirm.TYPE, GuiCraftConfirm::new);
            ScreenManager.registerFactory(ContainerInterfaceTerminal.TYPE, GuiInterfaceTerminal::new);
            ScreenManager.registerFactory(ContainerCraftingStatus.TYPE, GuiCraftingStatus::new);
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

        GrinderRecipe.TYPE = new AERecipeType<>(GrinderRecipeSerializer.INSTANCE.getRegistryName());
        InscriberRecipe.TYPE = new AERecipeType<>(InscriberRecipeSerializer.INSTANCE.getRegistryName());

        ItemFacade facadeItem = (ItemFacade) Api.INSTANCE.definitions().items().facade().item();
        r.registerAll(
                DisassembleRecipe.SERIALIZER,
                GrinderRecipeSerializer.INSTANCE,
                InscriberRecipeSerializer.INSTANCE,
				FacadeRecipe.getSerializer(facadeItem)
        );

        CraftingHelper.register(FeaturesEnabled.Serializer.INSTANCE);
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
        final IRegistryContainer registries = AEApi.instance().registries();
        // TODO: Do not use the internal API
        ApiDefinitions definitions = Api.INSTANCE.definitions();
        final IParts parts = definitions.parts();
        final IBlocks blocks = definitions.blocks();
        final IItems items = definitions.items();

        // default settings..
        ((P2PTunnelRegistry) registries.p2pTunnel()).configure();

        // Interface
        Upgrades.CRAFTING.registerItem(parts.iface(), 1);
        Upgrades.CRAFTING.registerItem(blocks.iface(), 1);

        // IO Port!
        Upgrades.SPEED.registerItem(blocks.iOPort(), 3);
        Upgrades.REDSTONE.registerItem(blocks.iOPort(), 1);

        // Level Emitter!
        Upgrades.FUZZY.registerItem(parts.levelEmitter(), 1);
        Upgrades.CRAFTING.registerItem(parts.levelEmitter(), 1);

        // Import Bus
        Upgrades.FUZZY.registerItem(parts.importBus(), 1);
        Upgrades.REDSTONE.registerItem(parts.importBus(), 1);
        Upgrades.CAPACITY.registerItem(parts.importBus(), 2);
        Upgrades.SPEED.registerItem(parts.importBus(), 4);

        // Fluid Import Bus
        Upgrades.CAPACITY.registerItem(parts.fluidImportBus(), 2);
        Upgrades.REDSTONE.registerItem(parts.fluidImportBus(), 1);
        Upgrades.SPEED.registerItem(parts.fluidImportBus(), 4);

        // Export Bus
        Upgrades.FUZZY.registerItem(parts.exportBus(), 1);
        Upgrades.REDSTONE.registerItem(parts.exportBus(), 1);
        Upgrades.CAPACITY.registerItem(parts.exportBus(), 2);
        Upgrades.SPEED.registerItem(parts.exportBus(), 4);
        Upgrades.CRAFTING.registerItem(parts.exportBus(), 1);

        // Fluid Export Bus
        Upgrades.CAPACITY.registerItem(parts.fluidExportBus(), 2);
        Upgrades.REDSTONE.registerItem(parts.fluidExportBus(), 1);
        Upgrades.SPEED.registerItem(parts.fluidExportBus(), 4);

        // Storage Cells
        Upgrades.FUZZY.registerItem(items.cell1k(), 1);
        Upgrades.INVERTER.registerItem(items.cell1k(), 1);

        Upgrades.FUZZY.registerItem(items.cell4k(), 1);
        Upgrades.INVERTER.registerItem(items.cell4k(), 1);

        Upgrades.FUZZY.registerItem(items.cell16k(), 1);
        Upgrades.INVERTER.registerItem(items.cell16k(), 1);

        Upgrades.FUZZY.registerItem(items.cell64k(), 1);
        Upgrades.INVERTER.registerItem(items.cell64k(), 1);

        Upgrades.FUZZY.registerItem(items.portableCell(), 1);
        Upgrades.INVERTER.registerItem(items.portableCell(), 1);

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
        mr.whiteListTileEntity(AEBaseTile.class);

        /*
         * world gen
         */
        for (final IWorldGen.WorldGenType type : IWorldGen.WorldGenType.values()) {
            // FIXME: registries.worldgen().disableWorldGenForProviderID( type,
            // StorageWorldProvider.class );

            registries.worldgen().disableWorldGenForDimension(type, DimensionType.THE_NETHER.getRegistryName());
        }

        // whitelist from config
        for (final String dimension : AEConfig.instance().getMeteoriteDimensionWhitelist()) {
            registries.worldgen().enableWorldGenForDimension(IWorldGen.WorldGenType.METEORITES,
                    new ResourceLocation(dimension));
        }

        ForgeRegistries.BIOMES.forEach(b -> {
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

        b.addStructure(MeteoriteStructure.INSTANCE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
        b.addFeature(GenerationStage.Decoration.TOP_LAYER_MODIFICATION,
                MeteoriteStructure.INSTANCE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
                        .withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
    }

    private static void addQuartzWorldGen(Biome b) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.CERTUS_QUARTZ_WORLD_GEN)) {
            return;
        }

        BlockState quartzOre = AEApi.instance().definitions().blocks().quartzOre().block().getDefaultState();
        b.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                Feature.ORE
                        .withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE,
                                quartzOre, AEConfig.instance().getQuartzOresPerCluster()))
                        .withPlacement(Placement.COUNT_RANGE.configure(
                                new CountRangeConfig(AEConfig.instance().getQuartzOresClusterAmount(), 12, 12, 72))));

        if (AEConfig.instance().isFeatureEnabled(AEFeature.CHARGED_CERTUS_ORE)) {

            BlockState chargedQuartzOre = AEApi.instance().definitions().blocks().quartzOreCharged().block()
                    .getDefaultState();
            b.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION,
                    ChargedQuartzOreFeature.INSTANCE
                            .withConfiguration(new ChargedQuartzOreConfig(quartzOre, chargedQuartzOre,
                                    AEConfig.instance().getSpawnChargedChance()))
                            .withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));

        }
    }

    public void registerWorldGen(RegistryEvent.Register<Feature<?>> evt) {
        IForgeRegistry<Feature<?>> r = evt.getRegistry();

//        r.register(MeteoriteWorldGen.INSTANCE);
        r.register(MeteoriteStructure.INSTANCE);
        r.register(ChargedQuartzOreFeature.INSTANCE);
    }

    public void registerBiomes(RegistryEvent.Register<Biome> evt) {
        evt.getRegistry().register(StorageCellBiome.INSTANCE);
    }

    public void registerModDimension(RegistryEvent.Register<ModDimension> evt) {
        evt.getRegistry().register(StorageCellModDimension.INSTANCE);
    }

    @OnlyIn(Dist.CLIENT)
    public void registerTextures(TextureStitchEvent.Pre event) {
        SkyChestTESR.registerTextures(event);
        InscriberTESR.registerTexture(event);
    }

    public void registerCommands(final FMLServerStartingEvent evt) {
        new AECommand().register(evt.getCommandDispatcher());
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
