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


import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import appeng.core.features.DefinitionConverter;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.features.registries.entries.BasicCellHandler;
import appeng.core.features.registries.entries.CreativeCellHandler;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.stats.PlayerStatsRegistration;
import appeng.hooks.AETrading;
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
import appeng.worldgen.MeteoriteWorldGen;
import appeng.worldgen.QuartzWorldGen;


public final class Registration
{
	public static final Registration INSTANCE = new Registration();

	private final RecipeHandler recipeHandler;
	private final DefinitionConverter converter;
	public BiomeGenBase storageBiome;

	private Registration()
	{
		this.converter = new DefinitionConverter();
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
		for( IFeatureHandler handler : definitions.getFeatureHandlerRegistry().getRegisteredFeatureHandlers() )
		{
			handler.register();
		}

		for( IAEFeature feature : definitions.getFeatureRegistry().getRegisteredFeatures() )
		{
			feature.postInit();
		}
	}

	private void registerSpatial( boolean force )
	{
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.SpatialIO ) )
		{
			return;
		}

		AEConfig config = AEConfig.instance;

		if( this.storageBiome == null )
		{
			if( force && config.storageBiomeID == -1 )
			{
				config.storageBiomeID = Platform.findEmpty( BiomeGenBase.getBiomeGenArray() );
				if( config.storageBiomeID == -1 )
				{
					throw new IllegalStateException( "Biome Array is full, please free up some Biome ID's or disable spatial." );
				}

				this.storageBiome = new BiomeGenStorage( config.storageBiomeID );
				config.save();
			}

			if( !force && config.storageBiomeID != -1 )
			{
				this.storageBiome = new BiomeGenStorage( config.storageBiomeID );
			}
		}

		if( config.storageProviderID != -1 )
		{
			DimensionManager.registerProviderType( config.storageProviderID, StorageWorldProvider.class, false );
		}

		if( config.storageProviderID == -1 && force )
		{
			config.storageProviderID = -11;

			while( !DimensionManager.registerProviderType( config.storageProviderID, StorageWorldProvider.class, false ) )
			{
				config.storageProviderID--;
			}

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
	 * Uses direct cast, since its only a temporary solution anyways
	 *
	 * @param target old API
	 * @param source new API
	 *
	 * @deprecated to be removed when the public definition API is removed
	 */
	@Deprecated
	private void assignMaterials( Materials target, IMaterials source )
	{
		target.materialCell2SpatialPart = this.converter.of( source.cell2SpatialPart() );
		target.materialCell16SpatialPart = this.converter.of( source.cell16SpatialPart() );
		target.materialCell128SpatialPart = this.converter.of( source.cell128SpatialPart() );

		target.materialSilicon = this.converter.of( source.silicon() );
		target.materialSkyDust = this.converter.of( source.skyDust() );

		target.materialCalcProcessorPress = this.converter.of( source.calcProcessorPress() );
		target.materialEngProcessorPress = this.converter.of( source.engProcessorPress() );
		target.materialLogicProcessorPress = this.converter.of( source.logicProcessorPress() );

		target.materialCalcProcessorPrint = this.converter.of( source.calcProcessorPrint() );
		target.materialEngProcessorPrint = this.converter.of( source.engProcessorPrint() );
		target.materialLogicProcessorPrint = this.converter.of( source.logicProcessorPrint() );

		target.materialSiliconPress = this.converter.of( source.siliconPress() );
		target.materialSiliconPrint = this.converter.of( source.siliconPrint() );

		target.materialNamePress = this.converter.of( source.namePress() );

		target.materialLogicProcessor = this.converter.of( source.logicProcessor() );
		target.materialCalcProcessor = this.converter.of( source.calcProcessor() );
		target.materialEngProcessor = this.converter.of( source.engProcessor() );

		target.materialBasicCard = this.converter.of( source.basicCard() );
		target.materialAdvCard = this.converter.of( source.advCard() );

		target.materialPurifiedCertusQuartzCrystal = this.converter.of( source.purifiedCertusQuartzCrystal() );
		target.materialPurifiedNetherQuartzCrystal = this.converter.of( source.purifiedNetherQuartzCrystal() );
		target.materialPurifiedFluixCrystal = this.converter.of( source.purifiedFluixCrystal() );

		target.materialCell1kPart = this.converter.of( source.cell1kPart() );
		target.materialCell4kPart = this.converter.of( source.cell4kPart() );
		target.materialCell16kPart = this.converter.of( source.cell16kPart() );
		target.materialCell64kPart = this.converter.of( source.cell64kPart() );
		target.materialEmptyStorageCell = this.converter.of( source.emptyStorageCell() );

		target.materialCardRedstone = this.converter.of( source.cardRedstone() );
		target.materialCardSpeed = this.converter.of( source.cardSpeed() );
		target.materialCardCapacity = this.converter.of( source.cardCapacity() );
		target.materialCardFuzzy = this.converter.of( source.cardFuzzy() );
		target.materialCardInverter = this.converter.of( source.cardInverter() );
		target.materialCardCrafting = this.converter.of( source.cardCrafting() );

		target.materialEnderDust = this.converter.of( source.enderDust() );
		target.materialFlour = this.converter.of( source.flour() );
		target.materialGoldDust = this.converter.of( source.goldDust() );
		target.materialIronDust = this.converter.of( source.ironDust() );
		target.materialFluixDust = this.converter.of( source.fluixDust() );
		target.materialCertusQuartzDust = this.converter.of( source.certusQuartzDust() );
		target.materialNetherQuartzDust = this.converter.of( source.netherQuartzDust() );

		target.materialMatterBall = this.converter.of( source.matterBall() );
		target.materialIronNugget = this.converter.of( source.ironNugget() );

		target.materialCertusQuartzCrystal = this.converter.of( source.certusQuartzCrystal() );
		target.materialCertusQuartzCrystalCharged = this.converter.of( source.certusQuartzCrystalCharged() );
		target.materialFluixCrystal = this.converter.of( source.fluixCrystal() );
		target.materialFluixPearl = this.converter.of( source.fluixPearl() );

		target.materialWoodenGear = this.converter.of( source.woodenGear() );

		target.materialWireless = this.converter.of( source.wireless() );
		target.materialWirelessBooster = this.converter.of( source.wirelessBooster() );

		target.materialAnnihilationCore = this.converter.of( source.annihilationCore() );
		target.materialFormationCore = this.converter.of( source.formationCore() );

		target.materialSingularity = this.converter.of( source.singularity() );
		target.materialQESingularity = this.converter.of( source.qESingularity() );
		target.materialBlankPattern = this.converter.of( source.blankPattern() );
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
		target.partQuartzFiber = this.converter.of( source.quartzFiber() );
		target.partToggleBus = this.converter.of( source.toggleBus() );
		target.partInvertedToggleBus = this.converter.of( source.invertedToggleBus() );
		target.partStorageBus = this.converter.of( source.storageBus() );
		target.partImportBus = this.converter.of( source.importBus() );
		target.partExportBus = this.converter.of( source.exportBus() );
		target.partInterface = this.converter.of( source.iface() );
		target.partLevelEmitter = this.converter.of( source.levelEmitter() );
		target.partAnnihilationPlane = this.converter.of( source.annihilationPlane() );
		target.partFormationPlane = this.converter.of( source.formationPlane() );

		target.partCableAnchor = this.converter.of( source.cableAnchor() );
		target.partP2PTunnelLight = target.partCableAnchor;
		target.partP2PTunnelRF = target.partP2PTunnelLight;
		target.partP2PTunnelEU = target.partP2PTunnelRF;
		target.partP2PTunnelLiquids = target.partP2PTunnelEU;
		target.partP2PTunnelItems = target.partP2PTunnelLiquids;
		target.partP2PTunnelRedstone = target.partP2PTunnelItems;
		target.partP2PTunnelME = target.partP2PTunnelRedstone;
		target.partMonitor = this.converter.of( source.monitor() );
		target.partSemiDarkMonitor = this.converter.of( source.semiDarkMonitor() );
		target.partDarkMonitor = this.converter.of( source.darkMonitor() );
		target.partInterfaceTerminal = this.converter.of( source.interfaceTerminal() );
		target.partPatternTerminal = this.converter.of( source.patternTerminal() );
		target.partCraftingTerminal = this.converter.of( source.craftingTerminal() );
		target.partTerminal = this.converter.of( source.terminal() );
		target.partStorageMonitor = this.converter.of( source.storageMonitor() );
		target.partConversionMonitor = this.converter.of( source.conversionMonitor() );
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
		target.blockMultiPart = this.converter.of( source.multiPart() );

		target.blockCraftingUnit = this.converter.of( source.craftingUnit() );
		target.blockCraftingAccelerator = this.converter.of( source.craftingAccelerator() );
		target.blockCraftingMonitor = this.converter.of( source.craftingMonitor() );
		target.blockCraftingStorage1k = this.converter.of( source.craftingStorage1k() );
		target.blockCraftingStorage4k = this.converter.of( source.craftingStorage4k() );
		target.blockCraftingStorage16k = this.converter.of( source.craftingStorage16k() );
		target.blockCraftingStorage64k = this.converter.of( source.craftingStorage64k() );
		target.blockMolecularAssembler = this.converter.of( source.molecularAssembler() );

		target.blockQuartzOre = this.converter.of( source.quartzOre() );
		target.blockQuartzOreCharged = this.converter.of( source.quartzOreCharged() );
		target.blockMatrixFrame = this.converter.of( source.matrixFrame() );
		target.blockQuartz = this.converter.of( source.quartz() );
		target.blockFluix = this.converter.of( source.fluix() );
		target.blockSkyStone = this.converter.of( source.skyStone() );
		target.blockSkyChest = this.converter.of( source.skyChest() );
		target.blockSkyCompass = this.converter.of( source.skyCompass() );

		target.blockQuartzGlass = this.converter.of( source.quartzGlass() );
		target.blockQuartzVibrantGlass = this.converter.of( source.quartzVibrantGlass() );
		target.blockQuartzPillar = this.converter.of( source.quartzPillar() );
		target.blockQuartzChiseled = this.converter.of( source.quartzChiseled() );
		target.blockQuartzTorch = this.converter.of( source.quartzTorch() );
		target.blockLightDetector = this.converter.of( source.lightDetector() );
		target.blockCharger = this.converter.of( source.charger() );
		target.blockQuartzGrowthAccelerator = this.converter.of( source.quartzGrowthAccelerator() );

		target.blockGrindStone = this.converter.of( source.grindStone() );
		target.blockCrankHandle = this.converter.of( source.crankHandle() );
		target.blockInscriber = this.converter.of( source.inscriber() );
		target.blockWireless = this.converter.of( source.wireless() );
		target.blockTinyTNT = this.converter.of( source.tinyTNT() );

		target.blockQuantumRing = this.converter.of( source.quantumRing() );
		target.blockQuantumLink = this.converter.of( source.quantumLink() );

		target.blockSpatialPylon = this.converter.of( source.spatialPylon() );
		target.blockSpatialIOPort = this.converter.of( source.spatialIOPort() );

		target.blockController = this.converter.of( source.controller() );
		target.blockDrive = this.converter.of( source.drive() );
		target.blockChest = this.converter.of( source.chest() );
		target.blockInterface = this.converter.of( source.iface() );
		target.blockCellWorkbench = this.converter.of( source.cellWorkbench() );
		target.blockIOPort = this.converter.of( source.iOPort() );
		target.blockCondenser = this.converter.of( source.condenser() );
		target.blockEnergyAcceptor = this.converter.of( source.energyAcceptor() );
		target.blockVibrationChamber = this.converter.of( source.vibrationChamber() );

		target.blockEnergyCell = this.converter.of( source.energyCell() );
		target.blockEnergyCellDense = this.converter.of( source.energyCellDense() );
		target.blockEnergyCellCreative = this.converter.of( source.energyCellCreative() );

		target.blockSecurity = this.converter.of( source.security() );
		target.blockPaint = this.converter.of( source.paint() );
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
		target.itemCellCreative = this.converter.of( source.cellCreative() );
		target.itemViewCell = this.converter.of( source.viewCell() );
		target.itemEncodedPattern = this.converter.of( source.encodedPattern() );

		target.itemCell1k = this.converter.of( source.cell1k() );
		target.itemCell4k = this.converter.of( source.cell4k() );
		target.itemCell16k = this.converter.of( source.cell16k() );
		target.itemCell64k = this.converter.of( source.cell64k() );

		target.itemSpatialCell2 = this.converter.of( source.spatialCell2() );
		target.itemSpatialCell16 = this.converter.of( source.spatialCell16() );
		target.itemSpatialCell128 = this.converter.of( source.spatialCell128() );

		target.itemCertusQuartzKnife = this.converter.of( source.certusQuartzKnife() );
		target.itemCertusQuartzWrench = this.converter.of( source.certusQuartzWrench() );
		target.itemCertusQuartzAxe = this.converter.of( source.certusQuartzAxe() );
		target.itemCertusQuartzHoe = this.converter.of( source.certusQuartzHoe() );
		target.itemCertusQuartzPick = this.converter.of( source.certusQuartzPick() );
		target.itemCertusQuartzShovel = this.converter.of( source.certusQuartzShovel() );
		target.itemCertusQuartzSword = this.converter.of( source.certusQuartzSword() );

		target.itemNetherQuartzKnife = this.converter.of( source.netherQuartzKnife() );
		target.itemNetherQuartzWrench = this.converter.of( source.netherQuartzWrench() );
		target.itemNetherQuartzAxe = this.converter.of( source.netherQuartzAxe() );
		target.itemNetherQuartzHoe = this.converter.of( source.netherQuartzHoe() );
		target.itemNetherQuartzPick = this.converter.of( source.netherQuartzPick() );
		target.itemNetherQuartzShovel = this.converter.of( source.netherQuartzShovel() );
		target.itemNetherQuartzSword = this.converter.of( source.netherQuartzSword() );

		target.itemMassCannon = this.converter.of( source.massCannon() );
		target.itemMemoryCard = this.converter.of( source.memoryCard() );
		target.itemChargedStaff = this.converter.of( source.chargedStaff() );
		target.itemEntropyManipulator = this.converter.of( source.entropyManipulator() );
		target.itemColorApplicator = this.converter.of( source.colorApplicator() );

		target.itemWirelessTerminal = this.converter.of( source.wirelessTerminal() );
		target.itemNetworkTool = this.converter.of( source.networkTool() );
		target.itemPortableCell = this.converter.of( source.portableCell() );
		target.itemBiometricCard = this.converter.of( source.biometricCard() );

		target.itemFacade = this.converter.of( source.facade() );
		target.itemCrystalSeed = this.converter.of( source.crystalSeed() );

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

		if( AEConfig.instance.isFeatureEnabled( AEFeature.CustomRecipes ) )
		{
			this.recipeHandler.parseRecipes( new ConfigLoader( AppEng.instance.getConfigDirectory() ), "index.recipe" );
		}
		else
		{
			this.recipeHandler.parseRecipes( new JarLoader( "/assets/appliedenergistics2/recipes/" ), "index.recipe" );
		}

		partHelper.registerNewLayer( "appeng.parts.layers.LayerISidedInventory", "net.minecraft.inventory.ISidedInventory" );
		partHelper.registerNewLayer( "appeng.parts.layers.LayerIFluidHandler", "net.minecraftforge.fluids.IFluidHandler" );
		partHelper.registerNewLayer( "appeng.parts.layers.LayerITileStorageMonitorable", "appeng.api.implementations.tiles.ITileStorageMonitorable" );

		if( AppEng.instance.isIntegrationEnabled( IntegrationType.IC2 ) )
		{
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIEnergySink", "ic2.api.energy.tile.IEnergySink" );
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIEnergySource", "ic2.api.energy.tile.IEnergySource" );
		}

		if( AppEng.instance.isIntegrationEnabled( IntegrationType.RF ) )
		{
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIEnergyHandler", "cofh.api.energy.IEnergyReceiver" );
		}

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.OpenComputers ) )
		{
			partHelper.registerNewLayer( "appeng.parts.layers.LayerSidedEnvironment", "li.cil.oc.api.network.SidedEnvironment" );
		}

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

		for( ItemStack ammoStack : api.definitions().materials().matterBall().maybeStack( 1 ).asSet() )
		{
			final double weight = 32;

			registries.matterCannon().registerAmmo( ammoStack, weight );
		}

		this.recipeHandler.injectRecipes();

		final PlayerStatsRegistration registration = new PlayerStatsRegistration( FMLCommonHandler.instance().bus(), AEConfig.instance );
		registration.registerAchievementHandlers();
		registration.registerAchievements();

		if( AEConfig.instance.isFeatureEnabled( AEFeature.enableDisassemblyCrafting ) )
		{
			GameRegistry.addRecipe( new DisassembleRecipe() );
			RecipeSorter.register( "appliedenergistics2:disassemble", DisassembleRecipe.class, Category.SHAPELESS, "after:minecraft:shapeless" );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.enableFacadeCrafting ) )
		{
			GameRegistry.addRecipe( new FacadeRecipe() );
			RecipeSorter.register( "appliedenergistics2:facade", FacadeRecipe.class, Category.SHAPED, "after:minecraft:shaped" );
		}
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

		Api.INSTANCE.partHelper().initFMPSupport();
		for( Block block : blocks.multiPart().maybeBlock().asSet() )
		{
			( (BlockCableBus) block ).setupTile();
		}

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

		for( Item wirelessTerminalItem : items.wirelessTerminal().maybeItem().asSet() )
		{
			registries.wireless().registerWirelessHandler( (IWirelessTermHandler) wirelessTerminalItem );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.ChestLoot ) )
		{
			ChestGenHooks d = ChestGenHooks.getInfo( ChestGenHooks.MINESHAFT_CORRIDOR );

			final IMaterials materials = definitions.materials();

			for( ItemStack crystal : materials.certusQuartzCrystal().maybeStack( 1 ).asSet() )
			{
				d.addItem( new WeightedRandomChestContent( crystal, 1, 4, 2 ) );
			}
			for( ItemStack dust : materials.certusQuartzDust().maybeStack( 1 ).asSet() )
			{
				d.addItem( new WeightedRandomChestContent( dust, 1, 4, 2 ) );
			}
		}

		// add villager trading to black smiths for a few basic materials
		if( AEConfig.instance.isFeatureEnabled( AEFeature.VillagerTrading ) )
		{
			VillagerRegistry.instance().registerVillageTradeHandler( 3, new AETrading() );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.CertusQuartzWorldGen ) )
		{
			GameRegistry.registerWorldGenerator( new QuartzWorldGen(), 0 );
		}

		if( AEConfig.instance.isFeatureEnabled( AEFeature.MeteoriteWorldGen ) )
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
		for( WorldGenType type : WorldGenType.values() )
		{
			registries.worldgen().disableWorldGenForProviderID( type, StorageWorldProvider.class );

			// nether
			registries.worldgen().disableWorldGenForDimension( type, -1 );

			// end
			registries.worldgen().disableWorldGenForDimension( type, 1 );
		}

		// whitelist from config
		for( int dimension : AEConfig.instance.meteoriteDimensionWhitelist )
		{
			registries.worldgen().enableWorldGenForDimension( WorldGenType.Meteorites, dimension );
		}

		/**
		 * initial recipe bake, if ore dictionary changes after this it re-bakes.
		 */
		OreDictionaryHandler.INSTANCE.bakeRecipes();
	}
}
