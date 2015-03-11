/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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


import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.Blocks;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.api.definitions.IParts;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
import appeng.api.definitions.Parts;
import appeng.api.features.IRecipeHandlerRegistry;
import appeng.api.features.IRegistryContainer;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWorldGen.WorldGenType;
import appeng.api.movable.IMovableRegistry;
import appeng.api.networking.IGridCacheRegistry;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.parts.IPartHelper;
import appeng.block.networking.BlockCableBus;
import appeng.core.features.AEFeature;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.features.registries.entries.BasicCellHandler;
import appeng.core.features.registries.entries.CreativeCellHandler;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.stats.PlayerStatsRegistration;
import appeng.hooks.AETrading;
import appeng.hooks.MeteoriteWorldGen;
import appeng.hooks.QuartzWorldGen;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationType;
import appeng.items.materials.ItemMultiMaterial;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cache.EnergyGridCache;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.P2PCache;
import appeng.me.cache.PathGridCache;
import appeng.me.cache.SecurityCache;
import appeng.me.cache.SpatialPylonCache;
import appeng.me.cache.TickManagerCache;
import appeng.me.storage.AEExternalHandler;
import appeng.parts.PartPlacement;
import appeng.recipes.AEItemResolver;
import appeng.recipes.RecipeHandler;
import appeng.recipes.game.DisassembleRecipe;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.game.ShapedRecipe;
import appeng.recipes.game.ShapelessRecipe;
import appeng.recipes.handlers.Crusher;
import appeng.recipes.handlers.Grind;
import appeng.recipes.handlers.GrindFZ;
import appeng.recipes.handlers.HCCrusher;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Macerator;
import appeng.recipes.handlers.MekCrusher;
import appeng.recipes.handlers.MekEnrichment;
import appeng.recipes.handlers.Press;
import appeng.recipes.handlers.Pulverizer;
import appeng.recipes.handlers.Shaped;
import appeng.recipes.handlers.Shapeless;
import appeng.recipes.handlers.Smelt;
import appeng.recipes.loader.ConfigLoader;
import appeng.recipes.loader.JarLoader;
import appeng.recipes.ores.OreDictionaryHandler;
import appeng.spatial.BiomeGenStorage;
import appeng.spatial.StorageWorldProvider;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


public final class Registration
{
	final public static Registration INSTANCE = new Registration();

	private final RecipeHandler recipeHandler;
	public BiomeGenBase storageBiome;

	private Registration()
	{
		this.recipeHandler = new RecipeHandler();
	}

	public void preInitialize( FMLPreInitializationEvent event )
	{
		this.registerSpatial( false );

		final Api api = Api.INSTANCE;
		IRecipeHandlerRegistry recipeRegistry = api.registries().recipes();
		this.registerCraftHandlers( recipeRegistry );

		RecipeSorter.register( "AE2-Facade", FacadeRecipe.class, Category.SHAPED, "" );
		RecipeSorter.register( "AE2-Shaped", ShapedRecipe.class, Category.SHAPED, "" );
		RecipeSorter.register( "AE2-Shapeless", ShapelessRecipe.class, Category.SHAPELESS, "" );

		MinecraftForge.EVENT_BUS.register( OreDictionaryHandler.INSTANCE );

		final ApiDefinitions definitions = api.definitions();

		final IBlocks apiBlocks = definitions.blocks();
		final IItems apiItems = definitions.items();
		final IMaterials apiMaterials = definitions.materials();
		final IParts apiParts = definitions.parts();

		final Items items = api.items();
		final Materials materials = api.materials();
		final Parts parts = api.parts();
		final Blocks blocks = api.blocks();

		this.assignMaterials( materials, apiMaterials );
		this.assignParts( parts, apiParts );
		this.assignBlocks( blocks, apiBlocks );
		this.assignItems( items, apiItems );

		// Register all detected handlers and features (items, blocks) in pre-init
		for ( IFeatureHandler handler : definitions.getFeatureHandlerRegistry().getRegisteredFeatureHandlers() )
		{
			handler.register();
		}

		for ( IAEFeature feature : definitions.getFeatureRegistry().getRegisteredFeatures() )
		{
			feature.postInit();
		}
	}

	private void registerSpatial( boolean force )
	{
		if ( !AEConfig.instance.isFeatureEnabled( AEFeature.SpatialIO ) )
			return;

		AEConfig config = AEConfig.instance;

		if ( this.storageBiome == null )
		{
			if ( force && config.storageBiomeID == -1 )
			{
				config.storageBiomeID = Platform.findEmpty( BiomeGenBase.getBiomeGenArray() );
				if ( config.storageBiomeID == -1 )
					throw new RuntimeException( "Biome Array is full, please free up some Biome ID's or disable spatial." );

				this.storageBiome = new BiomeGenStorage( config.storageBiomeID );
				config.save();
			}

			if ( !force && config.storageBiomeID != -1 )
				this.storageBiome = new BiomeGenStorage( config.storageBiomeID );
		}

		if ( config.storageProviderID != -1 )
		{
			DimensionManager.registerProviderType( config.storageProviderID, StorageWorldProvider.class, false );
		}

		if ( config.storageProviderID == -1 && force )
		{
			config.storageProviderID = -11;

			while ( !DimensionManager.registerProviderType( config.storageProviderID, StorageWorldProvider.class, false ) )
				config.storageProviderID--;

			config.save();
		}
	}

	private void registerCraftHandlers( IRecipeHandlerRegistry registry )
	{
		registry.addNewSubItemResolver( new AEItemResolver() );

		registry.addNewCraftHandler( "hccrusher", HCCrusher.class );
		registry.addNewCraftHandler( "mekcrusher", MekCrusher.class );
		registry.addNewCraftHandler( "mekechamber", MekEnrichment.class );
		registry.addNewCraftHandler( "grind", Grind.class );
		registry.addNewCraftHandler( "crusher", Crusher.class );
		registry.addNewCraftHandler( "grindfz", GrindFZ.class );
		registry.addNewCraftHandler( "pulverizer", Pulverizer.class );
		registry.addNewCraftHandler( "macerator", Macerator.class );

		registry.addNewCraftHandler( "smelt", Smelt.class );
		registry.addNewCraftHandler( "inscribe", Inscribe.class );
		registry.addNewCraftHandler( "press", Press.class );

		registry.addNewCraftHandler( "shaped", Shaped.class );
		registry.addNewCraftHandler( "shapeless", Shapeless.class );
	}

	/**
	 * Assigns materials from the new API to the old API
	 *
	 * @param target old API
	 * @param source new API
	 *
	 * @deprecated to be removed when the public definition API is removed
	 */
	@Deprecated
	private void assignMaterials( Materials target, IMaterials source )
	{
		target.materialCell2SpatialPart = source.cell2SpatialPart();
		target.materialCell16SpatialPart = source.cell16SpatialPart();
		target.materialCell128SpatialPart = source.cell128SpatialPart();

		target.materialSilicon = source.silicon();
		target.materialSkyDust = source.skyDust();

		target.materialCalcProcessorPress = source.calcProcessorPress();
		target.materialEngProcessorPress = source.engProcessorPress();
		target.materialLogicProcessorPress = source.logicProcessorPress();

		target.materialCalcProcessorPrint = source.calcProcessorPrint();
		target.materialEngProcessorPrint = source.engProcessorPrint();
		target.materialLogicProcessorPrint = source.logicProcessorPrint();

		target.materialSiliconPress = source.siliconPress();
		target.materialSiliconPrint = source.siliconPrint();

		target.materialNamePress = source.namePress();

		target.materialLogicProcessor = source.logicProcessor();
		target.materialCalcProcessor = source.calcProcessor();
		target.materialEngProcessor = source.engProcessor();

		target.materialBasicCard = source.basicCard();
		target.materialAdvCard = source.advCard();

		target.materialPurifiedCertusQuartzCrystal = source.purifiedCertusQuartzCrystal();
		target.materialPurifiedNetherQuartzCrystal = source.purifiedNetherQuartzCrystal();
		target.materialPurifiedFluixCrystal = source.purifiedFluixCrystal();

		target.materialCell1kPart = source.cell1kPart();
		target.materialCell4kPart = source.cell4kPart();
		target.materialCell16kPart = source.cell16kPart();
		target.materialCell64kPart = source.cell64kPart();
		target.materialEmptyStorageCell = source.emptyStorageCell();

		target.materialCardRedstone = source.cardRedstone();
		target.materialCardSpeed = source.cardSpeed();
		target.materialCardCapacity = source.cardCapacity();
		target.materialCardFuzzy = source.cardFuzzy();
		target.materialCardInverter = source.cardInverter();
		target.materialCardCrafting = source.cardCrafting();

		target.materialEnderDust = source.enderDust();
		target.materialFlour = source.flour();
		target.materialGoldDust = source.goldDust();
		target.materialIronDust = source.ironDust();
		target.materialFluixDust = source.fluixDust();
		target.materialCertusQuartzDust = source.certusQuartzDust();
		target.materialNetherQuartzDust = source.netherQuartzDust();

		target.materialMatterBall = source.matterBall();
		target.materialIronNugget = source.ironNugget();

		target.materialCertusQuartzCrystal = source.certusQuartzCrystal();
		target.materialCertusQuartzCrystalCharged = source.certusQuartzCrystalCharged();
		target.materialFluixCrystal = source.fluixCrystal();
		target.materialFluixPearl = source.fluixPearl();

		target.materialWoodenGear = source.woodenGear();

		target.materialWireless = source.wireless();
		target.materialWirelessBooster = source.wirelessBooster();

		target.materialAnnihilationCore = source.annihilationCore();
		target.materialFormationCore = source.formationCore();

		target.materialSingularity = source.singularity();
		target.materialQESingularity = source.qESingularity();
		target.materialBlankPattern = source.blankPattern();
	}

	/**
	 * Assigns parts from the new API to the old API
	 *
	 * @param target old API
	 * @param source new API
	 *
	 * @deprecated to be removed when the public definition API is removed
	 */
	@Deprecated
	private void assignParts( Parts target, IParts source )
	{
		target.partCableSmart = source.cableSmart();
		target.partCableCovered = source.cableCovered();
		target.partCableGlass = source.cableGlass();
		target.partCableDense = source.cableDense();
//		target.partLumenCableSmart = source.lumenCableSmart();
//		target.partLumenCableCovered = source.lumenCableCovered();
//		target.partLumenCableGlass = source.lumenCableGlass();
//		target.partLumenCableDense = source.lumenCableDense();
		target.partQuartzFiber = source.quartzFiber();
		target.partToggleBus = source.toggleBus();
		target.partInvertedToggleBus = source.invertedToggleBus();
		target.partStorageBus = source.storageBus();
		target.partImportBus = source.importBus();
		target.partExportBus = source.exportBus();
		target.partInterface = source.iface();
		target.partLevelEmitter = source.levelEmitter();
		target.partAnnihilationPlane = source.annihilationPlane();
		target.partFormationPlane = source.formationPlane();
		target.partP2PTunnelME = target.partP2PTunnelRedstone = target.partP2PTunnelItems = target.partP2PTunnelLiquids = target.partP2PTunnelMJ = target.partP2PTunnelEU = target.partP2PTunnelRF = target.partP2PTunnelLight = target.partCableAnchor = source.cableAnchor();
		target.partMonitor = source.monitor();
		target.partSemiDarkMonitor = source.semiDarkMonitor();
		target.partDarkMonitor = source.darkMonitor();
		target.partInterfaceTerminal = source.interfaceTerminal();
		target.partPatternTerminal = source.patternTerminal();
		target.partCraftingTerminal = source.craftingTerminal();
		target.partTerminal = source.terminal();
		target.partStorageMonitor = source.storageMonitor();
		target.partConversionMonitor = source.conversionMonitor();
	}

	/**
	 * Assigns blocks from the new API to the old API
	 *
	 * @param target old API
	 * @param source new API
	 *
	 * @deprecated to be removed when the public definition API is removed
	 */
	@Deprecated
	private void assignBlocks( Blocks target, IBlocks source )
	{
		target.blockMultiPart = source.multiPart();

		target.blockCraftingUnit = source.craftingUnit();
		target.blockCraftingAccelerator = source.craftingAccelerator();
		target.blockCraftingMonitor = source.craftingMonitor();
		target.blockCraftingStorage1k = source.craftingStorage1k();
		target.blockCraftingStorage4k = source.craftingStorage4k();
		target.blockCraftingStorage16k = source.craftingStorage16k();
		target.blockCraftingStorage64k = source.craftingStorage64k();
		target.blockMolecularAssembler = source.molecularAssembler();

		target.blockQuartzOre = source.quartzOre();
		target.blockQuartzOreCharged = source.quartzOreCharged();
		target.blockMatrixFrame = source.matrixFrame();
		target.blockQuartz = source.quartz();
		target.blockFluix = source.fluix();
		target.blockSkyStone = source.skyStone();
		target.blockSkyChest = source.skyChest();
		target.blockSkyCompass = source.skyCompass();

		target.blockQuartzGlass = source.quartzGlass();
		target.blockQuartzVibrantGlass = source.quartzVibrantGlass();
		target.blockQuartzPillar = source.quartzPillar();
		target.blockQuartzChiseled = source.quartzChiseled();
		target.blockQuartzTorch = source.quartzTorch();
		target.blockLightDetector = source.lightDetector();
		target.blockCharger = source.charger();
		target.blockQuartzGrowthAccelerator = source.quartzGrowthAccelerator();

		target.blockGrindStone = source.grindStone();
		target.blockCrankHandle = source.crankHandle();
		target.blockInscriber = source.inscriber();
		target.blockWireless = source.wireless();
		target.blockTinyTNT = source.tinyTNT();

		target.blockQuantumRing = source.quantumRing();
		target.blockQuantumLink = source.quantumLink();

		target.blockSpatialPylon = source.spatialPylon();
		target.blockSpatialIOPort = source.spatialIOPort();

		target.blockController = source.controller();
		target.blockDrive = source.drive();
		target.blockChest = source.chest();
		target.blockInterface = source.iface();
		target.blockCellWorkbench = source.cellWorkbench();
		target.blockIOPort = source.iOPort();
		target.blockCondenser = source.condenser();
		target.blockEnergyAcceptor = source.energyAcceptor();
		target.blockVibrationChamber = source.vibrationChamber();

		target.blockEnergyCell = source.energyCell();
		target.blockEnergyCellDense = source.energyCellDense();
		target.blockEnergyCellCreative = source.energyCellCreative();

		target.blockSecurity = source.security();
		target.blockPaint = source.paint();
	}

	/**
	 * Assigns materials from the new API to the old API
	 *
	 * @param target old API
	 * @param source new API
	 *
	 * @deprecated to be removed when the public definition API is removed
	 */
	@Deprecated
	private void assignItems( Items target, IItems source )
	{
		target.itemCellCreative = source.cellCreative();
		target.itemViewCell = source.viewCell();
		target.itemEncodedPattern = source.encodedPattern();

		target.itemCell1k = source.cell1k();
		target.itemCell4k = source.cell4k();
		target.itemCell16k = source.cell16k();
		target.itemCell64k = source.cell64k();

		target.itemSpatialCell2 = source.spatialCell2();
		target.itemSpatialCell16 = source.spatialCell16();
		target.itemSpatialCell128 = source.spatialCell128();

		target.itemCertusQuartzKnife = source.certusQuartzKnife();
		target.itemCertusQuartzWrench = source.certusQuartzWrench();
		target.itemCertusQuartzAxe = source.certusQuartzAxe();
		target.itemCertusQuartzHoe = source.certusQuartzHoe();
		target.itemCertusQuartzPick = source.certusQuartzPick();
		target.itemCertusQuartzShovel = source.certusQuartzShovel();
		target.itemCertusQuartzSword = source.certusQuartzSword();

		target.itemNetherQuartzKnife = source.netherQuartzKnife();
		target.itemNetherQuartzWrench = source.netherQuartzWrench();
		target.itemNetherQuartzAxe = source.netherQuartzAxe();
		target.itemNetherQuartzHoe = source.netherQuartzHoe();
		target.itemNetherQuartzPick = source.netherQuartzPick();
		target.itemNetherQuartzShovel = source.netherQuartzShovel();
		target.itemNetherQuartzSword = source.netherQuartzSword();

		target.itemMassCannon = source.massCannon();
		target.itemMemoryCard = source.memoryCard();
		target.itemChargedStaff = source.chargedStaff();
		target.itemEntropyManipulator = source.entropyManipulator();
		target.itemColorApplicator = source.colorApplicator();

		target.itemWirelessTerminal = source.wirelessTerminal();
		target.itemNetworkTool = source.networkTool();
		target.itemPortableCell = source.portableCell();
		target.itemBiometricCard = source.biometricCard();

		target.itemFacade = source.facade();
		target.itemCrystalSeed = source.crystalSeed();

		target.itemPaintBall = source.coloredPaintBall();
		target.itemLumenPaintBall = source.coloredLumenPaintBall();
	}

	public void initialize( FMLInitializationEvent event )
	{
		final IAppEngApi api = AEApi.instance();
		final IPartHelper partHelper = api.partHelper();
		final IRegistryContainer registries = api.registries();

		// Perform ore camouflage!
		ItemMultiMaterial.instance.makeUnique();

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.CustomRecipes ) )
			this.recipeHandler.parseRecipes( new ConfigLoader( AppEng.instance.getConfigPath() ), "index.recipe" );
		else
			this.recipeHandler.parseRecipes( new JarLoader( "/assets/appliedenergistics2/recipes/" ), "index.recipe" );

		partHelper.registerNewLayer( "appeng.parts.layers.LayerISidedInventory", "net.minecraft.inventory.ISidedInventory" );
		partHelper.registerNewLayer( "appeng.parts.layers.LayerIFluidHandler", "net.minecraftforge.fluids.IFluidHandler" );
		partHelper.registerNewLayer( "appeng.parts.layers.LayerITileStorageMonitorable", "appeng.api.implementations.tiles.ITileStorageMonitorable" );

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.IC2 ) )
		{
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIEnergySink", "ic2.api.energy.tile.IEnergySink" );
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIEnergySource", "ic2.api.energy.tile.IEnergySource" );
		}

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.RF ) )
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIEnergyHandler", "cofh.api.energy.IEnergyReceiver" );

		FMLCommonHandler.instance().bus().register( TickHandler.INSTANCE );
		MinecraftForge.EVENT_BUS.register( TickHandler.INSTANCE );

		PartPlacement pp = new PartPlacement();
		MinecraftForge.EVENT_BUS.register( pp );
		FMLCommonHandler.instance().bus().register( pp );

		IGridCacheRegistry gcr = registries.gridCache();
		gcr.registerGridCache( ITickManager.class, TickManagerCache.class );
		gcr.registerGridCache( IEnergyGrid.class, EnergyGridCache.class );
		gcr.registerGridCache( IPathingGrid.class, PathGridCache.class );
		gcr.registerGridCache( IStorageGrid.class, GridStorageCache.class );
		gcr.registerGridCache( P2PCache.class, P2PCache.class );
		gcr.registerGridCache( ISpatialCache.class, SpatialPylonCache.class );
		gcr.registerGridCache( ISecurityGrid.class, SecurityCache.class );
		gcr.registerGridCache( ICraftingGrid.class, CraftingGridCache.class );

		registries.externalStorage().addExternalStorageInterface( new AEExternalHandler() );

		registries.cell().addCellHandler( new BasicCellHandler() );
		registries.cell().addCellHandler( new CreativeCellHandler() );

		registries.matterCannon().registerAmmo( api.definitions().materials().matterBall().stack( 1 ), 32.0 );

		this.recipeHandler.injectRecipes();

		final PlayerStatsRegistration registration = new PlayerStatsRegistration( FMLCommonHandler.instance().bus(), AEConfig.instance );
		registration.registerAchievementHandlers();
		registration.registerAchievements();

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.enableDisassemblyCrafting ) )
			CraftingManager.getInstance().getRecipeList().add( new DisassembleRecipe() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.enableFacadeCrafting ) )
			CraftingManager.getInstance().getRecipeList().add( new FacadeRecipe() );
	}

	public void postInit( FMLPostInitializationEvent event )
	{
		this.registerSpatial( true );

		final IAppEngApi api = AEApi.instance();
		final IRegistryContainer registries = api.registries();
		final IDefinitions definitions = api.definitions();
		final IParts parts = definitions.parts();
		final IBlocks blocks = definitions.blocks();
		final IItems items = definitions.items();

		// default settings..
		( (P2PTunnelRegistry) registries.p2pTunnel() ).configure();

		// add to localization..
		PlayerMessages.values();
		GuiText.values();

		Api.INSTANCE.getPartHelper().initFMPSupport();
		( (BlockCableBus) blocks.multiPart().block() ).setupTile();

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

		// Export Bus
		Upgrades.FUZZY.registerItem( parts.exportBus(), 1 );
		Upgrades.REDSTONE.registerItem( parts.exportBus(), 1 );
		Upgrades.CAPACITY.registerItem( parts.exportBus(), 2 );
		Upgrades.SPEED.registerItem( parts.exportBus(), 4 );
		Upgrades.CRAFTING.registerItem( parts.exportBus(), 1 );

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

		registries.wireless().registerWirelessHandler( (IWirelessTermHandler) items.wirelessTerminal().item() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.ChestLoot ) )
		{
			ChestGenHooks d = ChestGenHooks.getInfo( ChestGenHooks.MINESHAFT_CORRIDOR );

			final IMaterials materials = definitions.materials();

			d.addItem( new WeightedRandomChestContent( materials.certusQuartzCrystal().stack( 1 ), 1, 4, 2 ) );
			d.addItem( new WeightedRandomChestContent( materials.certusQuartzDust().stack( 1 ), 1, 4, 2 ) );
		}

		// add villager trading to black smiths for a few basic materials
		if ( AEConfig.instance.isFeatureEnabled( AEFeature.VillagerTrading ) )
			VillagerRegistry.instance().registerVillageTradeHandler( 3, new AETrading() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.CertusQuartzWorldGen ) )
			GameRegistry.registerWorldGenerator( new QuartzWorldGen(), 0 );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.MeteoriteWorldGen ) )
		{
			GameRegistry.registerWorldGenerator( new MeteoriteWorldGen(), 0 );
		}

		IMovableRegistry mr = registries.movable();

		/**
		 * You can't move bed rock.
		 */
		mr.blacklistBlock( net.minecraft.init.Blocks.bedrock );

		/*
		 * White List Vanilla...
		 */
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityBeacon.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityBrewingStand.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityChest.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityCommandBlock.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityComparator.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityDaylightDetector.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityDispenser.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityDropper.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityEnchantmentTable.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityEnderChest.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityEndPortal.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntitySkull.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityFurnace.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityMobSpawner.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntitySign.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityPiston.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityFlowerPot.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityNote.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityHopper.class );

		/**
		 * Whitelist AE2
		 */
		mr.whiteListTileEntity( AEBaseTile.class );

		/**
		 * world gen
		 */
		for ( WorldGenType type : WorldGenType.values() )
		{
			registries.worldgen().disableWorldGenForProviderID( type, StorageWorldProvider.class );

			// nether
			registries.worldgen().disableWorldGenForDimension( type, -1 );

			// end
			registries.worldgen().disableWorldGenForDimension( type, 1 );
		}

		// whitelist from config
		for ( int dimension : AEConfig.instance.meteoriteDimensionWhitelist )
		{
			registries.worldgen().enableWorldGenForDimension( WorldGenType.Meteorites, dimension );
		}

		/**
		 * initial recipe bake, if ore dictionary changes after this it re-bakes.
		 */
		OreDictionaryHandler.INSTANCE.bakeRecipes();
	}
}
