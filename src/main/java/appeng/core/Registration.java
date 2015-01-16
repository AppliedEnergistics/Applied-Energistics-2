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


import java.lang.reflect.Field;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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
import appeng.api.util.AEColor;
import appeng.api.util.AEItemDefinition;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.block.crafting.BlockCraftingStorage;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.block.crafting.BlockMolecularAssembler;
import appeng.block.grindstone.BlockCrank;
import appeng.block.grindstone.BlockGrinder;
import appeng.block.misc.BlockCellWorkbench;
import appeng.block.misc.BlockCharger;
import appeng.block.misc.BlockCondenser;
import appeng.block.misc.BlockInscriber;
import appeng.block.misc.BlockInterface;
import appeng.block.misc.BlockLightDetector;
import appeng.block.misc.BlockPaint;
import appeng.block.misc.BlockQuartzGrowthAccelerator;
import appeng.block.misc.BlockQuartzTorch;
import appeng.block.misc.BlockSecurity;
import appeng.block.misc.BlockSkyCompass;
import appeng.block.misc.BlockTinyTNT;
import appeng.block.misc.BlockVibrationChamber;
import appeng.block.networking.BlockCableBus;
import appeng.block.networking.BlockController;
import appeng.block.networking.BlockCreativeEnergyCell;
import appeng.block.networking.BlockDenseEnergyCell;
import appeng.block.networking.BlockEnergyAcceptor;
import appeng.block.networking.BlockEnergyCell;
import appeng.block.networking.BlockWireless;
import appeng.block.qnb.BlockQuantumLinkChamber;
import appeng.block.qnb.BlockQuantumRing;
import appeng.block.solids.BlockFluix;
import appeng.block.solids.BlockQuartz;
import appeng.block.solids.BlockQuartzChiseled;
import appeng.block.solids.BlockQuartzGlass;
import appeng.block.solids.BlockQuartzLamp;
import appeng.block.solids.BlockQuartzPillar;
import appeng.block.solids.BlockSkyStone;
import appeng.block.solids.OreQuartz;
import appeng.block.solids.OreQuartzCharged;
import appeng.block.spatial.BlockMatrixFrame;
import appeng.block.spatial.BlockSpatialIOPort;
import appeng.block.spatial.BlockSpatialPylon;
import appeng.block.stair.ChiseledQuartzStairBlock;
import appeng.block.stair.FluixStairBlock;
import appeng.block.stair.QuartzPillarStairBlock;
import appeng.block.stair.QuartzStairBlock;
import appeng.block.stair.SkyStoneBlockStairBlock;
import appeng.block.stair.SkyStoneBrickStairBlock;
import appeng.block.stair.SkyStoneSmallBrickStairBlock;
import appeng.block.stair.SkyStoneStairBlock;
import appeng.block.storage.BlockChest;
import appeng.block.storage.BlockDrive;
import appeng.block.storage.BlockIOPort;
import appeng.block.storage.BlockSkyChest;
import appeng.core.features.AEFeature;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.DamagedItemDefinition;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.IStackSrc;
import appeng.core.features.ItemStackSrc;
import appeng.core.features.NullItemDefinition;
import appeng.core.features.WrappedDamageItemDefinition;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.features.registries.entries.BasicCellHandler;
import appeng.core.features.registries.entries.CreativeCellHandler;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.stats.PlayerStatsRegistration;
import appeng.debug.BlockChunkloader;
import appeng.debug.BlockCubeGenerator;
import appeng.debug.BlockItemGen;
import appeng.debug.BlockPhantomNode;
import appeng.debug.ToolDebugCard;
import appeng.debug.ToolEraser;
import appeng.debug.ToolMeteoritePlacer;
import appeng.debug.ToolReplicatorCard;
import appeng.hooks.AETrading;
import appeng.hooks.MeteoriteWorldGen;
import appeng.hooks.QuartzWorldGen;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationType;
import appeng.items.materials.ItemMultiMaterial;
import appeng.items.materials.MaterialType;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.misc.ItemPaintBall;
import appeng.items.parts.ItemFacade;
import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.items.storage.ItemCreativeStorageCell;
import appeng.items.storage.ItemSpatialStorageCell;
import appeng.items.storage.ItemViewCell;
import appeng.items.tools.ToolBiometricCard;
import appeng.items.tools.ToolMemoryCard;
import appeng.items.tools.ToolNetworkTool;
import appeng.items.tools.powered.ToolChargedStaff;
import appeng.items.tools.powered.ToolColorApplicator;
import appeng.items.tools.powered.ToolEntropyManipulator;
import appeng.items.tools.powered.ToolMassCannon;
import appeng.items.tools.powered.ToolPortableCell;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.items.tools.quartz.ToolQuartzAxe;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.items.tools.quartz.ToolQuartzHoe;
import appeng.items.tools.quartz.ToolQuartzPickaxe;
import appeng.items.tools.quartz.ToolQuartzSpade;
import appeng.items.tools.quartz.ToolQuartzSword;
import appeng.items.tools.quartz.ToolQuartzWrench;
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
import appeng.util.ClassInstantiation;
import appeng.util.Platform;


public final class Registration
{
	final public static Registration INSTANCE = new Registration();

	private final RecipeHandler recipeHandler;
	private final Multimap<AEFeature, Class<? extends IAEFeature>> featuresToEntities;
	public BiomeGenBase storageBiome;

	private Registration()
	{
		this.recipeHandler = new RecipeHandler();
		this.featuresToEntities = ArrayListMultimap.create();
	}

	public void preInitialize( FMLPreInitializationEvent event )
	{
		this.registerSpatial( false );

		IRecipeHandlerRegistry recipeRegistry = AEApi.instance().registries().recipes();
		recipeRegistry.addNewSubItemResolver( new AEItemResolver() );

		recipeRegistry.addNewCraftHandler( "hccrusher", HCCrusher.class );
		recipeRegistry.addNewCraftHandler( "mekcrusher", MekCrusher.class );
		recipeRegistry.addNewCraftHandler( "mekechamber", MekEnrichment.class );
		recipeRegistry.addNewCraftHandler( "grind", Grind.class );
		recipeRegistry.addNewCraftHandler( "crusher", Crusher.class );
		recipeRegistry.addNewCraftHandler( "grindfz", GrindFZ.class );
		recipeRegistry.addNewCraftHandler( "pulverizer", Pulverizer.class );
		recipeRegistry.addNewCraftHandler( "macerator", Macerator.class );

		recipeRegistry.addNewCraftHandler( "smelt", Smelt.class );
		recipeRegistry.addNewCraftHandler( "inscribe", Inscribe.class );
		recipeRegistry.addNewCraftHandler( "press", Press.class );

		recipeRegistry.addNewCraftHandler( "shaped", Shaped.class );
		recipeRegistry.addNewCraftHandler( "shapeless", Shapeless.class );

		RecipeSorter.register( "AE2-Facade", FacadeRecipe.class, Category.SHAPED, "" );
		RecipeSorter.register( "AE2-Shaped", ShapedRecipe.class, Category.SHAPED, "" );
		RecipeSorter.register( "AE2-Shapeless", ShapelessRecipe.class, Category.SHAPELESS, "" );

		MinecraftForge.EVENT_BUS.register( OreDictionaryHandler.INSTANCE );

		Items items = Api.INSTANCE.items();
		Materials materials = Api.INSTANCE.materials();
		Parts parts = Api.INSTANCE.parts();
		Blocks blocks = Api.INSTANCE.blocks();

		AEItemDefinition materialItem = this.addFeature( ItemMultiMaterial.class );

		Class<?> materialClass = materials.getClass();
		for ( MaterialType mat : MaterialType.values() )
		{
			try
			{
				if ( mat == MaterialType.InvalidType )
					( ( ItemMultiMaterial ) materialItem.item() ).createMaterial( mat );
				else
				{
					Field f = materialClass.getField( "material" + mat.name() );
					IStackSrc is = ( ( ItemMultiMaterial ) materialItem.item() ).createMaterial( mat );
					if ( is != null )
						f.set( materials, new DamagedItemDefinition( is ) );
					else
						f.set( materials, new NullItemDefinition() );
				}
			}
			catch ( Throwable err )
			{
				AELog.severe( "Error creating material: " + mat.name() );
				throw new RuntimeException( err );
			}
		}

		AEItemDefinition partItem = this.addFeature( ItemMultiPart.class );

		Class<?> partClass = parts.getClass();
		for ( PartType type : PartType.values() )
		{
			try
			{
				if ( type == PartType.InvalidType )
					( ( ItemMultiPart ) partItem.item() ).createPart( type, null );
				else
				{
					Field f = partClass.getField( "part" + type.name() );
					Enum<AEColor>[] variants = type.getVariants();
					if ( variants == null )
					{
						ItemStackSrc is = ( ( ItemMultiPart ) partItem.item() ).createPart( type, null );
						if ( is != null )
							f.set( parts, new DamagedItemDefinition( is ) );
						else
							f.set( parts, new NullItemDefinition() );
					}
					else
					{
						if ( variants[0] instanceof AEColor )
						{
							ColoredItemDefinition def = new ColoredItemDefinition();

							for ( Enum<AEColor> v : variants )
							{
								ItemStackSrc is = ( ( ItemMultiPart ) partItem.item() ).createPart( type, v );
								if ( is != null )
									def.add( ( AEColor ) v, is );
							}

							f.set( parts, def );
						}
					}
				}
			}
			catch ( Throwable err )
			{
				AELog.severe( "Error creating part: " + type.name() );
				throw new RuntimeException( err );
			}
		}

		// very important block!
		blocks.blockMultiPart = this.addFeature( BlockCableBus.class );

		blocks.blockCraftingUnit = this.addFeature( BlockCraftingUnit.class );
		blocks.blockCraftingAccelerator = new WrappedDamageItemDefinition( blocks.blockCraftingUnit, 1 );
		blocks.blockCraftingMonitor = this.addFeature( BlockCraftingMonitor.class );
		blocks.blockCraftingStorage1k = this.addFeature( BlockCraftingStorage.class );
		blocks.blockCraftingStorage4k = new WrappedDamageItemDefinition( blocks.blockCraftingStorage1k, 1 );
		blocks.blockCraftingStorage16k = new WrappedDamageItemDefinition( blocks.blockCraftingStorage1k, 2 );
		blocks.blockCraftingStorage64k = new WrappedDamageItemDefinition( blocks.blockCraftingStorage1k, 3 );
		blocks.blockMolecularAssembler = this.addFeature( BlockMolecularAssembler.class );

		blocks.blockQuartzOre = this.addFeature( OreQuartz.class );
		blocks.blockQuartzOreCharged = this.addFeature( OreQuartzCharged.class );
		blocks.blockMatrixFrame = this.addFeature( BlockMatrixFrame.class );
		blocks.blockQuartz = this.addFeature( BlockQuartz.class );
		blocks.blockFluix = this.addFeature( BlockFluix.class );
		blocks.blockSkyStone = this.addFeature( BlockSkyStone.class );
		blocks.blockSkyChest = this.addFeature( BlockSkyChest.class );
		blocks.blockSkyCompass = this.addFeature( BlockSkyCompass.class );

		blocks.blockQuartzGlass = this.addFeature( BlockQuartzGlass.class );
		blocks.blockQuartzVibrantGlass = this.addFeature( BlockQuartzLamp.class );
		blocks.blockQuartzPillar = this.addFeature( BlockQuartzPillar.class );
		blocks.blockQuartzChiseled = this.addFeature( BlockQuartzChiseled.class );
		blocks.blockQuartzTorch = this.addFeature( BlockQuartzTorch.class );
		blocks.blockLightDetector = this.addFeature( BlockLightDetector.class );
		blocks.blockCharger = this.addFeature( BlockCharger.class );
		blocks.blockQuartzGrowthAccelerator = this.addFeature( BlockQuartzGrowthAccelerator.class );

		blocks.blockGrindStone = this.addFeature( BlockGrinder.class );
		blocks.blockCrankHandle = this.addFeature( BlockCrank.class );
		blocks.blockInscriber = this.addFeature( BlockInscriber.class );
		blocks.blockWireless = this.addFeature( BlockWireless.class );
		blocks.blockTinyTNT = this.addFeature( BlockTinyTNT.class );

		blocks.blockQuantumRing = this.addFeature( BlockQuantumRing.class );
		blocks.blockQuantumLink = this.addFeature( BlockQuantumLinkChamber.class );

		blocks.blockSpatialPylon = this.addFeature( BlockSpatialPylon.class );
		blocks.blockSpatialIOPort = this.addFeature( BlockSpatialIOPort.class );

		blocks.blockController = this.addFeature( BlockController.class );
		blocks.blockDrive = this.addFeature( BlockDrive.class );
		blocks.blockChest = this.addFeature( BlockChest.class );
		blocks.blockInterface = this.addFeature( BlockInterface.class );
		blocks.blockCellWorkbench = this.addFeature( BlockCellWorkbench.class );
		blocks.blockIOPort = this.addFeature( BlockIOPort.class );
		blocks.blockCondenser = this.addFeature( BlockCondenser.class );
		blocks.blockEnergyAcceptor = this.addFeature( BlockEnergyAcceptor.class );
		blocks.blockVibrationChamber = this.addFeature( BlockVibrationChamber.class );

		blocks.blockEnergyCell = this.addFeature( BlockEnergyCell.class );
		blocks.blockEnergyCellDense = this.addFeature( BlockDenseEnergyCell.class );
		blocks.blockEnergyCellCreative = this.addFeature( BlockCreativeEnergyCell.class );

		blocks.blockSecurity = this.addFeature( BlockSecurity.class );
		blocks.blockPaint = this.addFeature( BlockPaint.class );

		items.itemCellCreative = this.addFeature( ItemCreativeStorageCell.class );
		items.itemViewCell = this.addFeature( ItemViewCell.class );
		items.itemEncodedPattern = this.addFeature( ItemEncodedPattern.class );

		items.itemCell1k = this.addFeature( ItemBasicStorageCell.class, MaterialType.Cell1kPart, 1 );
		items.itemCell4k = this.addFeature( ItemBasicStorageCell.class, MaterialType.Cell4kPart, 4 );
		items.itemCell16k = this.addFeature( ItemBasicStorageCell.class, MaterialType.Cell16kPart, 16 );
		items.itemCell64k = this.addFeature( ItemBasicStorageCell.class, MaterialType.Cell64kPart, 64 );

		items.itemSpatialCell2 = this.addFeature( ItemSpatialStorageCell.class, MaterialType.Cell2SpatialPart, 2 );
		items.itemSpatialCell16 = this.addFeature( ItemSpatialStorageCell.class, MaterialType.Cell16SpatialPart, 16 );
		items.itemSpatialCell128 = this.addFeature( ItemSpatialStorageCell.class, MaterialType.Cell128SpatialPart, 128 );

		items.itemCertusQuartzKnife = this.addFeature( ToolQuartzCuttingKnife.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzWrench = this.addFeature( ToolQuartzWrench.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzAxe = this.addFeature( ToolQuartzAxe.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzHoe = this.addFeature( ToolQuartzHoe.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzPick = this.addFeature( ToolQuartzPickaxe.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzShovel = this.addFeature( ToolQuartzSpade.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzSword = this.addFeature( ToolQuartzSword.class, AEFeature.CertusQuartzTools );

		items.itemNetherQuartzKnife = this.addFeature( ToolQuartzCuttingKnife.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzWrench = this.addFeature( ToolQuartzWrench.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzAxe = this.addFeature( ToolQuartzAxe.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzHoe = this.addFeature( ToolQuartzHoe.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzPick = this.addFeature( ToolQuartzPickaxe.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzShovel = this.addFeature( ToolQuartzSpade.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzSword = this.addFeature( ToolQuartzSword.class, AEFeature.NetherQuartzTools );

		items.itemMassCannon = this.addFeature( ToolMassCannon.class );
		items.itemMemoryCard = this.addFeature( ToolMemoryCard.class );
		items.itemChargedStaff = this.addFeature( ToolChargedStaff.class );
		items.itemEntropyManipulator = this.addFeature( ToolEntropyManipulator.class );
		items.itemColorApplicator = this.addFeature( ToolColorApplicator.class );

		items.itemWirelessTerminal = this.addFeature( ToolWirelessTerminal.class );
		items.itemNetworkTool = this.addFeature( ToolNetworkTool.class );
		items.itemPortableCell = this.addFeature( ToolPortableCell.class );
		items.itemBiometricCard = this.addFeature( ToolBiometricCard.class );

		items.itemFacade = this.addFeature( ItemFacade.class );
		items.itemCrystalSeed = this.addFeature( ItemCrystalSeed.class );

		ColoredItemDefinition paintBall;
		ColoredItemDefinition lumenPaintBall;
		items.itemPaintBall = paintBall = new ColoredItemDefinition();
		items.itemLumenPaintBall = lumenPaintBall = new ColoredItemDefinition();
		AEItemDefinition pb = this.addFeature( ItemPaintBall.class );

		for ( AEColor c : AEColor.values() )
		{
			if ( c != AEColor.Transparent )
			{
				paintBall.add( c, new ItemStackSrc( pb.item(), c.ordinal() ) );
				lumenPaintBall.add( c, new ItemStackSrc( pb.item(), 20 + c.ordinal() ) );
			}
		}

		// stairs
		this.addFeature( SkyStoneStairBlock.class, blocks.blockSkyStone.block(), 0 );
		this.addFeature( SkyStoneBlockStairBlock.class, blocks.blockSkyStone.block(), 1 );
		this.addFeature( SkyStoneBrickStairBlock.class, blocks.blockSkyStone.block(), 2 );
		this.addFeature( SkyStoneSmallBrickStairBlock.class, blocks.blockSkyStone.block(), 3 );
		this.addFeature( FluixStairBlock.class, blocks.blockFluix.block() );
		this.addFeature( QuartzStairBlock.class, blocks.blockQuartz.block() );
		this.addFeature( ChiseledQuartzStairBlock.class, blocks.blockQuartzChiseled.block() );
		this.addFeature( QuartzPillarStairBlock.class, blocks.blockQuartzPillar.block() );

		// unsupported developer tools
		this.addFeature( ToolEraser.class );
		this.addFeature( ToolMeteoritePlacer.class );
		this.addFeature( ToolDebugCard.class );
		this.addFeature( ToolReplicatorCard.class );
		this.addFeature( BlockItemGen.class );
		this.addFeature( BlockChunkloader.class );
		this.addFeature( BlockPhantomNode.class );
		this.addFeature( BlockCubeGenerator.class );
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

	private AEItemDefinition addFeature( Class<? extends IAEFeature> featureClass, Object... args )
	{
		final ClassInstantiation<IAEFeature> instantiation = new ClassInstantiation<IAEFeature>( featureClass, args );
		final Optional<IAEFeature> instance = instantiation.get();

		if ( instance.isPresent() )
		{
			final IAEFeature feature = instance.get();
			final IFeatureHandler handler = feature.handler();
			if ( handler.isFeatureAvailable() )
			{
				for ( AEFeature f : handler.getFeatures() )
				{
					this.featuresToEntities.put( f, featureClass );
				}

				handler.register();
				feature.postInit();

				return handler.getDefinition();
			}
			else
			{
				return null;
			}
		}
		else
		{
			throw new RuntimeException( "Error upon Class Instantiation with Feature: " + featureClass.getName() );
		}
	}

	public void initialize( FMLInitializationEvent event )
	{
		// Perform ore camouflage!
		ItemMultiMaterial.instance.makeUnique();

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.CustomRecipes ) )
			this.recipeHandler.parseRecipes( new ConfigLoader( AppEng.instance.getConfigPath() ), "index.recipe" );
		else
			this.recipeHandler.parseRecipes( new JarLoader( "/assets/appliedenergistics2/recipes/" ), "index.recipe" );

		IPartHelper ph = AEApi.instance().partHelper();
		ph.registerNewLayer( "appeng.parts.layers.LayerISidedInventory", "net.minecraft.inventory.ISidedInventory" );
		ph.registerNewLayer( "appeng.parts.layers.LayerIFluidHandler", "net.minecraftforge.fluids.IFluidHandler" );
		ph.registerNewLayer( "appeng.parts.layers.LayerITileStorageMonitorable", "appeng.api.implementations.tiles.ITileStorageMonitorable" );

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.IC2 ) )
		{
			ph.registerNewLayer( "appeng.parts.layers.LayerIEnergySink", "ic2.api.energy.tile.IEnergySink" );
			ph.registerNewLayer( "appeng.parts.layers.LayerIEnergySource", "ic2.api.energy.tile.IEnergySource" );
		}

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.MJ5 ) )
		{
			ph.registerNewLayer( "appeng.parts.layers.LayerIPowerEmitter", "buildcraft.api.power.IPowerEmitter" );
			ph.registerNewLayer( "appeng.parts.layers.LayerIPowerReceptor", "buildcraft.api.power.IPowerReceptor" );
		}

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.MJ6 ) )
			ph.registerNewLayer( "appeng.parts.layers.LayerIBatteryProvider", "buildcraft.api.mj.IBatteryProvider" );

		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.RF ) )
			ph.registerNewLayer( "appeng.parts.layers.LayerIEnergyHandler", "cofh.api.energy.IEnergyReceiver" );

		FMLCommonHandler.instance().bus().register( TickHandler.INSTANCE );
		MinecraftForge.EVENT_BUS.register( TickHandler.INSTANCE );

		PartPlacement pp = new PartPlacement();
		MinecraftForge.EVENT_BUS.register( pp );
		FMLCommonHandler.instance().bus().register( pp );

		IGridCacheRegistry gcr = AEApi.instance().registries().gridCache();
		gcr.registerGridCache( ITickManager.class, TickManagerCache.class );
		gcr.registerGridCache( IEnergyGrid.class, EnergyGridCache.class );
		gcr.registerGridCache( IPathingGrid.class, PathGridCache.class );
		gcr.registerGridCache( IStorageGrid.class, GridStorageCache.class );
		gcr.registerGridCache( P2PCache.class, P2PCache.class );
		gcr.registerGridCache( ISpatialCache.class, SpatialPylonCache.class );
		gcr.registerGridCache( ISecurityGrid.class, SecurityCache.class );
		gcr.registerGridCache( ICraftingGrid.class, CraftingGridCache.class );

		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new AEExternalHandler() );

		AEApi.instance().registries().cell().addCellHandler( new BasicCellHandler() );
		AEApi.instance().registries().cell().addCellHandler( new CreativeCellHandler() );

		AEApi.instance().registries().matterCannon().registerAmmo( AEApi.instance().materials().materialMatterBall.stack( 1 ), 32.0 );

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
		final Parts parts = api.parts();
		final Blocks blocks = api.blocks();
		final Items items = api.items();

		// default settings..
		( ( P2PTunnelRegistry ) registries.p2pTunnel() ).configure();

		// add to localization..
		PlayerMessages.values();
		GuiText.values();

		Api.INSTANCE.partHelper.initFMPSupport();
		( ( BlockCableBus ) blocks.blockMultiPart.block() ).setupTile();

		// Interface
		Upgrades.CRAFTING.registerItem( parts.partInterface, 1 );
		Upgrades.CRAFTING.registerItem( blocks.blockInterface, 1 );

		// IO Port!
		Upgrades.SPEED.registerItem( blocks.blockIOPort, 3 );
		Upgrades.REDSTONE.registerItem( blocks.blockIOPort, 1 );

		// Level Emitter!
		Upgrades.FUZZY.registerItem( parts.partLevelEmitter, 1 );
		Upgrades.CRAFTING.registerItem( parts.partLevelEmitter, 1 );
		Upgrades.REDSTONE.registerItem( parts.partLevelEmitter, 1 );
		Upgrades.SPEED.registerItem( parts.partLevelEmitter, 1 );

		// Import Bus
		Upgrades.FUZZY.registerItem( parts.partImportBus, 1 );
		Upgrades.REDSTONE.registerItem( parts.partImportBus, 1 );
		Upgrades.CAPACITY.registerItem( parts.partImportBus, 2 );
		Upgrades.SPEED.registerItem( parts.partImportBus, 4 );

		// Export Bus
		Upgrades.FUZZY.registerItem( parts.partExportBus, 1 );
		Upgrades.REDSTONE.registerItem( parts.partExportBus, 1 );
		Upgrades.CAPACITY.registerItem( parts.partExportBus, 2 );
		Upgrades.SPEED.registerItem( parts.partExportBus, 4 );
		Upgrades.CRAFTING.registerItem( parts.partExportBus, 1 );

		// Storage Cells
		Upgrades.FUZZY.registerItem( items.itemCell1k, 1 );
		Upgrades.INVERTER.registerItem( items.itemCell1k, 1 );

		Upgrades.FUZZY.registerItem( items.itemCell4k, 1 );
		Upgrades.INVERTER.registerItem( items.itemCell4k, 1 );

		Upgrades.FUZZY.registerItem( items.itemCell16k, 1 );
		Upgrades.INVERTER.registerItem( items.itemCell16k, 1 );

		Upgrades.FUZZY.registerItem( items.itemCell64k, 1 );
		Upgrades.INVERTER.registerItem( items.itemCell64k, 1 );

		Upgrades.FUZZY.registerItem( items.itemPortableCell, 1 );
		Upgrades.INVERTER.registerItem( items.itemPortableCell, 1 );

		Upgrades.FUZZY.registerItem( items.itemViewCell, 1 );
		Upgrades.INVERTER.registerItem( items.itemViewCell, 1 );

		// Storage Bus
		Upgrades.FUZZY.registerItem( parts.partStorageBus, 1 );
		Upgrades.INVERTER.registerItem( parts.partStorageBus, 1 );
		Upgrades.CAPACITY.registerItem( parts.partStorageBus, 5 );

		// Formation Plane
		Upgrades.FUZZY.registerItem( parts.partFormationPlane, 1 );
		Upgrades.INVERTER.registerItem( parts.partFormationPlane, 1 );
		Upgrades.CAPACITY.registerItem( parts.partFormationPlane, 5 );

		// Matter Cannon
		Upgrades.FUZZY.registerItem( items.itemMassCannon, 1 );
		Upgrades.INVERTER.registerItem( items.itemMassCannon, 1 );
		Upgrades.SPEED.registerItem( items.itemMassCannon, 4 );

		// Molecular Assembler
		Upgrades.SPEED.registerItem( blocks.blockMolecularAssembler, 5 );

		// Inscriber
		Upgrades.SPEED.registerItem( blocks.blockInscriber, 3 );

		if ( items.itemWirelessTerminal != null )
		{
			registries.wireless().registerWirelessHandler( ( IWirelessTermHandler ) items.itemWirelessTerminal.item() );
		}

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.ChestLoot ) )
		{
			ChestGenHooks d = ChestGenHooks.getInfo( ChestGenHooks.MINESHAFT_CORRIDOR );
			d.addItem( new WeightedRandomChestContent( api.materials().materialCertusQuartzCrystal.stack( 1 ), 1, 4, 2 ) );
			d.addItem( new WeightedRandomChestContent( api.materials().materialCertusQuartzDust.stack( 1 ), 1, 4, 2 ) );
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
