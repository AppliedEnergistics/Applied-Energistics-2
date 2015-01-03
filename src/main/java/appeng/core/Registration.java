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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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
import appeng.api.util.AEItemDefinition;
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
		target.materialCell2SpatialPart = source.cell2SpatialPart().orNull();
		target.materialCell16SpatialPart = source.cell16SpatialPart().orNull();
		target.materialCell128SpatialPart = source.cell128SpatialPart().orNull();

		target.materialSilicon = source.silicon().orNull();
		target.materialSkyDust = source.skyDust().orNull();

		target.materialCalcProcessorPress = source.calcProcessorPress().orNull();
		target.materialEngProcessorPress = source.engProcessorPress().orNull();
		target.materialLogicProcessorPress = source.logicProcessorPress().orNull();

		target.materialCalcProcessorPrint = source.calcProcessorPrint().orNull();
		target.materialEngProcessorPrint = source.engProcessorPrint().orNull();
		target.materialLogicProcessorPrint = source.logicProcessorPrint().orNull();

		target.materialSiliconPress = source.siliconPress().orNull();
		target.materialSiliconPrint = source.siliconPrint().orNull();

		target.materialNamePress = source.namePress().orNull();

		target.materialLogicProcessor = source.logicProcessor().orNull();
		target.materialCalcProcessor = source.calcProcessor().orNull();
		target.materialEngProcessor = source.engProcessor().orNull();

		target.materialBasicCard = source.basicCard().orNull();
		target.materialAdvCard = source.advCard().orNull();

		target.materialPurifiedCertusQuartzCrystal = source.purifiedCertusQuartzCrystal().orNull();
		target.materialPurifiedNetherQuartzCrystal = source.purifiedNetherQuartzCrystal().orNull();
		target.materialPurifiedFluixCrystal = source.purifiedFluixCrystal().orNull();

		target.materialCell1kPart = source.cell1kPart().orNull();
		target.materialCell4kPart = source.cell4kPart().orNull();
		target.materialCell16kPart = source.cell16kPart().orNull();
		target.materialCell64kPart = source.cell64kPart().orNull();
		target.materialEmptyStorageCell = source.emptyStorageCell().orNull();

		target.materialCardRedstone = source.cardRedstone().orNull();
		target.materialCardSpeed = source.cardSpeed().orNull();
		target.materialCardCapacity = source.cardCapacity().orNull();
		target.materialCardFuzzy = source.cardFuzzy().orNull();
		target.materialCardInverter = source.cardInverter().orNull();
		target.materialCardCrafting = source.cardCrafting().orNull();

		target.materialEnderDust = source.enderDust().orNull();
		target.materialFlour = source.flour().orNull();
		target.materialGoldDust = source.goldDust().orNull();
		target.materialIronDust = source.ironDust().orNull();
		target.materialFluixDust = source.fluixDust().orNull();
		target.materialCertusQuartzDust = source.certusQuartzDust().orNull();
		target.materialNetherQuartzDust = source.netherQuartzDust().orNull();

		target.materialMatterBall = source.matterBall().orNull();
		target.materialIronNugget = source.ironNugget().orNull();

		target.materialCertusQuartzCrystal = source.certusQuartzCrystal().orNull();
		target.materialCertusQuartzCrystalCharged = source.certusQuartzCrystalCharged().orNull();
		target.materialFluixCrystal = source.fluixCrystal().orNull();
		target.materialFluixPearl = source.fluixPearl().orNull();

		target.materialWoodenGear = source.woodenGear().orNull();

		target.materialWireless = source.wireless().orNull();
		target.materialWirelessBooster = source.wirelessBooster().orNull();

		target.materialAnnihilationCore = source.annihilationCore().orNull();
		target.materialFormationCore = source.formationCore().orNull();

		target.materialSingularity = source.singularity().orNull();
		target.materialQESingularity = source.qESingularity().orNull();
		target.materialBlankPattern = source.blankPattern().orNull();
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
		target.partCableSmart = source.cableSmart().orNull();
		target.partCableCovered = source.cableCovered().orNull();
		target.partCableGlass = source.cableGlass().orNull();
		target.partCableDense = source.cableDense().orNull();
		target.partLumenCableSmart = source.lumenCableSmart().orNull();
		target.partLumenCableCovered = source.lumenCableCovered().orNull();
		target.partLumenCableGlass = source.lumenCableGlass().orNull();
		target.partLumenCableDense = source.lumenCableDense().orNull();
		target.partQuartzFiber = source.quartzFiber().orNull();
		target.partToggleBus = source.toggleBus().orNull();
		target.partInvertedToggleBus = source.invertedToggleBus().orNull();
		target.partStorageBus = source.storageBus().orNull();
		target.partImportBus = source.importBus().orNull();
		target.partExportBus = source.exportBus().orNull();
		target.partInterface = source.iface().orNull();
		target.partLevelEmitter = source.levelEmitter().orNull();
		target.partAnnihilationPlane = source.annihilationPlane().orNull();
		target.partFormationPlane = source.formationPlane().orNull();
		target.partP2PTunnelME = target.partP2PTunnelRedstone = target.partP2PTunnelItems = target.partP2PTunnelLiquids = target.partP2PTunnelMJ = target.partP2PTunnelEU = target.partP2PTunnelRF = target.partP2PTunnelLight = target.partCableAnchor = source.cableAnchor().orNull();
		target.partMonitor = source.monitor().orNull();
		target.partSemiDarkMonitor = source.semiDarkMonitor().orNull();
		target.partDarkMonitor = source.darkMonitor().orNull();
		target.partInterfaceTerminal = source.interfaceTerminal().orNull();
		target.partPatternTerminal = source.patternTerminal().orNull();
		target.partCraftingTerminal = source.craftingTerminal().orNull();
		target.partTerminal = source.terminal().orNull();
		target.partStorageMonitor = source.storageMonitor().orNull();
		target.partConversionMonitor = source.conversionMonitor().orNull();
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
		target.blockMultiPart = source.multiPart().orNull();

		target.blockCraftingUnit = source.craftingUnit().orNull();
		target.blockCraftingAccelerator = source.craftingAccelerator().orNull();
		target.blockCraftingMonitor = source.craftingMonitor().orNull();
		target.blockCraftingStorage1k = source.craftingStorage1k().orNull();
		target.blockCraftingStorage4k = source.craftingStorage4k().orNull();
		target.blockCraftingStorage16k = source.craftingStorage16k().orNull();
		target.blockCraftingStorage64k = source.craftingStorage64k().orNull();
		target.blockMolecularAssembler = source.molecularAssembler().orNull();

		target.blockQuartzOre = source.quartzOre().orNull();
		target.blockQuartzOreCharged = source.quartzOreCharged().orNull();
		target.blockMatrixFrame = source.matrixFrame().orNull();
		target.blockQuartz = source.quartz().orNull();
		target.blockFluix = source.fluix().orNull();
		target.blockSkyStone = source.skyStone().orNull();
		target.blockSkyChest = source.skyChest().orNull();
		target.blockSkyCompass = source.skyCompass().orNull();

		target.blockQuartzGlass = source.quartzGlass().orNull();
		target.blockQuartzVibrantGlass = source.quartzVibrantGlass().orNull();
		target.blockQuartzPillar = source.quartzPillar().orNull();
		target.blockQuartzChiseled = source.quartzChiseled().orNull();
		target.blockQuartzTorch = source.quartzTorch().orNull();
		target.blockLightDetector = source.lightDetector().orNull();
		target.blockCharger = source.charger().orNull();
		target.blockQuartzGrowthAccelerator = source.quartzGrowthAccelerator().orNull();

		target.blockGrindStone = source.grindStone().orNull();
		target.blockCrankHandle = source.crankHandle().orNull();
		target.blockInscriber = source.inscriber().orNull();
		target.blockWireless = source.wireless().orNull();
		target.blockTinyTNT = source.tinyTNT().orNull();

		target.blockQuantumRing = source.quantumRing().orNull();
		target.blockQuantumLink = source.quantumLink().orNull();

		target.blockSpatialPylon = source.spatialPylon().orNull();
		target.blockSpatialIOPort = source.spatialIOPort().orNull();

		target.blockController = source.controller().orNull();
		target.blockDrive = source.drive().orNull();
		target.blockChest = source.chest().orNull();
		target.blockInterface = source.iface().orNull();
		target.blockCellWorkbench = source.cellWorkbench().orNull();
		target.blockIOPort = source.iOPort().orNull();
		target.blockCondenser = source.condenser().orNull();
		target.blockEnergyAcceptor = source.energyAcceptor().orNull();
		target.blockVibrationChamber = source.vibrationChamber().orNull();

		target.blockEnergyCell = source.energyCell().orNull();
		target.blockEnergyCellDense = source.energyCellDense().orNull();
		target.blockEnergyCellCreative = source.energyCellCreative().orNull();

		target.blockSecurity = source.security().orNull();
		target.blockPaint = source.paint().orNull();
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
		target.itemCellCreative = source.cellCreative().orNull();
		target.itemViewCell = source.viewCell().orNull();
		target.itemEncodedPattern = source.encodedPattern().orNull();

		target.itemCell1k = source.cell1k().orNull();
		target.itemCell4k = source.cell4k().orNull();
		target.itemCell16k = source.cell16k().orNull();
		target.itemCell64k = source.cell64k().orNull();

		target.itemSpatialCell2 = source.spatialCell2().orNull();
		target.itemSpatialCell16 = source.spatialCell16().orNull();
		target.itemSpatialCell128 = source.spatialCell128().orNull();

		target.itemCertusQuartzKnife = source.certusQuartzKnife().orNull();
		target.itemCertusQuartzWrench = source.certusQuartzWrench().orNull();
		target.itemCertusQuartzAxe = source.certusQuartzAxe().orNull();
		target.itemCertusQuartzHoe = source.certusQuartzHoe().orNull();
		target.itemCertusQuartzPick = source.certusQuartzPick().orNull();
		target.itemCertusQuartzShovel = source.certusQuartzShovel().orNull();
		target.itemCertusQuartzSword = source.certusQuartzSword().orNull();

		target.itemNetherQuartzKnife = source.netherQuartzKnife().orNull();
		target.itemNetherQuartzWrench = source.netherQuartzWrench().orNull();
		target.itemNetherQuartzAxe = source.netherQuartzAxe().orNull();
		target.itemNetherQuartzHoe = source.netherQuartzHoe().orNull();
		target.itemNetherQuartzPick = source.netherQuartzPick().orNull();
		target.itemNetherQuartzShovel = source.netherQuartzShovel().orNull();
		target.itemNetherQuartzSword = source.netherQuartzSword().orNull();

		target.itemMassCannon = source.massCannon().orNull();
		target.itemMemoryCard = source.memoryCard().orNull();
		target.itemChargedStaff = source.chargedStaff().orNull();
		target.itemEntropyManipulator = source.entropyManipulator().orNull();
		target.itemColorApplicator = source.colorApplicator().orNull();

		target.itemWirelessTerminal = source.wirelessTerminal().orNull();
		target.itemNetworkTool = source.networkTool().orNull();
		target.itemPortableCell = source.portableCell().orNull();
		target.itemBiometricCard = source.biometricCard().orNull();

		target.itemFacade = source.facade().orNull();
		target.itemCrystalSeed = source.crystalSeed().orNull();

		target.itemPaintBall = source.coloredPaintBall().orNull();
		target.itemLumenPaintBall = source.coloredLumenPaintBall().orNull();
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

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.MJ5 ) )
		{
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIPowerEmitter", "buildcraft.api.power.IPowerEmitter" );
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIPowerReceptor", "buildcraft.api.power.IPowerReceptor" );
		}

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.MJ6 ) )
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIBatteryProvider", "buildcraft.api.mj.IBatteryProvider" );

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

		for ( AEItemDefinition definition : api.definitions().materials().matterBall().asSet() )
		{
			registries.matterCannon().registerAmmo( definition.stack( 1 ), 32.0 );
		}

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
		for ( AEItemDefinition definition : blocks.multiPart().asSet() )
		{
			( (BlockCableBus) definition.block() ).setupTile();
		}

		// Interface
		for ( AEItemDefinition definition : parts.iface().asSet() )
		{
			Upgrades.CRAFTING.registerItem( definition, 1 );
		}

		for ( AEItemDefinition definition : blocks.iface().asSet() )
		{
			Upgrades.CRAFTING.registerItem( definition, 1 );
		}

		// IO Port!
		for ( AEItemDefinition definition : blocks.iOPort().asSet() )
		{
			Upgrades.SPEED.registerItem( definition, 3 );
			Upgrades.REDSTONE.registerItem( definition, 1 );
		}

		// Level Emitter!
		for ( AEItemDefinition definition : parts.levelEmitter().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.CRAFTING.registerItem( definition, 1 );
		}

		// Import Bus
		for ( AEItemDefinition definition : parts.importBus().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.REDSTONE.registerItem( definition, 1 );
			Upgrades.CAPACITY.registerItem( definition, 2 );
			Upgrades.SPEED.registerItem( definition, 4 );
		}

		// Export Bus
		for ( AEItemDefinition definition : parts.exportBus().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.REDSTONE.registerItem( definition, 1 );
			Upgrades.CAPACITY.registerItem( definition, 2 );
			Upgrades.SPEED.registerItem( definition, 4 );
			Upgrades.CRAFTING.registerItem( definition, 1 );
		}

		// Storage Cells
		for ( AEItemDefinition definition : items.cell1k().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.INVERTER.registerItem( definition, 1 );
		}

		for ( AEItemDefinition definition : items.cell4k().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.INVERTER.registerItem( definition, 1 );
		}

		for ( AEItemDefinition definition : items.cell16k().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.INVERTER.registerItem( definition, 1 );
		}

		for ( AEItemDefinition definition : items.cell64k().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.INVERTER.registerItem( definition, 1 );
		}

		for ( AEItemDefinition definition : items.portableCell().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.INVERTER.registerItem( definition, 1 );
		}

		for ( AEItemDefinition definition : items.viewCell().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.INVERTER.registerItem( definition, 1 );
		}

		// Storage Bus
		for ( AEItemDefinition definition : parts.storageBus().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.INVERTER.registerItem( definition, 1 );
			Upgrades.CAPACITY.registerItem( definition, 5 );
		}

		// Formation Plane
		for ( AEItemDefinition definition : parts.formationPlane().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.INVERTER.registerItem( definition, 1 );
			Upgrades.CAPACITY.registerItem( definition, 5 );
		}


		// Matter Cannon
		for ( AEItemDefinition definition : items.massCannon().asSet() )
		{
			Upgrades.FUZZY.registerItem( definition, 1 );
			Upgrades.INVERTER.registerItem( definition, 1 );
			Upgrades.SPEED.registerItem( definition, 4 );
		}

		// Molecular Assembler
		for ( AEItemDefinition definition : blocks.molecularAssembler().asSet() )
		{
			Upgrades.SPEED.registerItem( definition, 5 );
		}

		// Inscriber
		for ( AEItemDefinition definition : blocks.inscriber().asSet() )
		{
			Upgrades.SPEED.registerItem( definition, 3 );
		}

		for ( AEItemDefinition definition : items.wirelessTerminal().asSet() )
		{
			registries.wireless().registerWirelessHandler( (IWirelessTermHandler) definition.item() );
		}

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.ChestLoot ) )
		{
			ChestGenHooks d = ChestGenHooks.getInfo( ChestGenHooks.MINESHAFT_CORRIDOR );

			final IMaterials materials = definitions.materials();

			for ( AEItemDefinition definition : materials.certusQuartzCrystal().asSet() )
			{
				d.addItem( new WeightedRandomChestContent( definition.stack( 1 ), 1, 4, 2 ) );
			}
			for ( AEItemDefinition definition : materials.certusQuartzDust().asSet() )
			{
				d.addItem( new WeightedRandomChestContent( definition.stack( 1 ), 1, 4, 2 ) );
			}
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
