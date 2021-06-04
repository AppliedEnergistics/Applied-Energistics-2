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


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IParts;
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
import appeng.bootstrap.ICriterionTriggerRegistry;
import appeng.bootstrap.IModelRegistry;
import appeng.bootstrap.components.IBlockRegistrationComponent;
import appeng.bootstrap.components.IEntityRegistrationComponent;
import appeng.bootstrap.components.IInitComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.components.IModelRegistrationComponent;
import appeng.bootstrap.components.IOreDictComponent;
import appeng.bootstrap.components.IPostInitComponent;
import appeng.bootstrap.components.IPreInitComponent;
import appeng.bootstrap.components.IRecipeRegistrationComponent;
import appeng.capabilities.Capabilities;
import appeng.core.features.AEFeature;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.features.registries.cell.BasicCellHandler;
import appeng.core.features.registries.cell.BasicItemCellGuiHandler;
import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.PartItemPredicate;
import appeng.core.stats.Stats;
import appeng.core.worlddata.SpatialDimensionManager;
import appeng.fluids.registries.BasicFluidCellGuiHandler;
import appeng.hooks.TickHandler;
import appeng.items.materials.ItemMaterial;
import appeng.items.parts.ItemFacade;
import appeng.items.parts.ItemPart;
import appeng.loot.ChestLoot;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cache.EnergyGridCache;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.P2PCache;
import appeng.me.cache.PathGridCache;
import appeng.me.cache.SecurityCache;
import appeng.me.cache.SpatialPylonCache;
import appeng.me.cache.TickManagerCache;
import appeng.parts.PartPlacement;
import appeng.recipes.AEItemResolver;
import appeng.recipes.AERecipeLoader;
import appeng.recipes.game.DisassembleRecipe;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.ores.OreDictionaryHandler;
import appeng.spatial.BiomeGenStorage;
import appeng.spatial.StorageWorldProvider;
import appeng.tile.AEBaseTile;
import appeng.worldgen.MeteoriteWorldGen;
import appeng.worldgen.QuartzWorldGen;


final class Registration
{
	DimensionType storageDimensionType;
	int storageDimensionID;
	Biome storageBiome;
	AdvancementTriggers advancementTriggers;

	void preInitialize( final FMLPreInitializationEvent event )
	{
		Capabilities.register();

		final Api api = Api.INSTANCE;
		final IRecipeHandlerRegistry recipeRegistry = api.registries().recipes();
		this.registerCraftHandlers( recipeRegistry );

		MinecraftForge.EVENT_BUS.register( OreDictionaryHandler.INSTANCE );

		ApiDefinitions definitions = api.definitions();

		// Register
		definitions.getRegistry().getBootstrapComponents( IPreInitComponent.class ).forEachRemaining( b -> b.preInitialize( event.getSide() ) );
	}

	private void registerSpatialBiome( IForgeRegistry<Biome> registry )
	{
		if( !AEConfig.instance().isFeatureEnabled( AEFeature.SPATIAL_IO ) )
		{
			return;
		}

		if( this.storageBiome == null )
		{
			this.storageBiome = new BiomeGenStorage();
		}
		registry.register( this.storageBiome.setRegistryName( "appliedenergistics2:storage_biome" ) );
	}

	private void registerSpatialDimension()
	{
		final AEConfig config = AEConfig.instance();
		if( !config.isFeatureEnabled( AEFeature.SPATIAL_IO ) )
		{
			return;
		}

		if( config.getStorageProviderID() == -1 )
		{
			final Set<Integer> ids = new HashSet<>();
			for( DimensionType type : DimensionType.values() )
			{
				ids.add( type.getId() );
			}

			int newId = -11;
			while( ids.contains( newId ) )
			{
				--newId;
			}
			config.setStorageProviderID( newId );
			config.save();
		}

		this.storageDimensionType = DimensionType.register( "Storage Cell", "_cell", config.getStorageProviderID(), StorageWorldProvider.class, true );

		if( config.getStorageDimensionID() == -1 )
		{
			config.setStorageDimensionID( DimensionManager.getNextFreeDimId() );
			config.save();
		}
		this.storageDimensionID = config.getStorageDimensionID();

		DimensionManager.registerDimension( this.storageDimensionID, this.storageDimensionType );
	}

	private void registerCraftHandlers( final IRecipeHandlerRegistry registry )
	{
		registry.addNewSubItemResolver( new AEItemResolver() );
	}

	public void initialize( @Nonnull final FMLInitializationEvent event, @Nonnull final File recipeDirectory )
	{
		Preconditions.checkNotNull( event );
		Preconditions.checkNotNull( recipeDirectory );
		Preconditions.checkArgument( !recipeDirectory.isFile() );

		final Api api = Api.INSTANCE;
		final IRegistryContainer registries = api.registries();

		ApiDefinitions definitions = api.definitions();
		definitions.getRegistry().getBootstrapComponents( IInitComponent.class ).forEachRemaining( b -> b.initialize( event.getSide() ) );

		MinecraftForge.EVENT_BUS.register( TickHandler.INSTANCE );

		MinecraftForge.EVENT_BUS.register( new PartPlacement() );

		if( AEConfig.instance().isFeatureEnabled( AEFeature.CHEST_LOOT ) )
		{
			MinecraftForge.EVENT_BUS.register( new ChestLoot() );
		}

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
		Stats.register();
		this.advancementTriggers = new AdvancementTriggers( new CriterionTrigggerRegistry() );
	}

	@SubscribeEvent
	public void registerBiomes( RegistryEvent.Register<Biome> event )
	{
		final IForgeRegistry<Biome> registry = event.getRegistry();
		this.registerSpatialBiome( registry );
	}

	@SubscribeEvent
	@SideOnly( Side.CLIENT )
	public void modelRegistryEvent( ModelRegistryEvent event )
	{
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		final IModelRegistry registry = new ModelLoaderWrapper();
		final Side side = FMLCommonHandler.instance().getEffectiveSide();
		definitions.getRegistry().getBootstrapComponents( IModelRegistrationComponent.class ).forEachRemaining( b -> b.modelRegistration( side, registry ) );
	}

	@SubscribeEvent
	public void registerBlocks( RegistryEvent.Register<Block> event )
	{
		final IForgeRegistry<Block> registry = event.getRegistry();
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		final Side side = FMLCommonHandler.instance().getEffectiveSide();
		definitions.getRegistry().getBootstrapComponents( IBlockRegistrationComponent.class ).forEachRemaining( b -> b.blockRegistration( side, registry ) );
	}

	@SubscribeEvent
	public void registerItems( RegistryEvent.Register<Item> event )
	{
		final IForgeRegistry<Item> registry = event.getRegistry();
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		final Side side = FMLCommonHandler.instance().getEffectiveSide();
		definitions.getRegistry().getBootstrapComponents( IItemRegistrationComponent.class ).forEachRemaining( b -> b.itemRegistration( side, registry ) );
		// register oredicts
		definitions.getRegistry().getBootstrapComponents( IOreDictComponent.class ).forEachRemaining( b -> b.oreRegistration( side ) );
		ItemMaterial.instance.registerOredicts();
		ItemPart.instance.registerOreDicts();
	}

	@SubscribeEvent
	public void registerRecipes( RegistryEvent.Register<IRecipe> event )
	{
		final IForgeRegistry<IRecipe> registry = event.getRegistry();

		final Api api = Api.INSTANCE;
		final ApiDefinitions definitions = api.definitions();
		final Side side = FMLCommonHandler.instance().getEffectiveSide();

		if( AEConfig.instance().isFeatureEnabled( AEFeature.ENABLE_DISASSEMBLY_CRAFTING ) )
		{
			DisassembleRecipe r = new DisassembleRecipe();
			registry.register( r.setRegistryName( AppEng.MOD_ID.toLowerCase(), "disassemble" ) );
		}

		if( AEConfig.instance().isFeatureEnabled( AEFeature.ENABLE_FACADE_CRAFTING ) )
		{
			definitions.items().facade().maybeItem().ifPresent( facadeItem ->
			{
				FacadeRecipe f = new FacadeRecipe( (ItemFacade) facadeItem );
				registry.register( f.setRegistryName( AppEng.MOD_ID.toLowerCase(), "facade" ) );
			} );
		}

		definitions.getRegistry().getBootstrapComponents( IRecipeRegistrationComponent.class ).forEachRemaining( b -> b.recipeRegistration( side, registry ) );

		final AERecipeLoader ldr = new AERecipeLoader();
		ldr.loadProcessingRecipes();
	}

	@SubscribeEvent
	public void registerEntities( RegistryEvent.Register<EntityEntry> event )
	{
		final IForgeRegistry<EntityEntry> registry = event.getRegistry();
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		definitions.getRegistry().getBootstrapComponents( IEntityRegistrationComponent.class ).forEachRemaining( b -> b.entityRegistration( registry ) );
	}

	@SubscribeEvent
	public void attachSpatialDimensionManager( AttachCapabilitiesEvent<World> event )
	{
		if( AEConfig.instance()
				.isFeatureEnabled( AEFeature.SPATIAL_IO ) && event.getObject() == DimensionManager.getWorld( AEConfig.instance().getStorageDimensionID() ) )
		{
			event.addCapability( new ResourceLocation( "appliedenergistics2:spatial_dimension_manager" ), new SpatialDimensionManager( event.getObject() ) );
		}
	}

	void postInit( final FMLPostInitializationEvent event )
	{
		final IRegistryContainer registries = Api.INSTANCE.registries();
		ApiDefinitions definitions = Api.INSTANCE.definitions();
		final IParts parts = definitions.parts();
		final IBlocks blocks = definitions.blocks();
		final IItems items = definitions.items();

		this.registerSpatialDimension();

		// default settings..
		( (P2PTunnelRegistry) registries.p2pTunnel() ).configure();

		// add to localization..
		PlayerMessages.values();
		GuiText.values();

		definitions.getRegistry().getBootstrapComponents( IPostInitComponent.class ).forEachRemaining( b -> b.postInitialize( event.getSide() ) );

		// Interface
		Upgrades.CRAFTING.registerItem( parts.iface(), 1 );
		Upgrades.CRAFTING.registerItem( blocks.iface(), 1 );
		Upgrades.PATTERN_EXPANSION.registerItem( parts.iface(), 3 );
		Upgrades.PATTERN_EXPANSION.registerItem( blocks.iface(), 3 );

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

		// add villager trading to black smiths for a few basic materials
		if( AEConfig.instance().isFeatureEnabled( AEFeature.VILLAGER_TRADING ) )
		{
			// TODO: VILLAGER TRADING
			// VillagerRegistry.instance().getRegisteredVillagers().registerVillageTradeHandler( 3, new AETrading() );
		}

		if( AEConfig.instance().isFeatureEnabled( AEFeature.CERTUS_QUARTZ_WORLD_GEN ) )
		{
			GameRegistry.registerWorldGenerator( new QuartzWorldGen(), 0 );
		}

		if( AEConfig.instance().isFeatureEnabled( AEFeature.METEORITE_WORLD_GEN ) )
		{
			GameRegistry.registerWorldGenerator( new MeteoriteWorldGen(), 0 );
		}

		final IMovableRegistry mr = registries.movable();

		/*
		 * You can't move bed rock.
		 */
		mr.blacklistBlock( net.minecraft.init.Blocks.BEDROCK );

		/*
		 * White List Vanilla...
		 */
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityBanner.class );
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
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityFlowerPot.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityFurnace.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityHopper.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityMobSpawner.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityNote.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityPiston.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntityShulkerBox.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntitySign.class );
		mr.whiteListTileEntity( net.minecraft.tileentity.TileEntitySkull.class );

		/*
		 * Whitelist AE2
		 */
		mr.whiteListTileEntity( AEBaseTile.class );

		/*
		 * world gen
		 */
		for( final WorldGenType type : WorldGenType.values() )
		{
			registries.worldgen().disableWorldGenForProviderID( type, StorageWorldProvider.class );

			// nether
			registries.worldgen().disableWorldGenForDimension( type, -1 );

			// end
			registries.worldgen().disableWorldGenForDimension( type, 1 );
		}

		// whitelist from config
		for( final int dimension : AEConfig.instance().getMeteoriteDimensionWhitelist() )
		{
			registries.worldgen().enableWorldGenForDimension( WorldGenType.METEORITES, dimension );
		}
	}

	private static class ModelLoaderWrapper implements IModelRegistry
	{

		@Override
		public void registerItemVariants( Item item, ResourceLocation... names )
		{
			ModelLoader.registerItemVariants( item, names );
		}

		@Override
		public void setCustomModelResourceLocation( Item item, int metadata, ModelResourceLocation model )
		{
			ModelLoader.setCustomModelResourceLocation( item, metadata, model );
		}

		@Override
		public void setCustomMeshDefinition( Item item, ItemMeshDefinition meshDefinition )
		{
			ModelLoader.setCustomMeshDefinition( item, meshDefinition );
		}

		@Override
		public void setCustomStateMapper( Block block, IStateMapper mapper )
		{
			ModelLoader.setCustomStateMapper( block, mapper );
		}
	}

	private static class CriterionTrigggerRegistry implements ICriterionTriggerRegistry
	{
		private Method method;

		CriterionTrigggerRegistry()
		{
			this.method = ReflectionHelper.findMethod( CriteriaTriggers.class, "register", "func_192118_a", ICriterionTrigger.class );
			this.method.setAccessible( true );
		}

		@Override
		public void register( ICriterionTrigger<? extends ICriterionInstance> trigger )
		{
			try
			{
				this.method.invoke( null, trigger );
			}
			catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
			{
				AELog.debug( e );
			}
		}

	}

}
