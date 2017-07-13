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

package appeng.util;


import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.stats.Achievement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.definitions.IParts;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAETagCompound;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.capabilities.Capabilities;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.stats.Stats;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.GuiHostType;
import appeng.hooks.TickHandler;
import appeng.integration.Integrations;
import appeng.me.GridAccessException;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.helpers.ItemComparisonHelper;
import appeng.util.helpers.P2PHelper;
import appeng.util.item.AEItemStack;
import appeng.util.item.AESharedNBT;
import appeng.util.prioritylist.IPartitionList;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class Platform
{

	public static final Block AIR_BLOCK = Blocks.AIR;

	public static final int DEF_OFFSET = 16;

	private static final boolean CLIENT_INSTALL = FMLCommonHandler.instance().getSide().isClient();

	/*
	 * random source, use it for item drop locations...
	 */
	private static final Random RANDOM_GENERATOR = new Random();
	private static final WeakHashMap<World, EntityPlayer> FAKE_PLAYERS = new WeakHashMap<World, EntityPlayer>();
	// private static Method getEntry;

	private static final ItemComparisonHelper ITEM_COMPARISON_HELPER = new ItemComparisonHelper();
	private static final P2PHelper P2P_HELPER = new P2PHelper();

	public static ItemComparisonHelper itemComparisons()
	{
		return ITEM_COMPARISON_HELPER;
	}

	public static P2PHelper p2p()
	{
		return P2P_HELPER;
	}

	public static Random getRandom()
	{
		return RANDOM_GENERATOR;
	}

	public static float getRandomFloat()
	{
		return RANDOM_GENERATOR.nextFloat();
	}

	/**
	 * This displays the value for encoded longs ( double *100 )
	 *
	 * @param n to be formatted long value
	 * @param isRate if true it adds a /t to the formatted string
	 *
	 * @return formatted long value
	 */
	public static String formatPowerLong( final long n, final boolean isRate )
	{
		double p = ( (double) n ) / 100;

		final PowerUnits displayUnits = AEConfig.instance().selectedPowerUnit();
		p = PowerUnits.AE.convertTo( displayUnits, p );

		final String[] preFixes = { "k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y" };
		String unitName = displayUnits.name();

		String level = "";
		int offset = 0;
		while( p > 1000 && offset < preFixes.length )
		{
			p /= 1000;
			level = preFixes[offset];
			offset++;
		}

		final DecimalFormat df = new DecimalFormat( "#.##" );
		return df.format( p ) + ' ' + level + unitName + ( isRate ? "/t" : "" );
	}

	public static AEPartLocation crossProduct( final AEPartLocation forward, final AEPartLocation up )
	{
		final int west_x = forward.yOffset * up.zOffset - forward.zOffset * up.yOffset;
		final int west_y = forward.zOffset * up.xOffset - forward.xOffset * up.zOffset;
		final int west_z = forward.xOffset * up.yOffset - forward.yOffset * up.xOffset;

		switch( west_x + west_y * 2 + west_z * 3 )
		{
			case 1:
				return AEPartLocation.EAST;
			case -1:
				return AEPartLocation.WEST;

			case 2:
				return AEPartLocation.UP;
			case -2:
				return AEPartLocation.DOWN;

			case 3:
				return AEPartLocation.SOUTH;
			case -3:
				return AEPartLocation.NORTH;
		}

		return AEPartLocation.INTERNAL;
	}

	public static EnumFacing crossProduct( final EnumFacing forward, final EnumFacing up )
	{
		final int west_x = forward.getFrontOffsetY() * up.getFrontOffsetZ() - forward.getFrontOffsetZ() * up.getFrontOffsetY();
		final int west_y = forward.getFrontOffsetZ() * up.getFrontOffsetX() - forward.getFrontOffsetX() * up.getFrontOffsetZ();
		final int west_z = forward.getFrontOffsetX() * up.getFrontOffsetY() - forward.getFrontOffsetY() * up.getFrontOffsetX();

		switch( west_x + west_y * 2 + west_z * 3 )
		{
			case 1:
				return EnumFacing.EAST;
			case -1:
				return EnumFacing.WEST;

			case 2:
				return EnumFacing.UP;
			case -2:
				return EnumFacing.DOWN;

			case 3:
				return EnumFacing.SOUTH;
			case -3:
				return EnumFacing.NORTH;
		}

		// something is better then nothing?
		return EnumFacing.NORTH;
	}

	public static <T extends Enum> T rotateEnum( T ce, final boolean backwards, final EnumSet validOptions )
	{
		do
		{
			if( backwards )
			{
				ce = prevEnum( ce );
			}
			else
			{
				ce = nextEnum( ce );
			}
		}
		while( !validOptions.contains( ce ) || isNotValidSetting( ce ) );

		return ce;
	}

	/*
	 * Simple way to cycle an enum...
	 */
	private static <T extends Enum> T prevEnum( final T ce )
	{
		final EnumSet valList = EnumSet.allOf( ce.getClass() );

		int pLoc = ce.ordinal() - 1;
		if( pLoc < 0 )
		{
			pLoc = valList.size() - 1;
		}

		if( pLoc < 0 || pLoc >= valList.size() )
		{
			pLoc = 0;
		}

		int pos = 0;
		for( final Object g : valList )
		{
			if( pos == pLoc )
			{
				return (T) g;
			}
			pos++;
		}

		return null;
	}

	/*
	 * Simple way to cycle an enum...
	 */
	public static <T extends Enum> T nextEnum( final T ce )
	{
		final EnumSet valList = EnumSet.allOf( ce.getClass() );

		int pLoc = ce.ordinal() + 1;
		if( pLoc >= valList.size() )
		{
			pLoc = 0;
		}

		if( pLoc < 0 || pLoc >= valList.size() )
		{
			pLoc = 0;
		}

		int pos = 0;
		for( final Object g : valList )
		{
			if( pos == pLoc )
			{
				return (T) g;
			}
			pos++;
		}

		return null;
	}

	private static boolean isNotValidSetting( final Enum e )
	{
		if( e == SortOrder.INVTWEAKS && !Integrations.invTweaks().isEnabled() )
		{
			return true;
		}

		if( e == SearchBoxMode.JEI_AUTOSEARCH && !Integrations.jei().isEnabled() )
		{
			return true;
		}

		if( e == SearchBoxMode.JEI_MANUAL_SEARCH && !Integrations.jei().isEnabled() )
		{
			return true;
		}

		return false;
	}

	public static void openGUI( @Nonnull final EntityPlayer p, @Nullable final TileEntity tile, @Nullable final AEPartLocation side, @Nonnull final GuiBridge type )
	{
		if( isClient() )
		{
			return;
		}

		int x = (int) p.posX;
		int y = (int) p.posY;
		int z = (int) p.posZ;
		if( tile != null )
		{
			x = tile.getPos().getX();
			y = tile.getPos().getY();
			z = tile.getPos().getZ();
		}

		if( ( type.getType().isItem() && tile == null ) || type.hasPermissions( tile, x, y, z, side, p ) )
		{
			if( tile == null && type.getType() == GuiHostType.ITEM )
			{
				p.openGui( AppEng.instance(), type.ordinal() << 4, p.getEntityWorld(), p.inventory.currentItem, 0, 0 );
			}
			else if( tile == null || type.getType() == GuiHostType.ITEM )
			{
				p.openGui( AppEng.instance(), type.ordinal() << 4 | ( 1 << 3 ), p.getEntityWorld(), x, y, z );
			}
			else
			{
				p.openGui( AppEng.instance(), type.ordinal() << 4 | ( side.ordinal() ), tile.getWorld(), x, y, z );
			}
		}
	}

	/*
	 * returns true if the code is on the client.
	 */
	public static boolean isClient()
	{
		return FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

	/*
	 * returns true if client classes are available.
	 */
	public static boolean isClientInstall()
	{
		return CLIENT_INSTALL;
	}

	public static boolean hasPermissions( final DimensionalCoord dc, final EntityPlayer player )
	{
		return dc.getWorld().canMineBlockBody( player, dc.getPos() );
	}

	/*
	 * Checks to see if a block is air?
	 */
	public static boolean isBlockAir( final World w, final BlockPos pos )
	{
		try
		{
			return w.getBlockState( pos ).getBlock().isAir( w.getBlockState( pos ), w, pos );
		}
		catch( final Throwable e )
		{
			return false;
		}
	}

	/*
	 * The usual version of this returns an ItemStack, this version returns the recipe.
	 */
	public static IRecipe findMatchingRecipe( final InventoryCrafting inventoryCrafting, final World par2World )
	{
		final CraftingManager cm = CraftingManager.getInstance();
		final List<IRecipe> rl = cm.getRecipeList();

		for( final IRecipe r : rl )
		{
			if( r.matches( inventoryCrafting, par2World ) )
			{
				return r;
			}
		}

		return null;
	}

	public static ItemStack[] getBlockDrops( final World w, final BlockPos pos )
	{
		List<ItemStack> out = new ArrayList<ItemStack>();
		final IBlockState state = w.getBlockState( pos );

		if( state != null )
		{
			out = state.getBlock().getDrops( w, pos, state, 0 );
		}

		if( out == null )
		{
			return new ItemStack[0];
		}
		return out.toArray( new ItemStack[out.size()] );
	}

	public static AEPartLocation cycleOrientations( final AEPartLocation dir, final boolean upAndDown )
	{
		if( upAndDown )
		{
			switch( dir )
			{
				case NORTH:
					return AEPartLocation.SOUTH;
				case SOUTH:
					return AEPartLocation.EAST;
				case EAST:
					return AEPartLocation.WEST;
				case WEST:
					return AEPartLocation.NORTH;
				case UP:
					return AEPartLocation.UP;
				case DOWN:
					return AEPartLocation.DOWN;
				case INTERNAL:
					return AEPartLocation.INTERNAL;
			}
		}
		else
		{
			switch( dir )
			{
				case UP:
					return AEPartLocation.DOWN;
				case DOWN:
					return AEPartLocation.NORTH;
				case NORTH:
					return AEPartLocation.SOUTH;
				case SOUTH:
					return AEPartLocation.EAST;
				case EAST:
					return AEPartLocation.WEST;
				case WEST:
					return AEPartLocation.UP;
				case INTERNAL:
					return AEPartLocation.INTERNAL;
			}
		}

		return AEPartLocation.INTERNAL;
	}

	/*
	 * Creates / or loads previous NBT Data on items, used for editing items owned by AE.
	 */
	public static NBTTagCompound openNbtData( final ItemStack i )
	{
		NBTTagCompound compound = i.getTagCompound();

		if( compound == null )
		{
			i.setTagCompound( compound = new NBTTagCompound() );
		}

		return compound;
	}

	/*
	 * Generates Item entities in the world similar to how items are generally dropped.
	 */
	public static void spawnDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{
		if( isServer() )
		{
			for( final ItemStack i : drops )
			{
				if( i != null )
				{
					if( i.stackSize > 0 )
					{
						final double offset_x = ( getRandomInt() % 32 - 16 ) / 82;
						final double offset_y = ( getRandomInt() % 32 - 16 ) / 82;
						final double offset_z = ( getRandomInt() % 32 - 16 ) / 82;
						final EntityItem ei = new EntityItem( w, 0.5 + offset_x + pos.getX(), 0.5 + offset_y + pos.getY(), 0.2 + offset_z + pos.getZ(), i
								.copy() );
						w.spawnEntityInWorld( ei );
					}
				}
			}
		}
	}

	/*
	 * returns true if the code is on the server.
	 */
	public static boolean isServer()
	{
		return FMLCommonHandler.instance().getEffectiveSide().isServer();
	}

	public static int getRandomInt()
	{
		return Math.abs( RANDOM_GENERATOR.nextInt() );
	}

	/*
	 * Utility function to get the full inventory for a Double Chest in the World.
	 */
	public static IInventory GetChestInv( final Object te )
	{
		TileEntityChest teA = (TileEntityChest) te;
		TileEntity teB = null;
		final IBlockState myBlockID = teA.getWorld().getBlockState( teA.getPos() );

		final BlockPos posX = teA.getPos().offset( EnumFacing.EAST );
		final BlockPos negX = teA.getPos().offset( EnumFacing.WEST );

		if( teA.getWorld().getBlockState( posX ) == myBlockID )
		{
			teB = teA.getWorld().getTileEntity( posX );
			if( !( teB instanceof TileEntityChest ) )
			{
				teB = null;
			}
		}

		if( teB == null )
		{
			if( teA.getWorld().getBlockState( negX ) == myBlockID )
			{
				teB = teA.getWorld().getTileEntity( negX );
				if( !( teB instanceof TileEntityChest ) )
				{
					teB = null;
				}
				else
				{
					final TileEntityChest x = teA;
					teA = (TileEntityChest) teB;
					teB = x;
				}
			}
		}

		final BlockPos posY = teA.getPos().offset( EnumFacing.SOUTH );
		final BlockPos negY = teA.getPos().offset( EnumFacing.NORTH );

		if( teB == null )
		{
			if( teA.getWorld().getBlockState( posY ) == myBlockID )
			{
				teB = teA.getWorld().getTileEntity( posY );
				if( !( teB instanceof TileEntityChest ) )
				{
					teB = null;
				}
			}
		}

		if( teB == null )
		{
			if( teA.getWorld().getBlockState( negY ) == myBlockID )
			{
				teB = teA.getWorld().getTileEntity( negY );
				if( !( teB instanceof TileEntityChest ) )
				{
					teB = null;
				}
				else
				{
					final TileEntityChest x = teA;
					teA = (TileEntityChest) teB;
					teB = x;
				}
			}
		}

		if( teB == null )
		{
			return teA;
		}

		return new InventoryLargeChest( "", teA, (ILockableContainer) teB );
	}

	public static boolean isModLoaded( final String modid )
	{
		try
		{
			// if this fails for some reason, try the other method.
			return Loader.isModLoaded( modid );
		}
		catch( final Throwable ignored )
		{
		}

		for( final ModContainer f : Loader.instance().getActiveModList() )
		{
			if( f.getModId().equals( modid ) )
			{
				return true;
			}
		}
		return false;
	}

	public static ItemStack findMatchingRecipeOutput( final InventoryCrafting ic, final World worldObj )
	{
		return CraftingManager.getInstance().findMatchingRecipe( ic, worldObj );
	}

	@SideOnly( Side.CLIENT )
	public static List getTooltip( final Object o )
	{
		if( o == null )
		{
			return new ArrayList();
		}

		ItemStack itemStack = null;
		if( o instanceof AEItemStack )
		{
			final AEItemStack ais = (AEItemStack) o;
			return ais.getToolTip();
		}
		else if( o instanceof ItemStack )
		{
			itemStack = (ItemStack) o;
		}
		else
		{
			return new ArrayList();
		}

		try
		{
			return itemStack.getTooltip( Minecraft.getMinecraft().thePlayer, false );
		}
		catch( final Exception errB )
		{
			return new ArrayList();
		}
	}

	public static String getModId( final IAEItemStack is )
	{
		if( is == null )
		{
			return "** Null";
		}

		final String n = ( (AEItemStack) is ).getModID();
		return n == null ? "** Null" : n;
	}

	public static String getItemDisplayName( final Object o )
	{
		if( o == null )
		{
			return "** Null";
		}

		ItemStack itemStack = null;
		if( o instanceof AEItemStack )
		{
			final String n = ( (AEItemStack) o ).getDisplayName();
			return n == null ? "** Null" : n;
		}
		else if( o instanceof ItemStack )
		{
			itemStack = (ItemStack) o;
		}
		else
		{
			return "**Invalid Object";
		}

		try
		{
			String name = itemStack.getDisplayName();
			if( name == null || name.isEmpty() )
			{
				name = itemStack.getItem().getUnlocalizedName( itemStack );
			}
			return name == null ? "** Null" : name;
		}
		catch( final Exception errA )
		{
			try
			{
				final String n = itemStack.getUnlocalizedName();
				return n == null ? "** Null" : n;
			}
			catch( final Exception errB )
			{
				return "** Exception";
			}
		}
	}

	public static boolean hasSpecialComparison( final IAEItemStack willAdd )
	{
		if( willAdd == null )
		{
			return false;
		}
		final IAETagCompound tag = willAdd.getTagCompound();
		if( tag != null && tag.getSpecialComparison() != null )
		{
			return true;
		}
		return false;
	}

	public static boolean hasSpecialComparison( final ItemStack willAdd )
	{
		if( AESharedNBT.isShared( willAdd.getTagCompound() ) )
		{
			if( ( (IAETagCompound) willAdd.getTagCompound() ).getSpecialComparison() != null )
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isWrench( final EntityPlayer player, final ItemStack eq, final BlockPos pos )
	{
		if( eq != null )
		{
			try
			{
				// TODO: Build Craft Wrench?
				/*
				 * if( eq.getItem() instanceof IToolWrench )
				 * {
				 * IToolWrench wrench = (IToolWrench) eq.getItem();
				 * return wrench.canWrench( player, x, y, z );
				 * }
				 */
			}
			catch( final Throwable ignore )
			{ // explodes without BC

			}

			if( eq.getItem() instanceof IAEWrench )
			{
				final IAEWrench wrench = (IAEWrench) eq.getItem();
				return wrench.canWrench( eq, player, pos );
			}
		}
		return false;
	}

	public static boolean isChargeable( final ItemStack i )
	{
		if( i == null )
		{
			return false;
		}
		final Item it = i.getItem();
		if( it instanceof IAEItemPowerStorage )
		{
			return ( (IAEItemPowerStorage) it ).getPowerFlow( i ) != AccessRestriction.READ;
		}
		return false;
	}

	public static EntityPlayer getPlayer( final WorldServer w )
	{
		if( w == null )
		{
			throw new InvalidParameterException( "World is null." );
		}

		final EntityPlayer wrp = FAKE_PLAYERS.get( w );
		if( wrp != null )
		{
			return wrp;
		}

		final EntityPlayer p = FakePlayerFactory.getMinecraft( w );
		FAKE_PLAYERS.put( w, p );
		return p;
	}

	public static int MC2MEColor( final int color )
	{
		switch( color )
		{
			case 4: // "blue"
				return 0;
			case 0: // "black"
				return 1;
			case 15: // "white"
				return 2;
			case 3: // "brown"
				return 3;
			case 1: // "red"
				return 4;
			case 11: // "yellow"
				return 5;
			case 2: // "green"
				return 6;

			case 5: // "purple"
			case 6: // "cyan"
			case 7: // "silver"
			case 8: // "gray"
			case 9: // "pink"
			case 10: // "lime"
			case 12: // "lightBlue"
			case 13: // "magenta"
			case 14: // "orange"
		}
		return -1;
	}

	public static int findEmpty( final RegistryNamespaced registry, final int minId, final int maxId )
	{
		for( int x = minId; x < maxId; x++ )
		{
			if( registry.getObjectById( x ) == null )
			{
				return x;
			}
		}
		return -1;
	}

	public static int findEmpty( final Object[] l )
	{
		for( int x = 0; x < l.length; x++ )
		{
			if( l[x] == null )
			{
				return x;
			}
		}
		return -1;
	}

	/**
	 * Returns a random element from the given collection.
	 * 
	 * @return null if the collection is empty
	 */
	@Nullable
	public static <T> T pickRandom( final Collection<T> outs )
	{
		if( outs.isEmpty() )
		{
			return null;
		}

		int index = RANDOM_GENERATOR.nextInt( outs.size() );
		return Iterables.get( outs, index, null );
	}

	public static AEPartLocation rotateAround( final AEPartLocation forward, final AEPartLocation axis )
	{
		if( axis == AEPartLocation.INTERNAL || forward == AEPartLocation.INTERNAL )
		{
			return forward;
		}

		switch( forward )
		{
			case DOWN:
				switch( axis )
				{
					case DOWN:
						return forward;
					case UP:
						return forward;
					case NORTH:
						return AEPartLocation.EAST;
					case SOUTH:
						return AEPartLocation.WEST;
					case EAST:
						return AEPartLocation.NORTH;
					case WEST:
						return AEPartLocation.SOUTH;
					default:
						break;
				}
				break;
			case UP:
				switch( axis )
				{
					case NORTH:
						return AEPartLocation.WEST;
					case SOUTH:
						return AEPartLocation.EAST;
					case EAST:
						return AEPartLocation.SOUTH;
					case WEST:
						return AEPartLocation.NORTH;
					default:
						break;
				}
				break;
			case NORTH:
				switch( axis )
				{
					case UP:
						return AEPartLocation.WEST;
					case DOWN:
						return AEPartLocation.EAST;
					case EAST:
						return AEPartLocation.UP;
					case WEST:
						return AEPartLocation.DOWN;
					default:
						break;
				}
				break;
			case SOUTH:
				switch( axis )
				{
					case UP:
						return AEPartLocation.EAST;
					case DOWN:
						return AEPartLocation.WEST;
					case EAST:
						return AEPartLocation.DOWN;
					case WEST:
						return AEPartLocation.UP;
					default:
						break;
				}
				break;
			case EAST:
				switch( axis )
				{
					case UP:
						return AEPartLocation.NORTH;
					case DOWN:
						return AEPartLocation.SOUTH;
					case NORTH:
						return AEPartLocation.UP;
					case SOUTH:
						return AEPartLocation.DOWN;
					default:
						break;
				}
			case WEST:
				switch( axis )
				{
					case UP:
						return AEPartLocation.SOUTH;
					case DOWN:
						return AEPartLocation.NORTH;
					case NORTH:
						return AEPartLocation.DOWN;
					case SOUTH:
						return AEPartLocation.UP;
					default:
						break;
				}
			default:
				break;
		}
		return forward;
	}

	public static EnumFacing rotateAround( final EnumFacing forward, final EnumFacing axis )
	{
		switch( forward )
		{
			case DOWN:
				switch( axis )
				{
					case DOWN:
						return forward;
					case UP:
						return forward;
					case NORTH:
						return EnumFacing.EAST;
					case SOUTH:
						return EnumFacing.WEST;
					case EAST:
						return EnumFacing.NORTH;
					case WEST:
						return EnumFacing.SOUTH;
					default:
						break;
				}
				break;
			case UP:
				switch( axis )
				{
					case NORTH:
						return EnumFacing.WEST;
					case SOUTH:
						return EnumFacing.EAST;
					case EAST:
						return EnumFacing.SOUTH;
					case WEST:
						return EnumFacing.NORTH;
					default:
						break;
				}
				break;
			case NORTH:
				switch( axis )
				{
					case UP:
						return EnumFacing.WEST;
					case DOWN:
						return EnumFacing.EAST;
					case EAST:
						return EnumFacing.UP;
					case WEST:
						return EnumFacing.DOWN;
					default:
						break;
				}
				break;
			case SOUTH:
				switch( axis )
				{
					case UP:
						return EnumFacing.EAST;
					case DOWN:
						return EnumFacing.WEST;
					case EAST:
						return EnumFacing.DOWN;
					case WEST:
						return EnumFacing.UP;
					default:
						break;
				}
				break;
			case EAST:
				switch( axis )
				{
					case UP:
						return EnumFacing.NORTH;
					case DOWN:
						return EnumFacing.SOUTH;
					case NORTH:
						return EnumFacing.UP;
					case SOUTH:
						return EnumFacing.DOWN;
					default:
						break;
				}
			case WEST:
				switch( axis )
				{
					case UP:
						return EnumFacing.SOUTH;
					case DOWN:
						return EnumFacing.NORTH;
					case NORTH:
						return EnumFacing.DOWN;
					case SOUTH:
						return EnumFacing.UP;
					default:
						break;
				}
			default:
				break;
		}
		return forward;
	}

	@SideOnly( Side.CLIENT )
	public static String gui_localize( final String string )
	{
		return I18n.translateToLocal( string );
	}

	public static LookDirection getPlayerRay( final EntityPlayer playerIn, final float eyeOffset )
	{
		double reachDistance = 5.0d;

		final double x = playerIn.prevPosX + ( playerIn.posX - playerIn.prevPosX );
		final double y = playerIn.prevPosY + ( playerIn.posY - playerIn.prevPosY ) + playerIn.getEyeHeight();
		final double z = playerIn.prevPosZ + ( playerIn.posZ - playerIn.prevPosZ );

		final float playerPitch = playerIn.prevRotationPitch + ( playerIn.rotationPitch - playerIn.prevRotationPitch );
		final float playerYaw = playerIn.prevRotationYaw + ( playerIn.rotationYaw - playerIn.prevRotationYaw );

		final float yawRayX = MathHelper.sin( -playerYaw * 0.017453292f - (float) Math.PI );
		final float yawRayZ = MathHelper.cos( -playerYaw * 0.017453292f - (float) Math.PI );

		final float pitchMultiplier = -MathHelper.cos( -playerPitch * 0.017453292F );
		final float eyeRayY = MathHelper.sin( -playerPitch * 0.017453292F );
		final float eyeRayX = yawRayX * pitchMultiplier;
		final float eyeRayZ = yawRayZ * pitchMultiplier;

		if( playerIn instanceof EntityPlayerMP )
		{
			reachDistance = ( (EntityPlayerMP) playerIn ).interactionManager.getBlockReachDistance();
		}

		final Vec3d from = new Vec3d( x, y, z );
		final Vec3d to = from.addVector( eyeRayX * reachDistance, eyeRayY * reachDistance, eyeRayZ * reachDistance );

		return new LookDirection( from, to );
	}

	public static RayTraceResult rayTrace( final EntityPlayer p, final boolean hitBlocks, final boolean hitEntities )
	{
		final World w = p.getEntityWorld();

		final float f = 1.0F;
		float f1 = p.prevRotationPitch + ( p.rotationPitch - p.prevRotationPitch ) * f;
		final float f2 = p.prevRotationYaw + ( p.rotationYaw - p.prevRotationYaw ) * f;
		final double d0 = p.prevPosX + ( p.posX - p.prevPosX ) * f;
		final double d1 = p.prevPosY + ( p.posY - p.prevPosY ) * f + 1.62D - p.getYOffset();
		final double d2 = p.prevPosZ + ( p.posZ - p.prevPosZ ) * f;
		final Vec3d vec3 = new Vec3d( d0, d1, d2 );
		final float f3 = MathHelper.cos( -f2 * 0.017453292F - (float) Math.PI );
		final float f4 = MathHelper.sin( -f2 * 0.017453292F - (float) Math.PI );
		final float f5 = -MathHelper.cos( -f1 * 0.017453292F );
		final float f6 = MathHelper.sin( -f1 * 0.017453292F );
		final float f7 = f4 * f5;
		final float f8 = f3 * f5;
		final double d3 = 32.0D;

		final Vec3d vec31 = vec3.addVector( f7 * d3, f6 * d3, f8 * d3 );

		final AxisAlignedBB bb = new AxisAlignedBB( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ), Math.min( vec3.zCoord,
				vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ), Math.max( vec3.yCoord, vec31.yCoord ), Math.max( vec3.zCoord, vec31.zCoord ) ).expand(
						16, 16, 16 );

		Entity entity = null;
		double closest = 9999999.0D;
		if( hitEntities )
		{
			final List list = w.getEntitiesWithinAABBExcludingEntity( p, bb );

			for( int l = 0; l < list.size(); ++l )
			{
				final Entity entity1 = (Entity) list.get( l );

				if( !entity1.isDead && entity1 != p && !( entity1 instanceof EntityItem ) )
				{
					if( entity1.isEntityAlive() )
					{
						// prevent killing / flying of mounts.
						if( entity1.isRidingOrBeingRiddenBy( p ) )
						{
							continue;
						}

						f1 = 0.3F;
						final AxisAlignedBB boundingBox = entity1.getEntityBoundingBox().expand( f1, f1, f1 );
						final RayTraceResult RayTraceResult = boundingBox.calculateIntercept( vec3, vec31 );

						if( RayTraceResult != null )
						{
							final double nd = vec3.squareDistanceTo( RayTraceResult.hitVec );

							if( nd < closest )
							{
								entity = entity1;
								closest = nd;
							}
						}
					}
				}
			}
		}

		RayTraceResult pos = null;
		Vec3d vec = null;

		if( hitBlocks )
		{
			vec = new Vec3d( d0, d1, d2 );
			pos = w.rayTraceBlocks( vec3, vec31, true );
		}

		if( entity != null && pos != null && pos.hitVec.squareDistanceTo( vec ) > closest )
		{
			pos = new RayTraceResult( entity );
		}
		else if( entity != null && pos == null )
		{
			pos = new RayTraceResult( entity );
		}

		return pos;
	}

	public static long nanoTime()
	{
		// if ( Configuration.INSTANCE.enableNetworkProfiler )
		// return System.nanoTime();
		return 0;
	}

	public static <StackType extends IAEStack> StackType poweredExtraction( final IEnergySource energy, final IMEInventory<StackType> cell, final StackType request, final BaseActionSource src )
	{
		final StackType possible = cell.extractItems( (StackType) request.copy(), Actionable.SIMULATE, src );

		long retrieved = 0;
		if( possible != null )
		{
			retrieved = possible.getStackSize();
		}

		final double availablePower = energy.extractAEPower( retrieved, Actionable.SIMULATE, PowerMultiplier.CONFIG );

		final long itemToExtract = Math.min( (long) ( availablePower + 0.9 ), retrieved );

		if( itemToExtract > 0 )
		{
			energy.extractAEPower( retrieved, Actionable.MODULATE, PowerMultiplier.CONFIG );

			possible.setStackSize( itemToExtract );
			final StackType ret = cell.extractItems( possible, Actionable.MODULATE, src );

			if( ret != null && src.isPlayer() )
			{
				Stats.ItemsExtracted.addToPlayer( ( (PlayerSource) src ).player, (int) ret.getStackSize() );
			}

			return ret;
		}

		return null;
	}

	public static <StackType extends IAEStack> StackType poweredInsert( final IEnergySource energy, final IMEInventory<StackType> cell, final StackType input, final BaseActionSource src )
	{
		final StackType possible = cell.injectItems( (StackType) input.copy(), Actionable.SIMULATE, src );

		long stored = input.getStackSize();
		if( possible != null )
		{
			stored -= possible.getStackSize();
		}

		final double availablePower = energy.extractAEPower( stored, Actionable.SIMULATE, PowerMultiplier.CONFIG );

		final long itemToAdd = Math.min( (long) ( availablePower + 0.9 ), stored );

		if( itemToAdd > 0 )
		{
			energy.extractAEPower( stored, Actionable.MODULATE, PowerMultiplier.CONFIG );

			if( itemToAdd < input.getStackSize() )
			{
				final long original = input.getStackSize();
				final StackType split = (StackType) input.copy();
				split.decStackSize( itemToAdd );
				input.setStackSize( itemToAdd );
				split.add( cell.injectItems( input, Actionable.MODULATE, src ) );

				if( src.isPlayer() )
				{
					final long diff = original - split.getStackSize();
					Stats.ItemsInserted.addToPlayer( ( (PlayerSource) src ).player, (int) diff );
				}

				return split;
			}

			final StackType ret = cell.injectItems( input, Actionable.MODULATE, src );

			if( src.isPlayer() )
			{
				final long diff = ret == null ? input.getStackSize() : input.getStackSize() - ret.getStackSize();
				Stats.ItemsInserted.addToPlayer( ( (PlayerSource) src ).player, (int) diff );
			}

			return ret;
		}

		return input;
	}

	public static void postChanges( final IStorageGrid gs, final ItemStack removed, final ItemStack added, final BaseActionSource src )
	{
		final IItemList<IAEItemStack> itemChanges = AEApi.instance().storage().createItemList();
		final IItemList<IAEFluidStack> fluidChanges = AEApi.instance().storage().createFluidList();

		if( removed != null )
		{
			final IMEInventory<IAEItemStack> myItems = AEApi.instance().registries().cell().getCellInventory( removed, null, StorageChannel.ITEMS );

			if( myItems != null )
			{
				for( final IAEItemStack is : myItems.getAvailableItems( itemChanges ) )
				{
					is.setStackSize( -is.getStackSize() );
				}
			}

			final IMEInventory<IAEFluidStack> myFluids = AEApi.instance().registries().cell().getCellInventory( removed, null, StorageChannel.FLUIDS );

			if( myFluids != null )
			{
				for( final IAEFluidStack is : myFluids.getAvailableItems( fluidChanges ) )
				{
					is.setStackSize( -is.getStackSize() );
				}
			}
		}

		if( added != null )
		{
			final IMEInventory<IAEItemStack> myItems = AEApi.instance().registries().cell().getCellInventory( added, null, StorageChannel.ITEMS );

			if( myItems != null )
			{
				myItems.getAvailableItems( itemChanges );
			}

			final IMEInventory<IAEFluidStack> myFluids = AEApi.instance().registries().cell().getCellInventory( added, null, StorageChannel.FLUIDS );

			if( myFluids != null )
			{
				myFluids.getAvailableItems( fluidChanges );
			}
		}

		gs.postAlterationOfStoredItems( StorageChannel.ITEMS, itemChanges, src );
	}

	public static <T extends IAEStack<T>> void postListChanges( final IItemList<T> before, final IItemList<T> after, final IMEMonitorHandlerReceiver<T> meMonitorPassthrough, final BaseActionSource source )
	{
		final LinkedList<T> changes = new LinkedList<T>();

		for( final T is : before )
		{
			is.setStackSize( -is.getStackSize() );
		}

		for( final T is : after )
		{
			before.add( is );
		}

		for( final T is : before )
		{
			if( is.getStackSize() != 0 )
			{
				changes.add( is );
			}
		}

		if( !changes.isEmpty() )
		{
			meMonitorPassthrough.postChange( null, changes, source );
		}
	}

	public static int generateTileHash( final TileEntity target )
	{
		if( target == null )
		{
			return 0;
		}

		int hash = target.hashCode();

		if( target.hasCapability( Capabilities.STORAGE_MONITORABLE_ACCESSOR, null ) )
		{
			return 0;
		}
		else if( target instanceof TileEntityChest )
		{
			final TileEntityChest chest = (TileEntityChest) target;
			chest.checkForAdjacentChests();
			if( chest.adjacentChestZNeg != null )
			{
				hash ^= chest.adjacentChestZNeg.hashCode();
			}
			else if( chest.adjacentChestZPos != null )
			{
				hash ^= chest.adjacentChestZPos.hashCode();
			}
			else if( chest.adjacentChestXPos != null )
			{
				hash ^= chest.adjacentChestXPos.hashCode();
			}
			else if( chest.adjacentChestXNeg != null )
			{
				hash ^= chest.adjacentChestXNeg.hashCode();
			}
		}
		else if( target instanceof IInventory )
		{
			hash ^= ( (IInventory) target ).getSizeInventory();

			if( target instanceof ISidedInventory )
			{
				for( final EnumFacing dir : EnumFacing.VALUES )
				{

					final int[] sides = ( (ISidedInventory) target ).getSlotsForFace( dir );

					if( sides == null )
					{
						return 0;
					}

					int offset = 0;
					for( final int side : sides )
					{
						final int c = ( side << ( offset % 8 ) ) ^ ( 1 << dir.ordinal() );
						offset++;
						hash = c + ( hash << 6 ) + ( hash << 16 ) - hash;
					}
				}
			}
		}

		return hash;
	}

	public static boolean securityCheck( final GridNode a, final GridNode b )
	{
		if( a.getLastSecurityKey() == -1 && b.getLastSecurityKey() == -1 )
		{
			return false;
		}
		else if( a.getLastSecurityKey() == b.getLastSecurityKey() )
		{
			return false;
		}

		final boolean a_isSecure = isPowered( a.getGrid() ) && a.getLastSecurityKey() != -1;
		final boolean b_isSecure = isPowered( b.getGrid() ) && b.getLastSecurityKey() != -1;

		if( AEConfig.instance().isFeatureEnabled( AEFeature.LOG_SECURITY_AUDITS ) )
		{
			AELog.info(
					"Audit: " + a_isSecure + " : " + b_isSecure + " @ " + a.getLastSecurityKey() + " vs " + b.getLastSecurityKey() + " & " + a
							.getPlayerID() + " vs " + b.getPlayerID() );
		}

		// can't do that son...
		if( a_isSecure && b_isSecure )
		{
			return true;
		}

		if( !a_isSecure && b_isSecure )
		{
			return checkPlayerPermissions( b.getGrid(), a.getPlayerID() );
		}

		if( a_isSecure && !b_isSecure )
		{
			return checkPlayerPermissions( a.getGrid(), b.getPlayerID() );
		}

		return false;
	}

	private static boolean isPowered( final IGrid grid )
	{
		if( grid == null )
		{
			return false;
		}

		final IEnergyGrid eg = grid.getCache( IEnergyGrid.class );
		return eg.isNetworkPowered();
	}

	private static boolean checkPlayerPermissions( final IGrid grid, final int playerID )
	{
		if( grid == null )
		{
			return false;
		}

		final ISecurityGrid gs = grid.getCache( ISecurityGrid.class );

		if( gs == null )
		{
			return false;
		}

		if( !gs.isAvailable() )
		{
			return false;
		}

		return !gs.hasPermission( playerID, SecurityPermissions.BUILD );
	}

	public static void configurePlayer( final EntityPlayer player, final AEPartLocation side, final TileEntity tile )
	{
		float pitch = 0.0f;
		float yaw = 0.0f;
		// player.yOffset = 1.8f;

		switch( side )
		{
			case DOWN:
				pitch = 90.0f;
				// player.getYOffset() = -1.8f;
				break;
			case EAST:
				yaw = -90.0f;
				break;
			case NORTH:
				yaw = 180.0f;
				break;
			case SOUTH:
				yaw = 0.0f;
				break;
			case INTERNAL:
				break;
			case UP:
				pitch = 90.0f;
				break;
			case WEST:
				yaw = 90.0f;
				break;
		}

		player.posX = tile.getPos().getX() + 0.5;
		player.posY = tile.getPos().getY() + 0.5;
		player.posZ = tile.getPos().getZ() + 0.5;

		player.rotationPitch = player.prevCameraPitch = player.cameraPitch = pitch;
		player.rotationYaw = player.prevCameraYaw = player.cameraYaw = yaw;
	}

	public static boolean canAccess( final AENetworkProxy gridProxy, final BaseActionSource src )
	{
		try
		{
			if( src.isPlayer() )
			{
				return gridProxy.getSecurity().hasPermission( ( (PlayerSource) src ).player, SecurityPermissions.BUILD );
			}
			else if( src.isMachine() )
			{
				final IActionHost te = ( (MachineSource) src ).via;
				final IGridNode n = te.getActionableNode();
				if( n == null )
				{
					return false;
				}

				final int playerID = n.getPlayerID();
				return gridProxy.getSecurity().hasPermission( playerID, SecurityPermissions.BUILD );
			}
			else
			{
				return false;
			}
		}
		catch( final GridAccessException gae )
		{
			return false;
		}
	}

	public static ItemStack extractItemsByRecipe( final IEnergySource energySrc, final BaseActionSource mySrc, final IMEMonitor<IAEItemStack> src, final World w, final IRecipe r, final ItemStack output, final InventoryCrafting ci, final ItemStack providedTemplate, final int slot, final IItemList<IAEItemStack> items, final Actionable realForFake, final IPartitionList<IAEItemStack> filter )
	{
		if( energySrc.extractAEPower( 1, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > 0.9 )
		{
			if( providedTemplate == null )
			{
				return null;
			}

			final AEItemStack ae_req = AEItemStack.create( providedTemplate );
			ae_req.setStackSize( 1 );

			if( filter == null || filter.isListed( ae_req ) )
			{
				final IAEItemStack ae_ext = src.extractItems( ae_req, realForFake, mySrc );
				if( ae_ext != null )
				{
					final ItemStack extracted = ae_ext.getItemStack();
					if( extracted != null )
					{
						energySrc.extractAEPower( 1, realForFake, PowerMultiplier.CONFIG );
						return extracted;
					}
				}
			}

			final boolean checkFuzzy = ae_req.isOre() || providedTemplate.getItemDamage() == OreDictionary.WILDCARD_VALUE || providedTemplate
					.hasTagCompound() || providedTemplate.isItemStackDamageable();

			if( items != null && checkFuzzy )
			{
				for( final IAEItemStack x : items )
				{
					final ItemStack sh = x.getItemStack();
					if( ( Platform.itemComparisons().isEqualItemType( providedTemplate,
							sh ) || ae_req.sameOre( x ) ) && !Platform.itemComparisons().isEqualItem( sh, output ) )
					{ // Platform.isSameItemType( sh, providedTemplate )
						final ItemStack cp = Platform.cloneItemStack( sh );
						cp.stackSize = 1;
						ci.setInventorySlotContents( slot, cp );
						if( r.matches( ci, w ) && Platform.itemComparisons().isEqualItem( r.getCraftingResult( ci ), output ) )
						{
							final IAEItemStack ax = x.copy();
							ax.setStackSize( 1 );
							if( filter == null || filter.isListed( ax ) )
							{
								final IAEItemStack ex = src.extractItems( ax, realForFake, mySrc );
								if( ex != null )
								{
									energySrc.extractAEPower( 1, realForFake, PowerMultiplier.CONFIG );
									return ex.getItemStack();
								}
							}
						}
						ci.setInventorySlotContents( slot, providedTemplate );
					}
				}
			}
		}
		return null;
	}

	public static ItemStack cloneItemStack( final ItemStack a )
	{
		return a.copy();
	}

	public static ItemStack getContainerItem( final ItemStack stackInSlot )
	{
		if( stackInSlot == null )
		{
			return null;
		}

		final Item i = stackInSlot.getItem();
		if( i == null || !i.hasContainerItem( stackInSlot ) )
		{
			if( stackInSlot.stackSize > 1 )
			{
				stackInSlot.stackSize--;
				return stackInSlot;
			}
			return null;
		}

		ItemStack ci = i.getContainerItem( stackInSlot.copy() );
		if( ci != null && ci.isItemStackDamageable() && ci.getItemDamage() == ci.getMaxDamage() )
		{
			ci = null;
		}

		return ci;
	}

	public static void notifyBlocksOfNeighbors( final World worldObj, final BlockPos pos )
	{
		if( !worldObj.isRemote )
		{
			TickHandler.INSTANCE.addCallable( worldObj, new BlockUpdate( pos ) );
		}
	}

	public static boolean canRepair( final AEFeature type, final ItemStack a, final ItemStack b )
	{
		if( b == null || a == null )
		{
			return false;
		}

		if( type == AEFeature.CERTUS_QUARTZ_TOOLS )
		{
			final IItemDefinition certusQuartzCrystal = AEApi.instance().definitions().materials().certusQuartzCrystal();

			return certusQuartzCrystal.isSameAs( b );
		}

		if( type == AEFeature.NETHER_QUARTZ_TOOLS )
		{
			return Items.QUARTZ == b.getItem();
		}

		return false;
	}

	public static List<ItemStack> findPreferred( final ItemStack[] is )
	{
		final IParts parts = AEApi.instance().definitions().parts();

		for( final ItemStack stack : is )
		{
			if( parts.cableGlass().sameAs( AEColor.TRANSPARENT, stack ) )
			{
				return Collections.singletonList( stack );
			}

			if( parts.cableCovered().sameAs( AEColor.TRANSPARENT, stack ) )
			{
				return Collections.singletonList( stack );
			}

			if( parts.cableSmart().sameAs( AEColor.TRANSPARENT, stack ) )
			{
				return Collections.singletonList( stack );
			}

			if( parts.cableDense().sameAs( AEColor.TRANSPARENT, stack ) )
			{
				return Collections.singletonList( stack );
			}
		}

		return Lists.newArrayList( is );
	}

	public static void sendChunk( final Chunk c, final int verticalBits )
	{
		try
		{
			final WorldServer ws = (WorldServer) c.getWorld();
			final PlayerChunkMap pm = ws.getPlayerChunkMap();
			final PlayerChunkMapEntry playerInstance = pm.getEntry( c.xPosition, c.zPosition );

			if( playerInstance != null )
			{
				playerInstance.sendPacket( new SPacketChunkData( c, verticalBits ) );
			}
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}
	}

	public static float getEyeOffset( final EntityPlayer player )
	{
		assert player.worldObj.isRemote : "Valid only on client";
		return (float) ( player.posY + player.getEyeHeight() - player.getDefaultEyeHeight() );
	}

	public static void addStat( final int playerID, final Achievement achievement )
	{
		final EntityPlayer p = AEApi.instance().registries().players().findPlayer( playerID );
		if( p != null )
		{
			p.addStat( achievement, 1 );
		}
	}

	public static boolean isRecipePrioritized( final ItemStack what )
	{
		final IMaterials materials = AEApi.instance().definitions().materials();

		boolean isPurified = materials.purifiedCertusQuartzCrystal().isSameAs( what );
		isPurified |= materials.purifiedFluixCrystal().isSameAs( what );
		isPurified |= materials.purifiedNetherQuartzCrystal().isSameAs( what );

		return isPurified;
	}
}
