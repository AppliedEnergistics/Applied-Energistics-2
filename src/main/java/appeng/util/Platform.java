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


import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.definitions.IParts;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.*;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.*;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.stats.Stats;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.GuiHostType;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.me.GridAccessException;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.item.AEItemStack;
import appeng.util.item.AESharedNBT;
import appeng.util.item.OreHelper;
import appeng.util.item.OreReference;
import appeng.util.prioitylist.IPartitionList;
import buildcraft.api.tools.IToolWrench;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.stats.Achievement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.*;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class Platform
{

	public static final Block AIR_BLOCK = Blocks.air;

	public static final int DEF_OFFSET = 16;

	/*
	 * random source, use it for item drop locations...
	 */
	private static final Random RANDOM_GENERATOR = new Random();
	private static final WeakHashMap<World, EntityPlayer> FAKE_PLAYERS = new WeakHashMap<World, EntityPlayer>();
	private static Field tagList;
	private static Class playerInstance;
	private static Method getOrCreateChunkWatcher;
	private static Method sendToAllPlayersWatchingChunk;
	private static GameProfile fakeProfile = new GameProfile( UUID.fromString( "839eb18c-50bc-400c-8291-9383f09763e7" ), "[AE2Player]" );

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
	 * @param n      to be formatted long value
	 * @param isRate if true it adds a /t to the formatted string
	 * @return formatted long value
	 */
	public static String formatPowerLong( final long n, final boolean isRate )
	{
		double p = ( (double) n ) / 100;

		final PowerUnits displayUnits = AEConfig.instance.selectedPowerUnit();
		p = PowerUnits.AE.convertTo( displayUnits, p );

		String unitName = displayUnits.name();

		if( displayUnits == PowerUnits.WA )
		{
			unitName = "J";
		}

		if( displayUnits == PowerUnits.MK )
		{
			unitName = "J";
		}

		final String[] preFixes = {
				"k",
				"M",
				"G",
				"T",
				"P",
				"T",
				"P",
				"E",
				"Z",
				"Y"
		};
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

	public static ForgeDirection crossProduct( final ForgeDirection forward, final ForgeDirection up )
	{
		final int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		final int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		final int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

		switch( west_x + west_y * 2 + west_z * 3 )
		{
			case 1:
				return ForgeDirection.EAST;
			case -1:
				return ForgeDirection.WEST;

			case 2:
				return ForgeDirection.UP;
			case -2:
				return ForgeDirection.DOWN;

			case 3:
				return ForgeDirection.SOUTH;
			case -3:
				return ForgeDirection.NORTH;
		}

		return ForgeDirection.UNKNOWN;
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
		if( e == SortOrder.INVTWEAKS && !IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.InvTweaks ) )
		{
			return true;
		}

		if( e == SearchBoxMode.NEI_AUTOSEARCH && !IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.NEI ) )
		{
			return true;
		}

		return e == SearchBoxMode.NEI_MANUAL_SEARCH && !IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.NEI );

	}

	public static void openGUI( @Nonnull final EntityPlayer p, @Nullable final TileEntity tile, @Nullable final ForgeDirection side, @Nonnull final GuiBridge type )
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
			x = tile.xCoord;
			y = tile.yCoord;
			z = tile.zCoord;
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
				p.openGui( AppEng.instance(), type.ordinal() << 4 | ( side.ordinal() ), tile.getWorldObj(), x, y, z );
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

	public static boolean hasPermissions( final DimensionalCoord dc, final EntityPlayer player )
	{
		return dc.getWorld().canMineBlock( player, dc.x, dc.y, dc.z );
	}

	/*
	 * Checks to see if a block is air?
	 */
	public static boolean isBlockAir( final World w, final int x, final int y, final int z )
	{
		try
		{
			return w.getBlock( x, y, z ).isAir( w, x, y, z );
		}
		catch( final Throwable e )
		{
			return false;
		}
	}

	/*
	 * Lots of silliness to try and account for weird tag related junk, basically requires that two tags have at least
	 * something in their tags before it wasts its time comparing them.
	 */
	private static boolean sameStackStags( final ItemStack a, final ItemStack b )
	{
		if( a == null && b == null )
		{
			return true;
		}
		if( a == null || b == null )
		{
			return false;
		}
		if( a == b )
		{
			return true;
		}

		final NBTTagCompound ta = a.getTagCompound();
		final NBTTagCompound tb = b.getTagCompound();
		if( ta == tb )
		{
			return true;
		}

		if( ( ta == null && tb == null ) || ( ta != null && ta.hasNoTags() && tb == null ) || ( tb != null && tb.hasNoTags() && ta == null ) || ( ta != null && ta.hasNoTags() && tb != null && tb.hasNoTags() ) )
		{
			return true;
		}

		if( ( ta == null && tb != null ) || ( ta != null && tb == null ) )
		{
			return false;
		}

		// if both tags are shared this is easy...
		if( AESharedNBT.isShared( ta ) && AESharedNBT.isShared( tb ) )
		{
			return ta == tb;
		}

		return NBTEqualityTest( ta, tb );
	}

	/*
	 * recursive test for NBT Equality, this was faster then trying to compare / generate hashes, its also more reliable
	 * then the vanilla version which likes to fail when NBT Compound data changes order, it is pretty expensive
	 * performance wise, so try an use shared tag compounds as long as the system remains in AE.
	 */
	public static boolean NBTEqualityTest( final NBTBase left, final NBTBase right )
	{
		// same type?
		final byte id = left.getId();
		if( id == right.getId() )
		{
			switch( id )
			{
				case 10:
				{
					final NBTTagCompound ctA = (NBTTagCompound) left;
					final NBTTagCompound ctB = (NBTTagCompound) right;

					final Set<String> cA = ctA.func_150296_c();
					final Set<String> cB = ctB.func_150296_c();

					if( cA.size() != cB.size() )
					{
						return false;
					}

					for( final String name : cA )
					{
						final NBTBase tag = ctA.getTag( name );
						final NBTBase aTag = ctB.getTag( name );
						if( aTag == null )
						{
							return false;
						}

						if( !NBTEqualityTest( tag, aTag ) )
						{
							return false;
						}
					}

					return true;
				}

				case 9: // ) // A instanceof NBTTagList )
				{
					final NBTTagList lA = (NBTTagList) left;
					final NBTTagList lB = (NBTTagList) right;
					if( lA.tagCount() != lB.tagCount() )
					{
						return false;
					}

					final List<NBTBase> tag = tagList( lA );
					final List<NBTBase> aTag = tagList( lB );
					if( tag.size() != aTag.size() )
					{
						return false;
					}

					for( int x = 0; x < tag.size(); x++ )
					{
						if( aTag.get( x ) == null )
						{
							return false;
						}

						if( !NBTEqualityTest( tag.get( x ), aTag.get( x ) ) )
						{
							return false;
						}
					}

					return true;
				}

				case 1: // ( A instanceof NBTTagByte )
					return ( (NBTBase.NBTPrimitive) left ).func_150287_d() == ( (NBTBase.NBTPrimitive) right ).func_150287_d();

				case 4: // else if ( A instanceof NBTTagLong )
					return ( (NBTBase.NBTPrimitive) left ).func_150291_c() == ( (NBTBase.NBTPrimitive) right ).func_150291_c();

				case 8: // else if ( A instanceof NBTTagString )
					return ( (NBTTagString) left ).func_150285_a_().equals( ( (NBTTagString) right ).func_150285_a_() ) || ( (NBTTagString) left ).func_150285_a_().equals( ( (NBTTagString) right ).func_150285_a_() );

				case 6: // else if ( A instanceof NBTTagDouble )
					return ( (NBTBase.NBTPrimitive) left ).func_150286_g() == ( (NBTBase.NBTPrimitive) right ).func_150286_g();

				case 5: // else if ( A instanceof NBTTagFloat )
					return ( (NBTBase.NBTPrimitive) left ).func_150288_h() == ( (NBTBase.NBTPrimitive) right ).func_150288_h();

				case 3: // else if ( A instanceof NBTTagInt )
					return ( (NBTBase.NBTPrimitive) left ).func_150287_d() == ( (NBTBase.NBTPrimitive) right ).func_150287_d();

				default:
					return left.equals( right );
			}
		}

		return false;
	}

	private static List<NBTBase> tagList( final NBTTagList lB )
	{
		if( tagList == null )
		{
			try
			{
				tagList = lB.getClass().getDeclaredField( "tagList" );
			}
			catch( final Throwable t )
			{
				try
				{
					tagList = lB.getClass().getDeclaredField( "field_74747_a" );
				}
				catch( final Throwable z )
				{
					AELog.debug( t );
					AELog.debug( z );
				}
			}
		}

		try
		{
			tagList.setAccessible( true );
			return (List<NBTBase>) tagList.get( lB );
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}

		return new ArrayList<NBTBase>();
	}

	/*
	 * Orderless hash on NBT Data, used to work thought huge piles fast, but ignores the order just in case MC decided
	 * to change it... WHICH IS BAD...
	 */
	public static int NBTOrderlessHash( final NBTBase nbt )
	{
		// same type?
		int hash = 0;
		final byte id = nbt.getId();
		hash += id;
		switch( id )
		{
			case 10:
			{
				final NBTTagCompound ctA = (NBTTagCompound) nbt;

				final Set<String> cA = ctA.func_150296_c();

				for( final String name : cA )
				{
					hash += name.hashCode() ^ NBTOrderlessHash( ctA.getTag( name ) );
				}

				return hash;
			}

			case 9: // ) // A instanceof NBTTagList )
			{
				final NBTTagList lA = (NBTTagList) nbt;
				hash += 9 * lA.tagCount();

				final List<NBTBase> l = tagList( lA );
				for( int x = 0; x < l.size(); x++ )
				{
					hash += ( (Integer) x ).hashCode() ^ NBTOrderlessHash( l.get( x ) );
				}

				return hash;
			}

			case 1: // ( A instanceof NBTTagByte )
				return hash + ( (NBTBase.NBTPrimitive) nbt ).func_150290_f();

			case 4: // else if ( A instanceof NBTTagLong )
				return hash + (int) ( (NBTBase.NBTPrimitive) nbt ).func_150291_c();

			case 8: // else if ( A instanceof NBTTagString )
				return hash + ( (NBTTagString) nbt ).func_150285_a_().hashCode();

			case 6: // else if ( A instanceof NBTTagDouble )
				return hash + (int) ( (NBTBase.NBTPrimitive) nbt ).func_150286_g();

			case 5: // else if ( A instanceof NBTTagFloat )
				return hash + (int) ( (NBTBase.NBTPrimitive) nbt ).func_150288_h();

			case 3: // else if ( A instanceof NBTTagInt )
				return hash + ( (NBTBase.NBTPrimitive) nbt ).func_150287_d();

			default:
				return hash;
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

	public static ItemStack[] getBlockDrops( final World w, final int x, final int y, final int z )
	{
		List<ItemStack> out = new ArrayList<ItemStack>();
		final Block which = w.getBlock( x, y, z );

		if( which != null )
		{
			out = which.getDrops( w, x, y, z, w.getBlockMetadata( x, y, z ), 0 );
		}

		if( out == null )
		{
			return new ItemStack[0];
		}
		return out.toArray( new ItemStack[out.size()] );
	}

	public static ForgeDirection cycleOrientations( final ForgeDirection dir, final boolean upAndDown )
	{
		if( upAndDown )
		{
			switch( dir )
			{
				case NORTH:
					return ForgeDirection.SOUTH;
				case SOUTH:
					return ForgeDirection.EAST;
				case EAST:
					return ForgeDirection.WEST;
				case WEST:
					return ForgeDirection.NORTH;
				case UP:
					return ForgeDirection.UP;
				case DOWN:
					return ForgeDirection.DOWN;
				case UNKNOWN:
					return ForgeDirection.UNKNOWN;
			}
		}
		else
		{
			switch( dir )
			{
				case UP:
					return ForgeDirection.DOWN;
				case DOWN:
					return ForgeDirection.NORTH;
				case NORTH:
					return ForgeDirection.SOUTH;
				case SOUTH:
					return ForgeDirection.EAST;
				case EAST:
					return ForgeDirection.WEST;
				case WEST:
					return ForgeDirection.UP;
				case UNKNOWN:
					return ForgeDirection.UNKNOWN;
			}
		}

		return ForgeDirection.UNKNOWN;
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
	public static void spawnDrops( final World w, final int x, final int y, final int z, final List<ItemStack> drops )
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
						final EntityItem ei = new EntityItem( w, 0.5 + offset_x + x, 0.5 + offset_y + y, 0.2 + offset_z + z, i.copy() );
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
		final Block myBlockID = teA.getWorldObj().getBlock( teA.xCoord, teA.yCoord, teA.zCoord );

		if( teA.getWorldObj().getBlock( teA.xCoord + 1, teA.yCoord, teA.zCoord ) == myBlockID )
		{
			teB = teA.getWorldObj().getTileEntity( teA.xCoord + 1, teA.yCoord, teA.zCoord );
			if( !( teB instanceof TileEntityChest ) )
			{
				teB = null;
			}
		}

		if( teB == null )
		{
			if( teA.getWorldObj().getBlock( teA.xCoord - 1, teA.yCoord, teA.zCoord ) == myBlockID )
			{
				teB = teA.getWorldObj().getTileEntity( teA.xCoord - 1, teA.yCoord, teA.zCoord );
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
			if( teA.getWorldObj().getBlock( teA.xCoord, teA.yCoord, teA.zCoord + 1 ) == myBlockID )
			{
				teB = teA.getWorldObj().getTileEntity( teA.xCoord, teA.yCoord, teA.zCoord + 1 );
				if( !( teB instanceof TileEntityChest ) )
				{
					teB = null;
				}
			}
		}

		if( teB == null )
		{
			if( teA.getWorldObj().getBlock( teA.xCoord, teA.yCoord, teA.zCoord - 1 ) == myBlockID )
			{
				teB = teA.getWorldObj().getTileEntity( teA.xCoord, teA.yCoord, teA.zCoord - 1 );
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

		return new InventoryLargeChest( "", teA, (IInventory) teB );
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
		return tag != null && tag.getSpecialComparison() != null;
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

	public static boolean isWrench( final EntityPlayer player, final ItemStack eq, final int x, final int y, final int z )
	{
		if( eq != null )
		{
			try
			{
				if( eq.getItem() instanceof IToolWrench )
				{
					final IToolWrench wrench = (IToolWrench) eq.getItem();
					return wrench.canWrench( player, x, y, z );
				}
			}
			catch( final Throwable ignore )
			{ // explodes without BC

			}

			if( eq.getItem() instanceof IAEWrench )
			{
				final IAEWrench wrench = (IAEWrench) eq.getItem();
				return wrench.canWrench( eq, player, x, y, z );
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

		final EntityPlayer p = FakePlayerFactory.get( w, fakeProfile );
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

	public static <T> T pickRandom( final Collection<T> outs )
	{
		int index = RANDOM_GENERATOR.nextInt( outs.size() );
		final Iterator<T> i = outs.iterator();
		while( i.hasNext() && index > 0 )
		{
			index--;
			i.next();
		}
		index--;
		if( i.hasNext() )
		{
			return i.next();
		}
		return null; // wtf?
	}

	public static ForgeDirection rotateAround( final ForgeDirection forward, final ForgeDirection axis )
	{
		if( axis == ForgeDirection.UNKNOWN || forward == ForgeDirection.UNKNOWN )
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
						return ForgeDirection.EAST;
					case SOUTH:
						return ForgeDirection.WEST;
					case EAST:
						return ForgeDirection.NORTH;
					case WEST:
						return ForgeDirection.SOUTH;
					default:
						break;
				}
				break;
			case UP:
				switch( axis )
				{
					case NORTH:
						return ForgeDirection.WEST;
					case SOUTH:
						return ForgeDirection.EAST;
					case EAST:
						return ForgeDirection.SOUTH;
					case WEST:
						return ForgeDirection.NORTH;
					default:
						break;
				}
				break;
			case NORTH:
				switch( axis )
				{
					case UP:
						return ForgeDirection.WEST;
					case DOWN:
						return ForgeDirection.EAST;
					case EAST:
						return ForgeDirection.UP;
					case WEST:
						return ForgeDirection.DOWN;
					default:
						break;
				}
				break;
			case SOUTH:
				switch( axis )
				{
					case UP:
						return ForgeDirection.EAST;
					case DOWN:
						return ForgeDirection.WEST;
					case EAST:
						return ForgeDirection.DOWN;
					case WEST:
						return ForgeDirection.UP;
					default:
						break;
				}
				break;
			case EAST:
				switch( axis )
				{
					case UP:
						return ForgeDirection.NORTH;
					case DOWN:
						return ForgeDirection.SOUTH;
					case NORTH:
						return ForgeDirection.UP;
					case SOUTH:
						return ForgeDirection.DOWN;
					default:
						break;
				}
			case WEST:
				switch( axis )
				{
					case UP:
						return ForgeDirection.SOUTH;
					case DOWN:
						return ForgeDirection.NORTH;
					case NORTH:
						return ForgeDirection.DOWN;
					case SOUTH:
						return ForgeDirection.UP;
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
		return StatCollector.translateToLocal( string );
	}

	public static boolean isSameItemPrecise( @Nullable final ItemStack is, @Nullable final ItemStack filter )
	{
		return isSameItem( is, filter ) && sameStackStags( is, filter );
	}

	public static boolean isSameItemFuzzy( final ItemStack a, final ItemStack b, final FuzzyMode mode )
	{
		if( a == null && b == null )
		{
			return true;
		}

		if( a == null )
		{
			return false;
		}

		if( b == null )
		{
			return false;
		}

		/*
		 * if ( a.itemID != 0 && b.itemID != 0 && a.isItemStackDamageable() && ! a.getHasSubtypes() && a.itemID ==
		 * b.itemID ) { return (a.getItemDamage() > 0) == (b.getItemDamage() > 0); }
		 */

		// test damageable items..
		if( a.getItem() != null && b.getItem() != null && a.getItem().isDamageable() && a.getItem() == b.getItem() )
		{
			try
			{
				if( mode == FuzzyMode.IGNORE_ALL )
				{
					return true;
				}
				else if( mode == FuzzyMode.PERCENT_99 )
				{
					return ( a.getItemDamageForDisplay() > 1 ) == ( b.getItemDamageForDisplay() > 1 );
				}
				else
				{
					final float percentDamagedOfA = 1.0f - (float) a.getItemDamageForDisplay() / (float) a.getMaxDamage();
					final float percentDamagedOfB = 1.0f - (float) b.getItemDamageForDisplay() / (float) b.getMaxDamage();

					return ( percentDamagedOfA > mode.breakPoint ) == ( percentDamagedOfB > mode.breakPoint );
				}
			}
			catch( final Throwable e )
			{
				if( mode == FuzzyMode.IGNORE_ALL )
				{
					return true;
				}
				else if( mode == FuzzyMode.PERCENT_99 )
				{
					return ( a.getItemDamage() > 1 ) == ( b.getItemDamage() > 1 );
				}
				else
				{
					final float percentDamagedOfA = (float) a.getItemDamage() / (float) a.getMaxDamage();
					final float percentDamagedOfB = (float) b.getItemDamage() / (float) b.getMaxDamage();

					return ( percentDamagedOfA > mode.breakPoint ) == ( percentDamagedOfB > mode.breakPoint );
				}
			}
		}

		final OreReference aOR = OreHelper.INSTANCE.isOre( a );
		final OreReference bOR = OreHelper.INSTANCE.isOre( b );

		if( OreHelper.INSTANCE.sameOre( aOR, bOR ) )
		{
			return true;
		}

		/*
		 * // test ore dictionary.. int OreID = getOreID( a ); if ( OreID != -1 ) return OreID == getOreID( b );
		 * if ( Mode != FuzzyMode.IGNORE_ALL ) { if ( a.hasTagCompound() && !isShared( a.getTagCompound() ) ) { a =
		 * Platform.getSharedItemStack( AEItemStack.create( a ) ); }
		 * if ( b.hasTagCompound() && !isShared( b.getTagCompound() ) ) { b = Platform.getSharedItemStack(
		 * AEItemStack.create( b ) ); }
		 * // test regular items with damage values and what not... if ( isShared( a.getTagCompound() ) && isShared(
		 * b.getTagCompound() ) && a.itemID == b.itemID ) { return ((AppEngSharedNBTTagCompound)
		 * a.getTagCompound()).compareFuzzyWithRegistry( (AppEngSharedNBTTagCompound) b.getTagCompound() ); } }
		 */

		return a.isItemEqual( b );
	}

	public static LookDirection getPlayerRay( final EntityPlayer player, final float eyeOffset )
	{
		final float f = 1.0F;
		final float f1 = player.prevRotationPitch + ( player.rotationPitch - player.prevRotationPitch ) * f;
		final float f2 = player.prevRotationYaw + ( player.rotationYaw - player.prevRotationYaw ) * f;
		final double d0 = player.prevPosX + ( player.posX - player.prevPosX ) * f;
		final double d1 = eyeOffset;
		final double d2 = player.prevPosZ + ( player.posZ - player.prevPosZ ) * f;

		final Vec3 vec3 = Vec3.createVectorHelper( d0, d1, d2 );
		final float f3 = MathHelper.cos( -f2 * 0.017453292F - (float) Math.PI );
		final float f4 = MathHelper.sin( -f2 * 0.017453292F - (float) Math.PI );
		final float f5 = -MathHelper.cos( -f1 * 0.017453292F );
		final float f6 = MathHelper.sin( -f1 * 0.017453292F );
		final float f7 = f4 * f5;
		final float f8 = f3 * f5;
		double d3 = 5.0D;

		if( player instanceof EntityPlayerMP )
		{
			d3 = ( (EntityPlayerMP) player ).theItemInWorldManager.getBlockReachDistance();
		}
		final Vec3 vec31 = vec3.addVector( f7 * d3, f6 * d3, f8 * d3 );
		return new LookDirection( vec3, vec31 );
	}

	public static MovingObjectPosition rayTrace( final EntityPlayer p, final boolean hitBlocks, final boolean hitEntities )
	{
		final World w = p.getEntityWorld();

		final float f = 1.0F;
		float f1 = p.prevRotationPitch + ( p.rotationPitch - p.prevRotationPitch ) * f;
		final float f2 = p.prevRotationYaw + ( p.rotationYaw - p.prevRotationYaw ) * f;
		final double d0 = p.prevPosX + ( p.posX - p.prevPosX ) * f;
		final double d1 = p.prevPosY + ( p.posY - p.prevPosY ) * f + 1.62D - p.yOffset;
		final double d2 = p.prevPosZ + ( p.posZ - p.prevPosZ ) * f;
		final Vec3 vec3 = Vec3.createVectorHelper( d0, d1, d2 );
		final float f3 = MathHelper.cos( -f2 * 0.017453292F - (float) Math.PI );
		final float f4 = MathHelper.sin( -f2 * 0.017453292F - (float) Math.PI );
		final float f5 = -MathHelper.cos( -f1 * 0.017453292F );
		final float f6 = MathHelper.sin( -f1 * 0.017453292F );
		final float f7 = f4 * f5;
		final float f8 = f3 * f5;
		final double d3 = 32.0D;

		final Vec3 vec31 = vec3.addVector( f7 * d3, f6 * d3, f8 * d3 );

		final AxisAlignedBB bb = AxisAlignedBB.getBoundingBox( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ), Math.min( vec3.zCoord, vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ), Math.max( vec3.yCoord, vec31.yCoord ), Math.max( vec3.zCoord, vec31.zCoord ) ).expand( 16, 16, 16 );

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
						if( entity1.riddenByEntity == p )
						{
							continue;
						}

						f1 = 0.3F;
						final AxisAlignedBB boundingBox = entity1.boundingBox.expand( f1, f1, f1 );
						final MovingObjectPosition movingObjectPosition = boundingBox.calculateIntercept( vec3, vec31 );

						if( movingObjectPosition != null )
						{
							final double nd = vec3.squareDistanceTo( movingObjectPosition.hitVec );

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

		MovingObjectPosition pos = null;
		Vec3 vec = null;

		if( hitBlocks )
		{
			vec = Vec3.createVectorHelper( d0, d1, d2 );
			pos = w.rayTraceBlocks( vec3, vec31, true );
		}

		if( entity != null && pos != null && pos.hitVec.squareDistanceTo( vec ) > closest )
		{
			pos = new MovingObjectPosition( entity );
		}
		else if( entity != null && pos == null )
		{
			pos = new MovingObjectPosition( entity );
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

		if( target instanceof ITileStorageMonitorable )
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
				for( final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS )
				{

					final int[] sides = ( (ISidedInventory) target ).getAccessibleSlotsFromSide( dir.ordinal() );

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

		if( AEConfig.instance.isFeatureEnabled( AEFeature.LogSecurityAudits ) )
		{
			AELog.info( "Audit: " + a_isSecure + " : " + b_isSecure + " @ " + a.getLastSecurityKey() + " vs " + b.getLastSecurityKey() + " & " + a.getPlayerID() + " vs " + b.getPlayerID() );
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

	public static void configurePlayer( final EntityPlayer player, final ForgeDirection side, final TileEntity tile )
	{
		player.yOffset = 1.8f;

		float yaw = 0.0f;
		float pitch = 0.0f;
		switch( side )
		{
			case DOWN:
				pitch = 90.0f;
				player.yOffset = -1.8f;
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
			case UNKNOWN:
				break;
			case UP:
				pitch = 90.0f;
				break;
			case WEST:
				yaw = 90.0f;
				break;
		}

		player.posX = tile.xCoord + 0.5;
		player.posY = tile.yCoord + 0.5;
		player.posZ = tile.zCoord + 0.5;

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

			final boolean checkFuzzy = ae_req.isOre() || providedTemplate.getItemDamage() == OreDictionary.WILDCARD_VALUE || providedTemplate.hasTagCompound() || providedTemplate.isItemStackDamageable();

			if( items != null && checkFuzzy )
			{
				for( final IAEItemStack x : items )
				{
					final ItemStack sh = x.getItemStack();
					if( ( Platform.isSameItemType( providedTemplate, sh ) || ae_req.sameOre( x ) ) && !Platform.isSameItem( sh, output ) )
					{ // Platform.isSameItemType( sh, providedTemplate )
						final ItemStack cp = Platform.cloneItemStack( sh );
						cp.stackSize = 1;
						ci.setInventorySlotContents( slot, cp );
						if( r.matches( ci, w ) && Platform.isSameItem( r.getCraftingResult( ci ), output ) )
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

	public static boolean isSameItemType( final ItemStack that, final ItemStack other )
	{
		if( that != null && other != null && that.getItem() == other.getItem() )
		{
			if( that.isItemStackDamageable() )
			{
				return true;
			}
			return that.getItemDamage() == other.getItemDamage();
		}
		return false;
	}

	public static boolean isSameItem( @Nullable final ItemStack left, @Nullable final ItemStack right )
	{
		return left != null && right != null && left.isItemEqual( right );
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

	public static void notifyBlocksOfNeighbors( final World worldObj, final int xCoord, final int yCoord, final int zCoord )
	{
		if( !worldObj.isRemote )
		{
			TickHandler.INSTANCE.addCallable( worldObj, new BlockUpdate( xCoord, yCoord, zCoord ) );
		}
	}

	public static boolean canRepair( final AEFeature type, final ItemStack a, final ItemStack b )
	{
		if( b == null || a == null )
		{
			return false;
		}

		if( type == AEFeature.CertusQuartzTools )
		{
			final IItemDefinition certusQuartzCrystal = AEApi.instance().definitions().materials().certusQuartzCrystal();

			return certusQuartzCrystal.isSameAs( b );
		}

		if( type == AEFeature.NetherQuartzTools )
		{
			return Items.quartz == b.getItem();
		}

		return false;
	}

	public static Object findPreferred( final ItemStack[] is )
	{
		final IParts parts = AEApi.instance().definitions().parts();

		for( final ItemStack stack : is )
		{
			if( parts.cableGlass().sameAs( AEColor.Transparent, stack ) )
			{
				return stack;
			}

			if( parts.cableCovered().sameAs( AEColor.Transparent, stack ) )
			{
				return stack;
			}

			if( parts.cableSmart().sameAs( AEColor.Transparent, stack ) )
			{
				return stack;
			}

			if( parts.cableDense().sameAs( AEColor.Transparent, stack ) )
			{
				return stack;
			}
		}

		return is;
	}

	public static void sendChunk( final Chunk c, final int verticalBits )
	{
		try
		{
			final WorldServer ws = (WorldServer) c.worldObj;
			final PlayerManager pm = ws.getPlayerManager();

			if( getOrCreateChunkWatcher == null )
			{
				getOrCreateChunkWatcher = ReflectionHelper.findMethod( PlayerManager.class, pm, new String[] { "getOrCreateChunkWatcher", "func_72690_a" }, int.class, int.class, boolean.class );
			}

			if( getOrCreateChunkWatcher != null )
			{
				final Object playerInstance = getOrCreateChunkWatcher.invoke( pm, c.xPosition, c.zPosition, false );
				if( playerInstance != null )
				{
					Platform.playerInstance = playerInstance.getClass();

					if( sendToAllPlayersWatchingChunk == null )
					{
						sendToAllPlayersWatchingChunk = ReflectionHelper.findMethod( Platform.playerInstance, playerInstance, new String[] { "sendToAllPlayersWatchingChunk", "func_151251_a" }, Packet.class );
					}

					if( sendToAllPlayersWatchingChunk != null )
					{
						sendToAllPlayersWatchingChunk.invoke( playerInstance, new S21PacketChunkData( c, false, verticalBits ) );
					}
				}
			}
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}
	}

	public static AxisAlignedBB getPrimaryBox( final ForgeDirection side, final int facadeThickness )
	{
		switch( side )
		{
			case DOWN:
				return AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, ( facadeThickness ) / 16.0, 1.0 );
			case EAST:
				return AxisAlignedBB.getBoundingBox( ( 16.0 - facadeThickness ) / 16.0, 0.0, 0.0, 1.0, 1.0, 1.0 );
			case NORTH:
				return AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, 1.0, ( facadeThickness ) / 16.0 );
			case SOUTH:
				return AxisAlignedBB.getBoundingBox( 0.0, 0.0, ( 16.0 - facadeThickness ) / 16.0, 1.0, 1.0, 1.0 );
			case UP:
				return AxisAlignedBB.getBoundingBox( 0.0, ( 16.0 - facadeThickness ) / 16.0, 0.0, 1.0, 1.0, 1.0 );
			case WEST:
				return AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, ( facadeThickness ) / 16.0, 1.0, 1.0 );
			default:
				break;
		}
		return AxisAlignedBB.getBoundingBox( 0, 0, 0, 1, 1, 1 );
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
