package appeng.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.Blocks;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
import appeng.api.definitions.Parts;
import appeng.api.features.IRecipeHandlerRegistry;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.networking.IGridCacheRegistry;
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
import appeng.block.storage.BlockChest;
import appeng.block.storage.BlockDrive;
import appeng.block.storage.BlockIOPort;
import appeng.block.storage.BlockSkyChest;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.DamagedItemDefinition;
import appeng.core.features.IAEFeature;
import appeng.core.features.ItemStackSrc;
import appeng.core.features.NullItemDefinition;
import appeng.core.features.WrappedDamageItemDefinition;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.features.registries.entries.BasicCellHandler;
import appeng.core.features.registries.entries.CreativeCellHandler;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.debug.BlockChunkloader;
import appeng.debug.BlockItemGen;
import appeng.debug.ToolDebugCard;
import appeng.debug.ToolEraser;
import appeng.debug.ToolMeteoritePlacer;
import appeng.debug.ToolReplicatorCard;
import appeng.hooks.AETrading;
import appeng.hooks.MeteoriteWorldGen;
import appeng.hooks.QuartzWorldGen;
import appeng.hooks.TickHandler;
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
import appeng.me.cache.CraftingCache;
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
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.game.ShapedRecipe;
import appeng.recipes.game.ShapelessRecipe;
import appeng.recipes.handlers.Crusher;
import appeng.recipes.handlers.Grind;
import appeng.recipes.handlers.GrindFZ;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Macerator;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;

public class Registration
{

	final public static Registration instance = new Registration();

	public RecipeHandler recipeHandler;
	public BiomeGenBase storageBiome;

	private Registration() {
		recipeHandler = new RecipeHandler();
	}

	final private Multimap<AEFeature, Class> featuresToEntities = ArrayListMultimap.create();

	public void PreInit(FMLPreInitializationEvent event)
	{
		registerSpatial( false );

		IRecipeHandlerRegistry recipeRegistery = AEApi.instance().registries().recipes();
		recipeRegistery.addNewSubItemResolver( new AEItemResolver() );

		recipeRegistery.addNewCraftHandler( "grind", Grind.class );
		recipeRegistery.addNewCraftHandler( "crusher", Crusher.class );
		recipeRegistery.addNewCraftHandler( "grindfz", GrindFZ.class );
		recipeRegistery.addNewCraftHandler( "pulverizer", Pulverizer.class );
		recipeRegistery.addNewCraftHandler( "macerator", Macerator.class );

		recipeRegistery.addNewCraftHandler( "smelt", Smelt.class );
		recipeRegistery.addNewCraftHandler( "inscribe", Inscribe.class );
		recipeRegistery.addNewCraftHandler( "press", Press.class );

		recipeRegistery.addNewCraftHandler( "shaped", Shaped.class );
		recipeRegistery.addNewCraftHandler( "shapeless", Shapeless.class );

		RecipeSorter.register( "AE2-Facade", FacadeRecipe.class, Category.SHAPED, "" );
		RecipeSorter.register( "AE2-Shaped", ShapedRecipe.class, Category.SHAPED, "" );
		RecipeSorter.register( "AE2-Shapeless", ShapelessRecipe.class, Category.SHAPELESS, "" );

		MinecraftForge.EVENT_BUS.register( OreDictionaryHandler.instance );

		Items items = appeng.core.Api.instance.items();
		Materials materials = appeng.core.Api.instance.materials();
		Parts parts = appeng.core.Api.instance.parts();
		Blocks blocks = appeng.core.Api.instance.blocks();

		AEItemDefinition materialItem = (AEFeatureHandler) addFeature( ItemMultiMaterial.class );

		Class materialClass = materials.getClass();
		for (MaterialType mat : MaterialType.values())
		{
			try
			{
				Field f = materialClass.getField( "material" + mat.name() );
				ItemStackSrc is = ((ItemMultiMaterial) materialItem.item()).createMaterial( mat );
				if ( is != null )
					f.set( materials, new DamagedItemDefinition( is ) );
				else
					f.set( materials, new NullItemDefinition() );
			}
			catch (Throwable err)
			{
				AELog.severe( "Error creating material: " + mat.name() );
				throw new RuntimeException( err );
			}
		}

		AEItemDefinition partItem = (AEFeatureHandler) addFeature( ItemMultiPart.class );

		Class partClass = parts.getClass();
		for (PartType type : PartType.values())
		{
			try
			{
				Field f = partClass.getField( "part" + type.name() );
				Enum varients[] = type.getVarients();
				if ( varients == null )
				{
					ItemStackSrc is = ((ItemMultiPart) partItem.item()).createPart( type, null );
					if ( is != null )
						f.set( parts, new DamagedItemDefinition( is ) );
					else
						f.set( parts, new NullItemDefinition() );
				}
				else
				{
					if ( varients[0] instanceof AEColor )
					{
						ColoredItemDefinition def = new ColoredItemDefinition();

						for (Enum v : varients)
						{
							ItemStackSrc is = ((ItemMultiPart) partItem.item()).createPart( type, v );
							if ( is != null )
								def.add( (AEColor) v, is );
						}

						f.set( parts, def );
					}
				}
			}
			catch (Throwable err)
			{
				AELog.severe( "Error creating part: " + type.name() );
				throw new RuntimeException( err );
			}
		}

		// very important block!
		blocks.blockMultiPart = addFeature( BlockCableBus.class );

		blocks.blockCraftingUnit = addFeature( BlockCraftingUnit.class );
		blocks.blockCraftingAccelerator = new WrappedDamageItemDefinition( blocks.blockCraftingUnit, 1 );
		blocks.blockCraftingMonitor = addFeature( BlockCraftingMonitor.class );
		blocks.blockCraftingStorage1k = addFeature( BlockCraftingStorage.class );
		blocks.blockCraftingStorage4k = new WrappedDamageItemDefinition( blocks.blockCraftingStorage1k, 1 );
		blocks.blockCraftingStorage16k = new WrappedDamageItemDefinition( blocks.blockCraftingStorage1k, 2 );
		blocks.blockCraftingStorage64k = new WrappedDamageItemDefinition( blocks.blockCraftingStorage1k, 3 );
		blocks.blockMolecularAssembler = addFeature( BlockMolecularAssembler.class );

		blocks.blockQuartzOre = addFeature( OreQuartz.class );
		blocks.blockQuartzOreCharged = addFeature( OreQuartzCharged.class );
		blocks.blockMatrixFrame = addFeature( BlockMatrixFrame.class );
		blocks.blockPaint = addFeature( BlockPaint.class );
		blocks.blockQuartz = addFeature( BlockQuartz.class );
		blocks.blockFluix = addFeature( BlockFluix.class );
		blocks.blockSkyStone = addFeature( BlockSkyStone.class );
		blocks.blockSkyChest = addFeature( BlockSkyChest.class );
		blocks.blockSkyCompass = addFeature( BlockSkyCompass.class );

		blocks.blockQuartzGlass = addFeature( BlockQuartzGlass.class );
		blocks.blockQuartzVibrantGlass = addFeature( BlockQuartzLamp.class );
		blocks.blockQuartzPiller = addFeature( BlockQuartzPillar.class );
		blocks.blockQuartzChiseled = addFeature( BlockQuartzChiseled.class );
		blocks.blockQuartzTorch = addFeature( BlockQuartzTorch.class );
		blocks.blockCharger = addFeature( BlockCharger.class );
		blocks.blockQuartzGrowthAccelerator = addFeature( BlockQuartzGrowthAccelerator.class );

		blocks.blockGrindStone = addFeature( BlockGrinder.class );
		blocks.blockCrankHandle = addFeature( BlockCrank.class );
		blocks.blockInscriber = addFeature( BlockInscriber.class );
		blocks.blockWireless = addFeature( BlockWireless.class );
		blocks.blockTinyTNT = addFeature( BlockTinyTNT.class );

		blocks.blockQuantumRing = addFeature( BlockQuantumRing.class );
		blocks.blockQuantumLink = addFeature( BlockQuantumLinkChamber.class );

		blocks.blockSpatialPylon = addFeature( BlockSpatialPylon.class );
		blocks.blockSpatialIOPort = addFeature( BlockSpatialIOPort.class );

		blocks.blockController = addFeature( BlockController.class );
		blocks.blockDrive = addFeature( BlockDrive.class );
		blocks.blockChest = addFeature( BlockChest.class );
		blocks.blockInterface = addFeature( BlockInterface.class );
		blocks.blockCellWorkbench = addFeature( BlockCellWorkbench.class );
		blocks.blockIOPort = addFeature( BlockIOPort.class );
		blocks.blockCondenser = addFeature( BlockCondenser.class );
		blocks.blockEnergyAcceptor = addFeature( BlockEnergyAcceptor.class );
		blocks.blockVibrationChamber = addFeature( BlockVibrationChamber.class );

		blocks.blockEnergyCell = addFeature( BlockEnergyCell.class );
		blocks.blockEnergyCellDense = addFeature( BlockDenseEnergyCell.class );
		blocks.blockEnergyCellCreative = addFeature( BlockCreativeEnergyCell.class );

		blocks.blockSecurity = addFeature( BlockSecurity.class );

		items.itemCellCreative = addFeature( ItemCreativeStorageCell.class );
		items.itemViewCell = addFeature( ItemViewCell.class );
		items.itemEncodedPattern = addFeature( ItemEncodedPattern.class );

		items.itemCell1k = addFeature( ItemBasicStorageCell.class, MaterialType.Cell1kPart, 1 );
		items.itemCell4k = addFeature( ItemBasicStorageCell.class, MaterialType.Cell4kPart, 4 );
		items.itemCell16k = addFeature( ItemBasicStorageCell.class, MaterialType.Cell16kPart, 16 );
		items.itemCell64k = addFeature( ItemBasicStorageCell.class, MaterialType.Cell64kPart, 64 );

		items.itemSpatialCell2 = addFeature( ItemSpatialStorageCell.class, MaterialType.Cell2SpatialPart, 2 );
		items.itemSpatialCell16 = addFeature( ItemSpatialStorageCell.class, MaterialType.Cell16SpatialPart, 16 );
		items.itemSpatialCell128 = addFeature( ItemSpatialStorageCell.class, MaterialType.Cell128SpatialPart, 128 );

		items.itemCertusQuartzKnife = addFeature( ToolQuartzCuttingKnife.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzWrench = addFeature( ToolQuartzWrench.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzAxe = addFeature( ToolQuartzAxe.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzHoe = addFeature( ToolQuartzHoe.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzPick = addFeature( ToolQuartzPickaxe.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzShovel = addFeature( ToolQuartzSpade.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzSword = addFeature( ToolQuartzSword.class, AEFeature.CertusQuartzTools );

		items.itemNetherQuartzKnife = addFeature( ToolQuartzCuttingKnife.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzWrench = addFeature( ToolQuartzWrench.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzAxe = addFeature( ToolQuartzAxe.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzHoe = addFeature( ToolQuartzHoe.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzPick = addFeature( ToolQuartzPickaxe.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzShovel = addFeature( ToolQuartzSpade.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzSword = addFeature( ToolQuartzSword.class, AEFeature.NetherQuartzTools );

		items.itemMassCannon = addFeature( ToolMassCannon.class );
		items.itemMemoryCard = addFeature( ToolMemoryCard.class );
		items.itemChargedStaff = addFeature( ToolChargedStaff.class );
		items.itemEntropyManipulator = addFeature( ToolEntropyManipulator.class );
		items.itemColorApplicator = addFeature( ToolColorApplicator.class );

		items.itemWirelessTerminal = addFeature( ToolWirelessTerminal.class );
		items.itemNetworkTool = addFeature( ToolNetworkTool.class );
		items.itemPortableCell = addFeature( ToolPortableCell.class );
		items.itemBiometricCard = addFeature( ToolBiometricCard.class );

		items.itemFacade = addFeature( ItemFacade.class );
		items.itemCrystalSeed = addFeature( ItemCrystalSeed.class );
		items.itemPaintBall = addFeature( ItemPaintBall.class );

		addFeature( ToolEraser.class );
		addFeature( ToolMeteoritePlacer.class );
		addFeature( ToolDebugCard.class );
		addFeature( ToolReplicatorCard.class );
		addFeature( BlockItemGen.class );
		addFeature( BlockChunkloader.class );
	}

	private AEItemDefinition addFeature(Class c, Object... Args)
	{

		try
		{
			java.lang.reflect.Constructor[] con = c.getConstructors();
			Object obj = null;

			for (Constructor conItem : con)
			{
				Class paramTypes[] = conItem.getParameterTypes();
				if ( paramTypes.length == Args.length )
				{
					boolean valid = true;

					for (int idx = 0; idx < paramTypes.length; idx++)
					{
						Class cz = Args[idx].getClass();
						if ( !isClassMatch( paramTypes[idx], cz, Args[idx] ) )
							valid = false;
					}

					if ( valid )
					{
						obj = conItem.newInstance( Args );
						break;
					}
				}
			}

			if ( obj instanceof IAEFeature )
			{
				IAEFeature feature = (IAEFeature) obj;

				for (AEFeature f : feature.feature().getFeatures())
					featuresToEntities.put( f, c );

				feature.feature().register();

				feature.postInit();

				return feature.feature();
			}
			else if ( obj == null )
				throw new RuntimeException( "No valid constructor found." );
			else
				throw new RuntimeException( "Non AE Feature Registered" );

		}
		catch (Throwable e)
		{
			throw new RuntimeException( "Error with Feature: " + c.getName(), e );
		}
	}

	private boolean isClassMatch(Class expected, Class got, Object value)
	{
		if ( value == null && !expected.isPrimitive() )
			return true;

		expected = condense( expected, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class );
		got = condense( got, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class );

		if ( expected == got || expected.isAssignableFrom( got ) )
			return true;

		return false;
	}

	private Class condense(Class expected, Class... wrappers)
	{
		if ( expected.isPrimitive() )
		{
			for (Class clz : wrappers)
			{
				try
				{
					if ( expected == clz.getField( "TYPE" ).get( null ) )
						return clz;
				}
				catch (Throwable t)
				{
					AELog.error( t );
				}
			}
		}
		return expected;
	}

	public void Init(FMLInitializationEvent event)
	{
		if ( AEConfig.instance.isFeatureEnabled( AEFeature.CustomRecipes ) )
			recipeHandler.parseRecipes( new ConfigLoader( AppEng.instance.getConfigPath() ), "index.recipe" );
		else
			recipeHandler.parseRecipes( new JarLoader( "/assets/appliedenergistics2/recipes/" ), "index.recipe" );

		IPartHelper ph = AEApi.instance().partHelper();
		ph.registerNewLayer( "appeng.api.parts.layers.LayerISidedInventory", "net.minecraft.inventory.ISidedInventory" );
		ph.registerNewLayer( "appeng.api.parts.layers.LayerIFluidHandler", "net.minecraftforge.fluids.IFluidHandler" );
		ph.registerNewLayer( "appeng.api.parts.layers.LayerITileStorageMonitorable", "appeng.api.implementations.tiles.ITileStorageMonitorable" );

		if ( AppEng.instance.isIntegrationEnabled( "IC2" ) )
		{
			ph.registerNewLayer( "appeng.api.parts.layers.LayerIEnergySink", "ic2.api.energy.tile.IEnergySink" );
			ph.registerNewLayer( "appeng.api.parts.layers.LayerIEnergySource", "ic2.api.energy.tile.IEnergySource" );
		}

		if ( AppEng.instance.isIntegrationEnabled( "MJ5" ) )
		{
			ph.registerNewLayer( "appeng.api.parts.layers.LayerIPowerEmitter", "buildcraft.api.power.IPowerEmitter" );
			ph.registerNewLayer( "appeng.api.parts.layers.LayerIPowerReceptor", "buildcraft.api.power.IPowerReceptor" );
		}

		if ( AppEng.instance.isIntegrationEnabled( "MJ6" ) )
		{
			ph.registerNewLayer( "appeng.api.parts.layers.LayerIBatteryProvider", "buildcraft.api.mj.IBatteryProvider" );
		}

		if ( AppEng.instance.isIntegrationEnabled( "RF" ) )
			ph.registerNewLayer( "appeng.api.parts.layers.LayerIEnergyHandler", "cofh.api.energy.IEnergyHandler" );

		FMLCommonHandler.instance().bus().register( TickHandler.instance );
		MinecraftForge.EVENT_BUS.register( TickHandler.instance );
		MinecraftForge.EVENT_BUS.register( new PartPlacement() );

		IGridCacheRegistry gcr = AEApi.instance().registries().gridCache();
		gcr.registerGridCache( ITickManager.class, TickManagerCache.class );
		gcr.registerGridCache( IEnergyGrid.class, EnergyGridCache.class );
		gcr.registerGridCache( IPathingGrid.class, PathGridCache.class );
		gcr.registerGridCache( IStorageGrid.class, GridStorageCache.class );
		gcr.registerGridCache( P2PCache.class, P2PCache.class );
		gcr.registerGridCache( ISpatialCache.class, SpatialPylonCache.class );
		gcr.registerGridCache( ISecurityGrid.class, SecurityCache.class );
		gcr.registerGridCache( CraftingCache.class, CraftingCache.class );

		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new AEExternalHandler() );

		AEApi.instance().registries().cell().addCellHandler( new BasicCellHandler() );
		AEApi.instance().registries().cell().addCellHandler( new CreativeCellHandler() );

		AEApi.instance().registries().matterCannon().registerAmmo( AEApi.instance().materials().materialMatterBall.stack( 1 ), 32.0 );

		// default settings..
		((P2PTunnelRegistry) AEApi.instance().registries().p2pTunnel()).configure();

		// NetworkRegistry.instance().registerGuiHandler( AppEng.instance, GuiBridge.GUI_Handler );
	}

	public void PostInit(FMLPostInitializationEvent event)
	{
		registerSpatial( true );

		// add to localizaiton..
		PlayerMessages.values();
		GuiText.values();

		Api.instance.partHelper.initFMPSupport();
		((BlockCableBus) AEApi.instance().blocks().blockMultiPart.block()).setupTile();

		// IO PORT!
		Upgrades.SPEED.registerItem( AEApi.instance().blocks().blockIOPort.stack( 1 ), 3 );
		Upgrades.REDSTONE.registerItem( AEApi.instance().blocks().blockIOPort.stack( 1 ), 1 );

		// partLevelEmitter
		Upgrades.FUZZY.registerItem( AEApi.instance().parts().partLevelEmitter.stack( 1 ), 1 );

		// partImportBus
		Upgrades.FUZZY.registerItem( AEApi.instance().parts().partImportBus.stack( 1 ), 1 );
		Upgrades.REDSTONE.registerItem( AEApi.instance().parts().partImportBus.stack( 1 ), 1 );
		Upgrades.CAPACITY.registerItem( AEApi.instance().parts().partImportBus.stack( 1 ), 2 );
		Upgrades.SPEED.registerItem( AEApi.instance().parts().partImportBus.stack( 1 ), 4 );

		// partExportBus
		Upgrades.FUZZY.registerItem( AEApi.instance().parts().partExportBus.stack( 1 ), 1 );
		Upgrades.REDSTONE.registerItem( AEApi.instance().parts().partExportBus.stack( 1 ), 1 );
		Upgrades.CAPACITY.registerItem( AEApi.instance().parts().partExportBus.stack( 1 ), 2 );
		Upgrades.SPEED.registerItem( AEApi.instance().parts().partExportBus.stack( 1 ), 4 );

		// blockCellWorkbench
		Upgrades.FUZZY.registerItem( AEApi.instance().items().itemCell1k.stack( 1 ), 1 );
		Upgrades.INVERTER.registerItem( AEApi.instance().items().itemCell1k.stack( 1 ), 1 );

		Upgrades.FUZZY.registerItem( AEApi.instance().items().itemCell4k.stack( 1 ), 1 );
		Upgrades.INVERTER.registerItem( AEApi.instance().items().itemCell4k.stack( 1 ), 1 );

		Upgrades.FUZZY.registerItem( AEApi.instance().items().itemCell16k.stack( 1 ), 1 );
		Upgrades.INVERTER.registerItem( AEApi.instance().items().itemCell16k.stack( 1 ), 1 );

		Upgrades.FUZZY.registerItem( AEApi.instance().items().itemCell64k.stack( 1 ), 1 );
		Upgrades.INVERTER.registerItem( AEApi.instance().items().itemCell64k.stack( 1 ), 1 );

		Upgrades.FUZZY.registerItem( AEApi.instance().items().itemPortableCell.stack( 1 ), 1 );
		Upgrades.INVERTER.registerItem( AEApi.instance().items().itemPortableCell.stack( 1 ), 1 );

		Upgrades.FUZZY.registerItem( AEApi.instance().items().itemViewCell.stack( 1 ), 1 );
		Upgrades.INVERTER.registerItem( AEApi.instance().items().itemViewCell.stack( 1 ), 1 );

		// partStorageBus
		Upgrades.FUZZY.registerItem( AEApi.instance().parts().partStorageBus.stack( 1 ), 1 );
		Upgrades.INVERTER.registerItem( AEApi.instance().parts().partStorageBus.stack( 1 ), 1 );
		Upgrades.CAPACITY.registerItem( AEApi.instance().parts().partStorageBus.stack( 1 ), 5 );

		// partFormationPlane
		Upgrades.FUZZY.registerItem( AEApi.instance().parts().partFormationPlane.stack( 1 ), 1 );
		Upgrades.INVERTER.registerItem( AEApi.instance().parts().partFormationPlane.stack( 1 ), 1 );
		Upgrades.CAPACITY.registerItem( AEApi.instance().parts().partFormationPlane.stack( 1 ), 5 );

		// matter cannon
		Upgrades.FUZZY.registerItem( AEApi.instance().items().itemMassCannon.stack( 1 ), 1 );
		Upgrades.INVERTER.registerItem( AEApi.instance().items().itemMassCannon.stack( 1 ), 1 );
		Upgrades.SPEED.registerItem( AEApi.instance().items().itemMassCannon.stack( 1 ), 4 );

		// molecular assembler
		Upgrades.REDSTONE.registerItem( AEApi.instance().blocks().blockMolecularAssembler.stack( 1 ), 1 );
		Upgrades.SPEED.registerItem( AEApi.instance().blocks().blockMolecularAssembler.stack( 1 ), 5 );

		AEApi.instance().registries().wireless().registerWirelessHandler( (IWirelessTermHandler) AEApi.instance().items().itemWirelessTerminal.item() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.ChestLoot ) )
		{
			ChestGenHooks d = ChestGenHooks.getInfo( ChestGenHooks.MINESHAFT_CORRIDOR );
			d.addItem( new WeightedRandomChestContent( AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ), 1, 4, 2 ) );
			d.addItem( new WeightedRandomChestContent( AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ), 1, 4, 2 ) );
		}

		// add villager trading to black smiths for a few basic materials
		if ( AEConfig.instance.isFeatureEnabled( AEFeature.VillagerTrading ) )
			VillagerRegistry.instance().registerVillageTradeHandler( 3, new AETrading() );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.CertusQuartzWorldGen ) )
			GameRegistry.registerWorldGenerator( new QuartzWorldGen(), 0 );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.MeteoriteWorldGen ) )
			GameRegistry.registerWorldGenerator( new MeteoriteWorldGen(), 0 );

		IMovableRegistry mr = AEApi.instance().registries().moveable();

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

		recipeHandler.registerHandlers();

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.enableFacadeCrafting ) )
			CraftingManager.getInstance().getRecipeList().add( new FacadeRecipe() );

	}

	private void registerSpatial(boolean force)
	{
		AEConfig config = AEConfig.instance;

		if ( storageBiome == null )
		{
			if ( force && config.storageBiomeID == -1 )
			{
				storageBiome = new BiomeGenStorage( config.storageBiomeID = Platform.findEmpty( BiomeGenBase.getBiomeGenArray() ) );
				config.save();
			}

			if ( !force && config.storageBiomeID != -1 )
				storageBiome = new BiomeGenStorage( config.storageBiomeID );
		}

		if ( config.storageProviderID != -1 )
		{
			DimensionManager.registerProviderType( config.storageProviderID, StorageWorldProvider.class, false );
		}

		if ( config.storageProviderID == -1 && force )
		{
			config.storageProviderID = -11;

			while (!DimensionManager.registerProviderType( config.storageProviderID, StorageWorldProvider.class, false ))
				config.storageProviderID--;

			config.save();
		}
	}

}
