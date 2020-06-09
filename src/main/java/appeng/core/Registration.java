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


import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IParts;
import appeng.api.features.IRegistryContainer;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.networking.IGridCacheRegistry;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.bootstrap.ICriterionTriggerRegistry;
import appeng.bootstrap.IModelRegistry;
import appeng.bootstrap.components.*;
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
import appeng.client.gui.implementations.GuiMAC;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiMEPortableCell;
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
import appeng.client.render.cablebus.CableBusModel;
import appeng.client.render.effects.*;
import appeng.client.render.model.BiometricCardModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.client.render.model.SkyCompassModel;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpener;
import appeng.container.implementations.*;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.features.registries.PartModels;
import appeng.core.features.registries.cell.BasicCellHandler;
import appeng.core.features.registries.cell.BasicItemCellGuiHandler;
import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.AeStats;
import appeng.core.stats.PartItemPredicate;
import appeng.fluids.client.gui.*;
import appeng.fluids.container.*;
import appeng.fluids.registries.BasicFluidCellGuiHandler;
import appeng.me.cache.*;
import appeng.recipes.conditions.FeaturesEnabled;
import appeng.recipes.game.DisassembleRecipe;
import appeng.recipes.ingredients.PartIngredientSerializer;
import appeng.server.AECommand;
import appeng.tile.AEBaseTile;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Method;


final class Registration
{

	public Registration() {
		AeStats.register();
		advancementTriggers = new AdvancementTriggers( CriteriaTriggers::register );
	}

	//	DimensionType storageDimensionType;
//	int storageDimensionID;
//	Biome storageBiome;
	AdvancementTriggers advancementTriggers;
//
//	void preInitialize( final FMLPreInitializationEvent event )
//	{
//		Capabilities.register();
//
//		final Api api = AEApi.instance();
//		final IRecipeHandlerRegistry recipeRegistry = api.registries().recipes();
//		this.registerCraftHandlers( recipeRegistry );
//
//		MinecraftForge.EVENT_BUS.register( OreDictionaryHandler.INSTANCE );
//
//		ApiDefinitions definitions = api.definitions();
//
//		// Register
//		definitions.getRegistry().getBootstrapComponents( IPreInitComponent.class ).forEachRemaining( b -> b.preInitialize( event.getSide() ) );
//	}
//
//	private void registerSpatialBiome( IForgeRegistry<Biome> registry )
//	{
//		if( !AEConfig.instance().isFeatureEnabled( AEFeature.SPATIAL_IO ) )
//		{
//			return;
//		}
//
//		if( this.storageBiome == null )
//		{
//			this.storageBiome = new BiomeGenStorage();
//		}
//		registry.register( this.storageBiome.setRegistryName( "appliedenergistics2:storage_biome" ) );
//	}
//
//	private void registerSpatialDimension()
//	{
//		final AEConfig config = AEConfig.instance();
//		if( !config.isFeatureEnabled( AEFeature.SPATIAL_IO ) )
//		{
//			return;
//		}
//
//		if( config.getStorageProviderID() == -1 )
//		{
//			final Set<Integer> ids = new HashSet<>();
//			for( DimensionType type : DimensionType.values() )
//			{
//				ids.add( type.getId() );
//			}
//
//			int newId = -11;
//			while( ids.contains( newId ) )
//			{
//				--newId;
//			}
//			config.setStorageProviderID( newId );
//			config.save();
//		}
//
//		this.storageDimensionType = DimensionType.register( "Storage Cell", "_cell", config.getStorageProviderID(), StorageWorldProvider.class, true );
//
//		if( config.getStorageDimensionID() == -1 )
//		{
//			config.setStorageDimensionID( DimensionManager.getNextFreeDimId() );
//			config.save();
//		}
//		this.storageDimensionID = config.getStorageDimensionID();
//
//		DimensionManager.registerDimension( this.storageDimensionID, this.storageDimensionType );
//	}
//
//	private void registerCraftHandlers( final IRecipeHandlerRegistry registry )
//	{
//		registry.addNewSubItemResolver( new AEItemResolver() );
//	}

	public static void setupInternalRegistries()
	{
		// TODO: Do not use the internal API
		final Api api = Api.INSTANCE;
		final IRegistryContainer registries = api.registries();

		final IGridCacheRegistry gcr = registries.gridCache();
		gcr.registerGridCache( ITickManager.class, TickManagerCache.class );
		gcr.registerGridCache( IEnergyGrid.class, EnergyGridCache.class );
		gcr.registerGridCache( IPathingGrid.class, PathGridCache.class );
		gcr.registerGridCache( IStorageGrid.class, GridStorageCache.class );
		gcr.registerGridCache( P2PCache.class, P2PCache.class );
		gcr.registerGridCache( ISpatialCache.class, SpatialPylonCache.class );
		gcr.registerGridCache( ISecurityGrid.class, SecurityCache.class );
		gcr.registerGridCache( ICraftingGrid.class, CraftingGridCache.class );

		registries.cell().addCellHandler( new BasicCellHandler() );
		registries.cell().addCellHandler( new CreativeCellHandler() );
		registries.cell().addCellGuiHandler( new BasicItemCellGuiHandler() );
		registries.cell().addCellGuiHandler( new BasicFluidCellGuiHandler() );

		api.definitions().materials().matterBall().maybeStack( 1 ).ifPresent( ammoStack ->
		{
			final double weight = 32;

			registries.matterCannon().registerAmmo( ammoStack, weight );
		} );

		PartItemPredicate.register();
	}

//	@SubscribeEvent
//	public void registerBiomes( RegistryEvent.Register<Biome> event )
//	{
//		final IForgeRegistry<Biome> registry = event.getRegistry();
//		this.registerSpatialBiome( registry );
//	}
//
	@OnlyIn( Dist.CLIENT )
	public void modelRegistryEvent( ModelRegistryEvent event )
	{
		registerSpecialModels();

		// TODO: Do not use the internal API
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		final IModelRegistry registry = new IModelRegistry() {
			@Override
			public void registerItemVariants(Item item, ResourceLocation... names) {

			}

			@Override
			public void setCustomModelResourceLocation(Item item, int metadata, ModelResourceLocation model) {
				// FIXME: this does not actually work
				ItemModelMesher mesher = Minecraft.getInstance().getItemRenderer().getItemModelMesher();
				mesher.register(item, model);
			}
		};
		final Dist dist = FMLEnvironment.dist;
		definitions.getRegistry().getBootstrapComponents( IModelRegistrationComponent.class ).forEachRemaining(b -> b.modelRegistration( dist, registry ) );
	}

	/**
	 * Registers any JSON model files with Minecraft that are not referenced via blockstates or item IDs
	 */
	@OnlyIn(Dist.CLIENT)
	private void registerSpecialModels() {
		SkyCompassModel.DEPENDENCIES.forEach(ModelLoader::addSpecialModel);
		ModelLoader.addSpecialModel(BiometricCardModel.MODEL_BASE);
		ModelLoader.addSpecialModel(MemoryCardModel.MODEL_BASE);
		DriveModel.DEPENDENCIES.forEach(ModelLoader::addSpecialModel);

		PartModels partModels = (PartModels) Api.INSTANCE.registries().partModels();
		partModels.getModels().forEach(ModelLoader::addSpecialModel);
		partModels.setInitialized(true);
	}

	public void registerBlocks( RegistryEvent.Register<Block> event )
	{
		final IForgeRegistry<Block> registry = event.getRegistry();
		// TODO: Do not use the internal API
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		final Dist dist = FMLEnvironment.dist;
		definitions.getRegistry().getBootstrapComponents( IBlockRegistrationComponent.class ).forEachRemaining( b -> b.blockRegistration( dist, registry ) );
	}

	public void registerItems( RegistryEvent.Register<Item> event )
	{
		final IForgeRegistry<Item> registry = event.getRegistry();
		// TODO: Do not use the internal API
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		final Dist dist = FMLEnvironment.dist;
		definitions.getRegistry().getBootstrapComponents( IItemRegistrationComponent.class ).forEachRemaining( b -> b.itemRegistration( dist, registry ) );
	}

	public void registerTileEntities( RegistryEvent.Register<TileEntityType<?>> event )
	{
		final IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
		// TODO: Do not use the internal API
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		definitions.getRegistry().getBootstrapComponents( ITileEntityRegistrationComponent.class ).forEachRemaining(b -> b.register( registry ) );
	}

	public void registerContainerTypes( RegistryEvent.Register<ContainerType<?>> event ) {
		final IForgeRegistry<ContainerType<?>> registry = event.getRegistry();

		ContainerCellWorkbench.TYPE = registerContainer(
				registry,
				"cellworkbench",
				ContainerCellWorkbench::fromNetwork,
				ContainerCellWorkbench::open
		);
		ContainerChest.TYPE = registerContainer(
				registry,
				"chest",
				ContainerChest::fromNetwork,
				ContainerChest::open
		);
		ContainerCondenser.TYPE = registerContainer(
				registry,
				"condenser",
				ContainerCondenser::fromNetwork,
				ContainerCondenser::open
		);
		ContainerCraftAmount.TYPE = registerContainer(
				registry,
				"craftamount",
				ContainerCraftAmount::fromNetwork,
				ContainerCraftAmount::open
		);
		ContainerCraftConfirm.TYPE = registerContainer(
				registry,
				"craftconfirm",
				ContainerCraftConfirm::fromNetwork,
				ContainerCraftConfirm::open
		);
		ContainerCraftingCPU.TYPE = registerContainer(
				registry,
				"craftingcpu",
				ContainerCraftingCPU::fromNetwork,
				ContainerCraftingCPU::open
		);
		ContainerCraftingStatus.TYPE = registerContainer(
				registry,
				"craftingstatus",
				ContainerCraftingStatus::fromNetwork,
				ContainerCraftingStatus::open
		);
		ContainerCraftingTerm.TYPE = registerContainer(
				registry,
				"craftingterm",
				ContainerCraftingTerm::fromNetwork,
				ContainerCraftingTerm::open
		);
		ContainerDrive.TYPE = registerContainer(
				registry,
				"drive",
				ContainerDrive::fromNetwork,
				ContainerDrive::open
		);
		ContainerFormationPlane.TYPE = registerContainer(
				registry,
				"formationplane",
				ContainerFormationPlane::fromNetwork,
				ContainerFormationPlane::open
		);
		ContainerGrinder.TYPE = registerContainer(
				registry,
				"grinder",
				ContainerGrinder::fromNetwork,
				ContainerGrinder::open
		);
		ContainerInscriber.TYPE = registerContainer(
				registry,
				"inscriber",
				ContainerInscriber::fromNetwork,
				ContainerInscriber::open
		);
		ContainerInterface.TYPE = registerContainer(
				registry,
				"interface",
				ContainerInterface::fromNetwork,
				ContainerInterface::open
		);
		ContainerInterfaceTerminal.TYPE = registerContainer(
				registry,
				"interfaceterminal",
				ContainerInterfaceTerminal::fromNetwork,
				ContainerInterfaceTerminal::open
		);
		ContainerIOPort.TYPE = registerContainer(
				registry,
				"ioport",
				ContainerIOPort::fromNetwork,
				ContainerIOPort::open
		);
		ContainerLevelEmitter.TYPE = registerContainer(
				registry,
				"levelemitter",
				ContainerLevelEmitter::fromNetwork,
				ContainerLevelEmitter::open
		);
		ContainerMAC.TYPE = registerContainer(
				registry,
				"mac",
				ContainerMAC::fromNetwork,
				ContainerMAC::open
		);
		ContainerMEMonitorable.TYPE = registerContainer(
				registry,
				"memonitorable",
				ContainerMEMonitorable::fromNetwork,
				ContainerMEMonitorable::open
		);
		ContainerMEPortableCell.TYPE = registerContainer(
				registry,
				"meportablecell",
				ContainerMEPortableCell::fromNetwork,
				ContainerMEPortableCell::open
		);
		ContainerNetworkStatus.TYPE = registerContainer(
				registry,
				"networkstatus",
				ContainerNetworkStatus::fromNetwork,
				ContainerNetworkStatus::open
		);
		ContainerNetworkTool.TYPE = registerContainer(
				registry,
				"networktool",
				ContainerNetworkTool::fromNetwork,
				ContainerNetworkTool::open
		);
		ContainerPatternTerm.TYPE = registerContainer(
				registry,
				"patternterm",
				ContainerPatternTerm::fromNetwork,
				ContainerPatternTerm::open
		);
		ContainerPriority.TYPE = registerContainer(
				registry,
				"priority",
				ContainerPriority::fromNetwork,
				ContainerPriority::open
		);
		ContainerQNB.TYPE = registerContainer(
				registry,
				"qnb",
				ContainerQNB::fromNetwork,
				ContainerQNB::open
		);
		ContainerQuartzKnife.TYPE = registerContainer(
				registry,
				"quartzknife",
				ContainerQuartzKnife::fromNetwork,
				ContainerQuartzKnife::open
		);
		ContainerSecurityStation.TYPE = registerContainer(
				registry,
				"securitystation",
				ContainerSecurityStation::fromNetwork,
				ContainerSecurityStation::open
		);
		ContainerSkyChest.TYPE = registerContainer(
				registry,
				"skychest",
				ContainerSkyChest::fromNetwork,
				ContainerSkyChest::open
		);
		ContainerSpatialIOPort.TYPE = registerContainer(
				registry,
				"spatialioport",
				ContainerSpatialIOPort::fromNetwork,
				ContainerSpatialIOPort::open
		);
		ContainerStorageBus.TYPE = registerContainer(
				registry,
				"storagebus",
				ContainerStorageBus::fromNetwork,
				ContainerStorageBus::open
		);
		ContainerUpgradeable.TYPE = registerContainer(
				registry,
				"upgradeable",
				ContainerUpgradeable::fromNetwork,
				ContainerUpgradeable::open
		);
		ContainerVibrationChamber.TYPE = registerContainer(
				registry,
				"vibrationchamber",
				ContainerVibrationChamber::fromNetwork,
				ContainerVibrationChamber::open
		);
		ContainerWireless.TYPE = registerContainer(
				registry,
				"wireless",
				ContainerWireless::fromNetwork,
				ContainerWireless::open
		);
		ContainerWirelessTerm.TYPE = registerContainer(
				registry,
				"wirelessterm",
				ContainerWirelessTerm::fromNetwork,
				ContainerWirelessTerm::open
		);

		ContainerFluidFormationPlane.TYPE = registerContainer(
				registry,
				"fluid_formation_plane",
				ContainerFluidFormationPlane::fromNetwork,
				ContainerFluidFormationPlane::open
		);
		ContainerFluidIO.TYPE = registerContainer(
				registry,
				"fluid_io",
				ContainerFluidIO::fromNetwork,
				ContainerFluidIO::open
		);
		ContainerFluidInterface.TYPE = registerContainer(
				registry,
				"fluid_interface",
				ContainerFluidInterface::fromNetwork,
				ContainerFluidInterface::open
		);
		ContainerFluidLevelEmitter.TYPE = registerContainer(
				registry,
				"fluid_level_emitter",
				ContainerFluidLevelEmitter::fromNetwork,
				ContainerFluidLevelEmitter::open
		);
		ContainerFluidStorageBus.TYPE = registerContainer(
				registry,
				"fluid_storage_bus",
				ContainerFluidStorageBus::fromNetwork,
				ContainerFluidStorageBus::open
		);
		ContainerFluidTerminal.TYPE = registerContainer(
				registry,
				"fluid_terminal",
				ContainerFluidTerminal::fromNetwork,
				ContainerFluidTerminal::open
		);

		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			ScreenManager.registerFactory(ContainerGrinder.TYPE, GuiGrinder::new);
			ScreenManager.registerFactory(ContainerQNB.TYPE, GuiQNB::new);
			ScreenManager.registerFactory(ContainerSkyChest.TYPE, GuiSkyChest::new);
			ScreenManager.registerFactory(ContainerChest.TYPE, GuiChest::new);
			ScreenManager.registerFactory(ContainerWireless.TYPE, GuiWireless::new);
			ScreenManager.<ContainerMEMonitorable, GuiMEMonitorable<ContainerMEMonitorable>>registerFactory(ContainerMEMonitorable.TYPE, GuiMEMonitorable::new);
			ScreenManager.registerFactory(ContainerMEPortableCell.TYPE, GuiMEPortableCell::new);
			ScreenManager.registerFactory(ContainerWirelessTerm.TYPE, GuiWirelessTerm::new);
			ScreenManager.registerFactory(ContainerNetworkStatus.TYPE, GuiNetworkStatus::new);
			ScreenManager.<ContainerCraftingCPU,GuiCraftingCPU<ContainerCraftingCPU>>registerFactory(ContainerCraftingCPU.TYPE, GuiCraftingCPU::new);
			ScreenManager.registerFactory(ContainerNetworkTool.TYPE, GuiNetworkTool::new);
			ScreenManager.registerFactory(ContainerQuartzKnife.TYPE, GuiQuartzKnife::new);
			ScreenManager.registerFactory(ContainerDrive.TYPE, GuiDrive::new);
			ScreenManager.registerFactory(ContainerVibrationChamber.TYPE, GuiVibrationChamber::new);
			ScreenManager.registerFactory(ContainerCondenser.TYPE, GuiCondenser::new);
			ScreenManager.registerFactory(ContainerInterface.TYPE, GuiInterface::new);
			ScreenManager.registerFactory(ContainerFluidInterface.TYPE, GuiFluidInterface::new);
			ScreenManager.<ContainerUpgradeable, GuiUpgradeable<ContainerUpgradeable>>registerFactory(ContainerUpgradeable.TYPE, GuiUpgradeable::new);
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
			ScreenManager.registerFactory(ContainerMAC.TYPE, GuiMAC::new);
			ScreenManager.registerFactory(ContainerCraftAmount.TYPE, GuiCraftAmount::new);
			ScreenManager.registerFactory(ContainerCraftConfirm.TYPE, GuiCraftConfirm::new);
			ScreenManager.registerFactory(ContainerInterfaceTerminal.TYPE, GuiInterfaceTerminal::new);
			ScreenManager.registerFactory(ContainerCraftingStatus.TYPE, GuiCraftingStatus::new);
		});
	}

	private <T extends AEBaseContainer> ContainerType<T> registerContainer(IForgeRegistry<ContainerType<?>> registry,
																		   String id,
																		   IContainerFactory<T> factory,
																		   ContainerOpener.Opener<T> opener) {
		ContainerType<T> type = IForgeContainerType.create(factory);
		type.setRegistryName(AppEng.MOD_ID, id);
		registry.register(type);
		ContainerOpener.addOpener(type, opener);
		return type;
	}

	public void registerRecipeSerializers( RegistryEvent.Register<IRecipeSerializer<?>> event )
	{
		IForgeRegistry<IRecipeSerializer<?>> r = event.getRegistry();

		// TODO: Do not use the internal API
		final ApiDefinitions definitions = Api.INSTANCE.definitions();

		r.registerAll(
				DisassembleRecipe.SERIALIZER
//				FacadeRecipe.getSerializer( (ItemFacade) definitions.items().facade().item() ) FIXME reimplement facades
//				this.factories.put( new ResourceLocation( AppEng.MOD_ID, "inscriber" ), new InscriberHandler() ); FIXME re-implement machine recipes
//				this.factories.put( new ResourceLocation( AppEng.MOD_ID, "smelt" ), new SmeltingHandler() );
//				this.factories.put( new ResourceLocation( AppEng.MOD_ID, "grinder" ), new GrinderHandler() );
		);

		CraftingHelper.register( FeaturesEnabled.Serializer.INSTANCE );
	}


	public void registerEntities( RegistryEvent.Register<EntityType<?>> event )
	{
		final IForgeRegistry<EntityType<?>> registry = event.getRegistry();
		// TODO: Do not use the internal API
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		definitions.getRegistry().getBootstrapComponents( IEntityRegistrationComponent.class ).forEachRemaining(b -> b.entityRegistration( registry ) );
	}

	public void registerParticleTypes( RegistryEvent.Register<ParticleType<?>> event )
	{
		final IForgeRegistry<ParticleType<?>> registry = event.getRegistry();
		registry.register(ChargedOreFX.TYPE);
		registry.register(VibrantFX.TYPE);
		registry.register(LightningFX.TYPE);
	}

	public void registerParticleFactories(ParticleFactoryRegisterEvent event) {
		Minecraft.getInstance().particles.registerFactory(AssemblerFX.TYPE, AssemblerFX.Factory::new);
		Minecraft.getInstance().particles.registerFactory(ChargedOreFX.TYPE, ChargedOreFX.Factory::new);
		Minecraft.getInstance().particles.registerFactory(CraftingFx.TYPE, CraftingFx.Factory::new);
		Minecraft.getInstance().particles.registerFactory(EnergyFx.TYPE, EnergyFx.Factory::new);
		Minecraft.getInstance().particles.registerFactory(LightningArcFX.TYPE, LightningArcFX.Factory::new);
		Minecraft.getInstance().particles.registerFactory(LightningFX.TYPE, LightningFX.Factory::new);
		Minecraft.getInstance().particles.registerFactory(MatterCannonFX.TYPE, MatterCannonFX.Factory::new);
		Minecraft.getInstance().particles.registerFactory(VibrantFX.TYPE, VibrantFX.Factory::new);
	}

//
//	@SubscribeEvent
//	public void attachSpatialDimensionManager( AttachCapabilitiesEvent<World> event )
//	{
//		if( AEConfig.instance()
//				.isFeatureEnabled( AEFeature.SPATIAL_IO ) && event.getObject() == DimensionManager.getWorld( AEConfig.instance().getStorageDimensionID() ) )
//		{
//			event.addCapability( new ResourceLocation( "appliedenergistics2:spatial_dimension_manager" ), new SpatialDimensionManager( event.getObject() ) );
//		}
//	}

	// FIXME LATER
	public static void postInit()
	{
		final IRegistryContainer registries = AEApi.instance().registries();
		// TODO: Do not use the internal API
		ApiDefinitions definitions = Api.INSTANCE.definitions();
		final IParts parts = definitions.parts();
		final IBlocks blocks = definitions.blocks();
		final IItems items = definitions.items();

		// FIXME this.registerSpatialDimension();

		// default settings..
		( (P2PTunnelRegistry) registries.p2pTunnel() ).configure();

		// Interface
		Upgrades.CRAFTING.registerItem( parts.iface(), 1 );
		Upgrades.CRAFTING.registerItem( blocks.iface(), 1 );

		// IO Port!
		Upgrades.SPEED.registerItem( blocks.iOPort(), 3 );
		Upgrades.REDSTONE.registerItem( blocks.iOPort(), 1 );

		// Level Emitter!
		Upgrades.FUZZY.registerItem( parts.levelEmitter(), 1 );
		Upgrades.CRAFTING.registerItem( parts.levelEmitter(), 1 );

		// Import Bus
		Upgrades.FUZZY.registerItem( parts.importBus(), 1 );
		Upgrades.REDSTONE.registerItem( parts.importBus(), 1 );
		Upgrades.CAPACITY.registerItem( parts.importBus(), 2 );
		Upgrades.SPEED.registerItem( parts.importBus(), 4 );

		// Fluid Import Bus
		Upgrades.CAPACITY.registerItem( parts.fluidImportBus(), 2 );
		Upgrades.REDSTONE.registerItem( parts.fluidImportBus(), 1 );
		Upgrades.SPEED.registerItem( parts.fluidImportBus(), 4 );

		// Export Bus
		Upgrades.FUZZY.registerItem( parts.exportBus(), 1 );
		Upgrades.REDSTONE.registerItem( parts.exportBus(), 1 );
		Upgrades.CAPACITY.registerItem( parts.exportBus(), 2 );
		Upgrades.SPEED.registerItem( parts.exportBus(), 4 );
		Upgrades.CRAFTING.registerItem( parts.exportBus(), 1 );

		// Fluid Export Bus
		Upgrades.CAPACITY.registerItem( parts.fluidExportBus(), 2 );
		Upgrades.REDSTONE.registerItem( parts.fluidExportBus(), 1 );
		Upgrades.SPEED.registerItem( parts.fluidExportBus(), 4 );

		// Storage Cells
		Upgrades.FUZZY.registerItem( items.cell1k(), 1 );
		Upgrades.INVERTER.registerItem( items.cell1k(), 1 );

		Upgrades.FUZZY.registerItem( items.cell4k(), 1 );
		Upgrades.INVERTER.registerItem( items.cell4k(), 1 );

		Upgrades.FUZZY.registerItem( items.cell16k(), 1 );
		Upgrades.INVERTER.registerItem( items.cell16k(), 1 );

		Upgrades.FUZZY.registerItem( items.cell64k(), 1 );
		Upgrades.INVERTER.registerItem( items.cell64k(), 1 );

		Upgrades.FUZZY.registerItem( items.portableCell(), 1 );
		Upgrades.INVERTER.registerItem( items.portableCell(), 1 );

		Upgrades.FUZZY.registerItem( items.viewCell(), 1 );
		Upgrades.INVERTER.registerItem( items.viewCell(), 1 );

		// Storage Bus
		Upgrades.FUZZY.registerItem( parts.storageBus(), 1 );
		Upgrades.INVERTER.registerItem( parts.storageBus(), 1 );
		Upgrades.CAPACITY.registerItem( parts.storageBus(), 5 );

		// Storage Bus Fluids
		Upgrades.INVERTER.registerItem( parts.fluidStorageBus(), 1 );
		Upgrades.CAPACITY.registerItem( parts.fluidStorageBus(), 5 );

		// Formation Plane
		Upgrades.FUZZY.registerItem( parts.formationPlane(), 1 );
		Upgrades.INVERTER.registerItem( parts.formationPlane(), 1 );
		Upgrades.CAPACITY.registerItem( parts.formationPlane(), 5 );

		// Matter Cannon
		Upgrades.FUZZY.registerItem( items.massCannon(), 1 );
		Upgrades.INVERTER.registerItem( items.massCannon(), 1 );
		Upgrades.SPEED.registerItem( items.massCannon(), 4 );

		// Molecular Assembler
		Upgrades.SPEED.registerItem( blocks.molecularAssembler(), 5 );

		// Inscriber
		Upgrades.SPEED.registerItem( blocks.inscriber(), 3 );

		// Wireless Terminal Handler
		items.wirelessTerminal().maybeItem().ifPresent( terminal -> registries.wireless().registerWirelessHandler( (IWirelessTermHandler) terminal ) );

		// Charge Rates
		items.chargedStaff().maybeItem().ifPresent( chargedStaff -> registries.charger().addChargeRate( chargedStaff, 320d ) );
		items.portableCell().maybeItem().ifPresent( chargedStaff -> registries.charger().addChargeRate( chargedStaff, 800d ) );
		items.colorApplicator().maybeItem().ifPresent( colorApplicator -> registries.charger().addChargeRate( colorApplicator, 800d ) );
		items.wirelessTerminal().maybeItem().ifPresent( terminal -> registries.charger().addChargeRate( terminal, 8000d ) );
		items.entropyManipulator().maybeItem().ifPresent( entropyManipulator -> registries.charger().addChargeRate( entropyManipulator, 8000d ) );
		items.massCannon().maybeItem().ifPresent( massCannon -> registries.charger().addChargeRate( massCannon, 8000d ) );
		blocks.energyCell().maybeItem().ifPresent( cell -> registries.charger().addChargeRate( cell, 8000d ) );
		blocks.energyCellDense().maybeItem().ifPresent( cell -> registries.charger().addChargeRate( cell, 16000d ) );

// FIXME		// add villager trading to black smiths for a few basic materials
// FIXME		if( AEConfig.instance().isFeatureEnabled( AEFeature.VILLAGER_TRADING ) )
// FIXME		{
// FIXME			// TODO: VILLAGER TRADING
// FIXME			// VillagerRegistry.instance().getRegisteredVillagers().registerVillageTradeHandler( 3, new AETrading() );
// FIXME		}

// FIXME		if( AEConfig.instance().isFeatureEnabled( AEFeature.CERTUS_QUARTZ_WORLD_GEN ) )
// FIXME		{
// FIXME			GameRegistry.registerWorldGenerator( new QuartzWorldGen(), 0 );
// FIXME		}
// FIXME
// FIXME		if( AEConfig.instance().isFeatureEnabled( AEFeature.METEORITE_WORLD_GEN ) )
// FIXME		{
// FIXME			GameRegistry.registerWorldGenerator( new MeteoriteWorldGen(), 0 );
// FIXME		}

		final IMovableRegistry mr = registries.movable();

		/*
		 * You can't move bed rock.
		 */
		mr.blacklistBlock( net.minecraft.block.Blocks.BEDROCK );

		/*
		 * White List Vanilla...
		 */
		mr.whiteListTileEntity( net.minecraft.tileentity.BannerTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.BeaconTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.BrewingStandTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.ChestTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.CommandBlockTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.ComparatorTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.DaylightDetectorTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.DispenserTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.DropperTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.EnchantingTableTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.EnderChestTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.EndPortalTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.FurnaceTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.HopperTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.MobSpawnerTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.PistonTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.ShulkerBoxTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.SignTileEntity.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.SkullTileEntity.class );

		/*
		 * Whitelist AE2
		 */
		mr.whiteListTileEntity( AEBaseTile.class );

// FIXME		/*
// FIXME		 * world gen
// FIXME		 */
// FIXME		for( final WorldGenType type : WorldGenType.values() )
// FIXME		{
// FIXME			registries.worldgen().disableWorldGenForProviderID( type, StorageWorldProvider.class );
// FIXME
// FIXME			// nether
// FIXME			registries.worldgen().disableWorldGenForDimension( type, -1 );
// FIXME
// FIXME			// end
// FIXME			registries.worldgen().disableWorldGenForDimension( type, 1 );
// FIXME		}
// FIXME
// FIXME		// whitelist from config
// FIXME		for( final int dimension : AEConfig.instance().getMeteoriteDimensionWhitelist() )
// FIXME		{
// FIXME			registries.worldgen().enableWorldGenForDimension( WorldGenType.METEORITES, dimension );
// FIXME		}
	}

//	private static class ModelLoaderWrapper implements IModelRegistry
//	{
//
//		@Override
//		public void registerItemVariants( Item item, ResourceLocation... names )
//		{
//			ModelLoader.registerItemVariants( item, names );
//		}
//
//		@Override
//		public void setCustomModelResourceLocation( Item item, int metadata, ModelResourceLocation model )
//		{
//			ModelLoader.setCustomModelResourceLocation( item, metadata, model );
//		}
//
//		@Override
//		public void setCustomMeshDefinition( Item item, ItemMeshDefinition meshDefinition )
//		{
//			ModelLoader.setCustomMeshDefinition( item, meshDefinition );
//		}
//
//		@Override
//		public void setCustomStateMapper( Block block, IStateMapper mapper )
//		{
//			ModelLoader.setCustomStateMapper( block, mapper );
//		}
//	}

	public void registerTextures(TextureStitchEvent.Pre event) {
		SkyChestTESR.registerTextures(event);
	}

	public void registerCommands( final FMLServerStartingEvent evt )
	{
		new AECommand().register(evt.getCommandDispatcher());
	}

	@OnlyIn(Dist.CLIENT)
	public void registerItemColors(ColorHandlerEvent.Item event) {
		// TODO: Do not use the internal API
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		definitions.getRegistry().getBootstrapComponents( IItemColorRegistrationComponent.class ).forEachRemaining(c -> c.register(event.getItemColors(), event.getBlockColors()));
	}

	@OnlyIn(Dist.CLIENT)
	public void handleModelBake(ModelBakeEvent event) {
		// TODO: Do not use the internal API
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		definitions.getRegistry().getBootstrapComponents( IModelBakeComponent.class ).forEachRemaining(c -> c.onModelBakeEvent(event));
	}
}
