package appeng.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.FakePlayer;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IAEItemPowerStorage;
import appeng.api.implementations.IAEWrench;
import appeng.api.implementations.ITileStorageMonitorable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReciever;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAETagCompound;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEItemDefinition;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.Configuration;
import appeng.core.sync.GuiBridge;
import appeng.server.AccessType;
import appeng.server.Security;
import appeng.util.item.AEItemStack;
import appeng.util.item.AESharedNBT;
import appeng.util.item.ItemList;
import buildcraft.api.tools.IToolWrench;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Platform
{

	public static final int DEF_OFFSET = 16;

	/*
	 * random source, use it for item drop locations...
	 */
	static private Random rdnSrc = new Random();

	public static int getRandomInt()
	{
		return Math.abs( rdnSrc.nextInt() );
	}

	public static float getRandomFloat()
	{
		return rdnSrc.nextFloat();
	}

	private static HashMap<String, String> modIDToName;
	private static HashMap<Integer, String> itemTomodID;

	public static String getMod(String modID)
	{
		String out = modIDToName.get( modID );

		if ( out == null )
		{
			out = modID;
		}

		return out;
	}

	public static String getMod(ItemStack is)
	{
		if ( itemTomodID == null )
		{
			itemTomodID = new HashMap<Integer, String>();
			ImmutableTable<String, String, Integer> modObjectTable;

			for (Field f : Block.class.getDeclaredFields())
			{
				try
				{
					Object o = f.get( Block.class );
					if ( o instanceof Block )
					{
						itemTomodID.put( ((Block) o).blockID, "minecraft" );
					}
				}
				catch (Throwable t)
				{
				}
			}

			try
			{
				Field f = GameData.class.getDeclaredField( "modObjectTable" );
				f.setAccessible( true );
				modObjectTable = (ImmutableTable<String, String, Integer>) f.get( GameData.class );
				f.setAccessible( false );

				ImmutableMap<String, Map<String, Integer>> fish = modObjectTable.rowMap();
				for (Map<String, Integer> g : fish.values())
				{
					for (String key : g.keySet())
						itemTomodID.put( g.get( key ), key );
				}
			}
			catch (Throwable t)
			{
			}
		}

		String out = itemTomodID.get( is.itemID );

		if ( out == null )
			return "Unknown";

		return out;
	}

	public static ForgeDirection crossProduct(ForgeDirection forward, ForgeDirection up)
	{
		int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

		switch (west_x + west_y * 2 + west_z * 3)
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

	/*
	 * Simple way to cycle an enum...
	 */
	public static <T extends Enum> T nextEnum(T ce)
	{
		EnumSet valList = EnumSet.allOf( ce.getClass() );

		int pLoc = ce.ordinal() + 1;
		if ( pLoc >= valList.size() )
			pLoc = 0;

		if ( pLoc < 0 || pLoc >= valList.size() )
			pLoc = 0;

		int pos = 0;
		for (Object g : valList)
		{
			if ( pos++ == pLoc )
				return (T) g;
		}

		return null;
	}

	/*
	 * Simple way to cycle an enum...
	 */
	public static <T extends Enum> T prevEnum(T ce)
	{
		EnumSet valList = EnumSet.allOf( ce.getClass() );

		int pLoc = ce.ordinal() - 1;
		if ( pLoc < 0 )
			pLoc = valList.size() - 1;

		if ( pLoc < 0 || pLoc >= valList.size() )
			pLoc = 0;

		int pos = 0;
		for (Object g : valList)
		{
			if ( pos++ == pLoc )
				return (T) g;
		}

		return null;
	}

	/*
	 * returns true if the code is on the client.
	 */
	public static boolean isClient()
	{
		return FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

	/*
	 * returns true if the code is on the server.
	 */
	public static boolean isServer()
	{
		return FMLCommonHandler.instance().getEffectiveSide().isServer();
	}

	public static void openGUI(EntityPlayer p, TileEntity tile, ForgeDirection side, GuiBridge type)
	{
		if ( isClient() )
			return;

		if ( Security.hasPermissions( tile, p, AccessType.BLOCK_ACCESS ) )
			p.openGui( AppEng.instance, type.ordinal() << 3 | (side.ordinal()), tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord );
	}

	/*
	 * Checks to see if a block is air?
	 */
	public static boolean isBlockAir(World w, int x, int y, int z)
	{
		try
		{
			int bid = w.getBlockId( x, y, z );
			if ( bid <= 0 )
				return true;
			if ( Block.blocksList[bid] == null )
				return true;
			return Block.blocksList[bid].isAirBlock( w, x, y, z );
		}
		catch (Throwable e)
		{
			return false;
		}
	}

	/*
	 * Lots of sillyness to try and account for weird tag related junk, basically requires that two tags have at least
	 * something in their tags before it wasts its time comparing them.
	 */
	public static boolean sameStackStags(ItemStack a, ItemStack b)
	{
		if ( a == null && b == null )
			return true;
		if ( a == null || b == null )
			return false;
		if ( a == b )
			return true;

		NBTTagCompound ta = a.getTagCompound();
		NBTTagCompound tb = b.getTagCompound();
		if ( ta == tb )
			return true;

		if ( (ta == null && tb == null) || (ta != null && ta.hasNoTags() && tb == null) || (tb != null && tb.hasNoTags() && ta == null)
				|| (ta != null && ta.hasNoTags() && tb != null && tb.hasNoTags()) )
			return true;

		if ( (ta == null && tb != null) || (ta != null && tb == null) )
			return false;

		// if both tags are shared this is easy...
		if ( AESharedNBT.isShared( ta ) && AESharedNBT.isShared( tb ) )
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
	public static boolean NBTEqualityTest(NBTBase A, NBTBase B)
	{
		// same type?
		byte id = A.getId();
		if ( id == B.getId() )
		{
			switch (id)
			{
			case 10: {
				NBTTagCompound ctA = (NBTTagCompound) A;
				NBTTagCompound ctB = (NBTTagCompound) B;

				Collection cA = ctA.getTags();
				Collection cB = ctB.getTags();

				if ( cA.size() != cB.size() )
					return false;

				Iterator<NBTBase> i = cA.iterator();
				while (i.hasNext())
				{
					NBTBase tag = i.next();
					NBTBase aTag = ctB.getTag( tag.getName() );
					if ( aTag == null )
						return false;

					if ( !NBTEqualityTest( tag, aTag ) )
						return false;
				}

				return true;
			}

			case 9: // ) // A instanceof NBTTagList )
			{
				NBTTagList lA = (NBTTagList) A;
				NBTTagList lB = (NBTTagList) B;
				if ( lA.tagCount() != lB.tagCount() )
					return false;

				for (int x = 0; x < lA.tagCount(); x++)
				{
					NBTBase tag = lA.tagAt( x );
					NBTBase aTag = lB.tagAt( x );

					if ( aTag == null )
						return false;

					if ( !NBTEqualityTest( tag, aTag ) )
						return false;
				}

				return true;
			}

			case 1: // ( A instanceof NBTTagByte )
				return ((NBTTagByte) A).data == ((NBTTagByte) B).data;

			case 4: // else if ( A instanceof NBTTagLong )
				return ((NBTTagLong) A).data == ((NBTTagLong) B).data;

			case 8: // else if ( A instanceof NBTTagString )
				return ((NBTTagString) A).data == ((NBTTagString) B).data || ((NBTTagString) A).data.equals( ((NBTTagString) B).data );

			case 6: // else if ( A instanceof NBTTagDouble )
				return ((NBTTagDouble) A).data == ((NBTTagDouble) B).data;

			case 5: // else if ( A instanceof NBTTagFloat )
				return ((NBTTagFloat) A).data == ((NBTTagFloat) B).data;

			case 3: // else if ( A instanceof NBTTagInt )
				return ((NBTTagInt) A).data == ((NBTTagInt) B).data;

			default:
				return A.equals( B );
			}
		}

		return false;
	}

	/*
	 * Orderless hash on NBT Data, used to work thought huge piles fast, but ignroes the order just in case MC decided
	 * to change it... WHICH IS BAD...
	 */
	public static int NBTOrderlessHash(NBTBase A)
	{
		// same type?
		int hash = 0;
		byte id = A.getId();
		hash += id;
		switch (id)
		{
		case 10: {
			NBTTagCompound ctA = (NBTTagCompound) A;

			Collection cA = ctA.getTags();

			Iterator<NBTBase> i = cA.iterator();
			while (i.hasNext())
			{
				NBTBase tag = i.next();
				hash += NBTOrderlessHash( ctA.getTag( tag.getName() ) );
			}

			return hash;
		}

		case 9: // ) // A instanceof NBTTagList )
		{
			NBTTagList lA = (NBTTagList) A;
			hash += 9 * lA.tagCount();

			for (int x = 0; x < lA.tagCount(); x++)
			{
				hash += NBTOrderlessHash( lA.tagAt( x ) );
			}

			return hash;
		}

		case 1: // ( A instanceof NBTTagByte )
			return hash + ((NBTTagByte) A).data;

		case 4: // else if ( A instanceof NBTTagLong )
			return hash + (int) ((NBTTagLong) A).data;

		case 8: // else if ( A instanceof NBTTagString )
			return hash + ((NBTTagString) A).data.hashCode();

		case 6: // else if ( A instanceof NBTTagDouble )
			return hash + (int) ((NBTTagDouble) A).data;

		case 5: // else if ( A instanceof NBTTagFloat )
			return hash + (int) ((NBTTagFloat) A).data;

		case 3: // else if ( A instanceof NBTTagInt )
			return hash + ((NBTTagInt) A).data;

		default:
			return hash;
		}
	}

	/*
	 * The usual version of this returns an ItemStack, this version returns the recipe.
	 */
	public static IRecipe findMatchingRecipe(InventoryCrafting par1InventoryCrafting, World par2World)
	{
		CraftingManager cm = CraftingManager.getInstance();
		List<IRecipe> rl = cm.getRecipeList();

		for (int x = 0; x < rl.size(); ++x)
		{
			IRecipe r = rl.get( x );

			if ( r.matches( par1InventoryCrafting, par2World ) )
			{
				return r;
			}
		}

		return null;
	}

	public static ItemStack[] getBlockDrops(World w, int x, int y, int z)
	{
		List<ItemStack> out = new ArrayList<ItemStack>();
		int bid = w.getBlockId( x, y, z );

		if ( Block.blocksList.length > bid )
		{
			Block which = Block.blocksList[bid];
			if ( which == null )
				return new ItemStack[0];
			out = which.getBlockDropped( w, x, y, z, w.getBlockMetadata( x, y, z ), 0 );
		}

		if ( out == null )
			return new ItemStack[0];
		return out.toArray( new ItemStack[out.size()] );
	}

	public static ForgeDirection cycleOrientations(ForgeDirection dir, boolean upAndDown)
	{
		if ( upAndDown )
		{
			switch (dir)
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
			switch (dir)
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
	public static NBTTagCompound openNbtData(ItemStack i)
	{
		NBTTagCompound compound = i.getTagCompound();

		if ( compound == null )
		{
			i.setTagCompound( compound = new NBTTagCompound() );
		}

		return compound;
	}

	/*
	 * Generates Item entiies in the world similar to how items are generally droped.
	 */
	public static void spawnDrops(World w, int x, int y, int z, List<ItemStack> drops)
	{
		if ( isServer() )
		{
			for (ItemStack i : drops)
			{
				if ( i != null )
				{
					if ( i.stackSize > 0 )
					{
						double offset_x = (getRandomInt() % 32 - 16) / 82;
						double offset_y = (getRandomInt() % 32 - 16) / 82;
						double offset_z = (getRandomInt() % 32 - 16) / 82;
						EntityItem ei = new EntityItem( w, 0.5 + offset_x + x, 0.5 + offset_y + y, 0.2 + offset_z + z, i.copy() );
						w.spawnEntityInWorld( ei );
					}
				}
			}
		}
	}

	/*
	 * Utility function to get the full inventory for a Double Chest in the World.
	 */
	public static IInventory GetChestInv(Object te)
	{
		TileEntityChest teA = (TileEntityChest) te;
		TileEntity teB = null;
		int myBlockID = teA.worldObj.getBlockId( teA.xCoord, teA.yCoord, teA.zCoord );

		if ( teA.worldObj.getBlockId( teA.xCoord + 1, teA.yCoord, teA.zCoord ) == myBlockID )
		{
			teB = teA.worldObj.getBlockTileEntity( teA.xCoord + 1, teA.yCoord, teA.zCoord );
			if ( !(teB instanceof TileEntityChest) )
				teB = null;
		}

		if ( teB == null )
		{
			if ( teA.worldObj.getBlockId( teA.xCoord - 1, teA.yCoord, teA.zCoord ) == myBlockID )
			{
				teB = teA.worldObj.getBlockTileEntity( teA.xCoord - 1, teA.yCoord, teA.zCoord );
				if ( !(teB instanceof TileEntityChest) )
					teB = null;
				else
				{
					TileEntityChest x = teA;
					teA = (TileEntityChest) teB;
					teB = x;
				}
			}
		}

		if ( teB == null )
		{
			if ( teA.worldObj.getBlockId( teA.xCoord, teA.yCoord, teA.zCoord + 1 ) == myBlockID )
			{
				teB = teA.worldObj.getBlockTileEntity( teA.xCoord, teA.yCoord, teA.zCoord + 1 );
				if ( !(teB instanceof TileEntityChest) )
					teB = null;
			}
		}

		if ( teB == null )
		{
			if ( teA.worldObj.getBlockId( teA.xCoord, teA.yCoord, teA.zCoord - 1 ) == myBlockID )
			{
				teB = teA.worldObj.getBlockTileEntity( teA.xCoord, teA.yCoord, teA.zCoord - 1 );
				if ( !(teB instanceof TileEntityChest) )
					teB = null;
				else
				{
					TileEntityChest x = teA;
					teA = (TileEntityChest) teB;
					teB = x;
				}
			}
		}

		if ( teB == null )
			return teA;

		return new InventoryLargeChest( "", teA, (TileEntityChest) teB );
	}

	public static boolean isModLoaded(String modid)
	{
		try
		{
			// if this fails for some reason, try the other method.
			return Loader.isModLoaded( modid );
		}
		catch (Throwable e)
		{
		}

		for (ModContainer f : Loader.instance().getActiveModList())
		{
			if ( f.getModId().equals( modid ) )
			{
				return true;
			}
		}
		return false;
	}

	public static ItemStack findMatchingRecipeOutput(InventoryCrafting ic, World worldObj)
	{
		return CraftingManager.getInstance().findMatchingRecipe( ic, worldObj );
	}

	@SideOnly(Side.CLIENT)
	public static List getTooltip(Object o)
	{
		if ( o == null )
			return new ArrayList();

		ItemStack itemStack = null;
		if ( o instanceof AEItemStack )
		{
			AEItemStack ais = (AEItemStack) o;
			return ais.getToolTip();
		}
		else if ( o instanceof ItemStack )
			itemStack = (ItemStack) o;
		else
			return new ArrayList();

		try
		{
			return itemStack.getTooltip( Minecraft.getMinecraft().thePlayer, false );
		}
		catch (Exception errB)
		{
			return new ArrayList();
		}
	}

	public static String getItemDisplayName(Object o)
	{
		if ( o == null )
			return "** Null";

		ItemStack itemStack = null;
		if ( o instanceof AEItemStack )
		{
			String n = ((AEItemStack) o).getDisplayName();
			return n == null ? "** Null" : n;
		}
		else if ( o instanceof ItemStack )
			itemStack = (ItemStack) o;
		else
			return "**Invalid Object";

		try
		{
			String name = itemStack.getDisplayName();
			if ( name == null || name.equals( "" ) )
				name = itemStack.getItem().getUnlocalizedName( itemStack );
			return name == null ? "** Null" : name;
		}
		catch (Exception errA)
		{
			try
			{
				String n = itemStack.getUnlocalizedName();
				return n == null ? "** Null" : n;
			}
			catch (Exception errB)
			{
				return "** Exception";
			}
		}
	}

	public static boolean hasSpecialComparison(IAEItemStack willAdd)
	{
		if ( willAdd == null )
			return false;
		IAETagCompound tag = willAdd.getTagCompound();
		if ( tag != null && ((AESharedNBT) tag).getSpecialComparison() != null )
			return true;
		return false;
	}

	public static boolean hasSpecialComparison(ItemStack willAdd)
	{
		if ( AESharedNBT.isShared( willAdd.getTagCompound() ) )
		{
			if ( ((AESharedNBT) willAdd.getTagCompound()).getSpecialComparison() != null )
				return true;
		}
		return false;
	}

	public static boolean isWrench(EntityPlayer player, ItemStack eq, int x, int y, int z)
	{
		if ( eq != null )
		{
			try
			{
				if ( eq.getItem() instanceof IToolWrench )
				{
					IToolWrench wrench = (IToolWrench) eq.getItem();
					return wrench.canWrench( player, x, y, z );
				}
			}
			catch (Throwable _)
			{ // explodes without BC
				if ( eq.getItem() instanceof IAEWrench )
				{
					IAEWrench wrench = (IAEWrench) eq.getItem();
					return wrench.canWrench( eq, player, x, y, z );
				}
			}
		}
		return false;
	}

	public static boolean isChargeable(ItemStack i)
	{
		if ( i == null )
			return false;
		Item it = i.getItem();
		if ( it instanceof IAEItemPowerStorage )
		{
			return ((IAEItemPowerStorage) it).getPowerFlow( i ) != AccessRestriction.READ;
		}
		return false;
	}

	private static WeakHashMap<World, EntityPlayer> fakePlayers = new WeakHashMap<World, EntityPlayer>();

	public static EntityPlayer getPlayer(World w)
	{
		EntityPlayer wrp = fakePlayers.get( w );
		if ( wrp != null )
			return wrp;

		EntityPlayer p = new FakePlayer( w, "[AppEng]" );
		fakePlayers.put( w, p );
		return p;
	}

	public static int MC2MEColor(int color)
	{
		switch (color)
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

	public static int findEmpty(Object[] l)
	{
		for (int x = 0; x < l.length; x++)
		{
			if ( l[x] == null )
				return x;
		}
		return -1;
	}

	public static <T> T pickRandom(Collection<T> outs)
	{
		int index = rdnSrc.nextInt( outs.size() );
		Iterator<T> i = outs.iterator();
		while (i.hasNext() && index > 0)
			i.next();
		if ( i.hasNext() )
			return i.next();
		return null; // wtf?
	}

	public static boolean blockAtLocationIs(IBlockAccess w, int x, int y, int z, AEItemDefinition def)
	{
		int blk = w.getBlockId( x, y, z );
		return def.block() == Block.blocksList[blk];
	}

	public static ForgeDirection rotateAround(ForgeDirection forward, ForgeDirection axis)
	{
		if ( axis == ForgeDirection.UNKNOWN || forward == ForgeDirection.UNKNOWN )
			return forward;

		switch (forward)
		{
		case DOWN:
			switch (axis)
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
			switch (axis)
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
			switch (axis)
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
			switch (axis)
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
			switch (axis)
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
			switch (axis)
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

	@SideOnly(Side.CLIENT)
	public static String gui_localize(String string)
	{
		AELog.localization( "gui", string );
		return StatCollector.translateToLocal( string );
	}

	public static boolean isSameItemType(ItemStack ol, ItemStack op)
	{
		if ( ol != null && op != null && ol.itemID == op.itemID )
		{
			if ( ol.isItemStackDamageable() )
				return true;
			return ol.getItemDamage() == ol.getItemDamage();
		}
		return false;
	}

	public static boolean isSameItem(ItemStack ol, ItemStack op)
	{
		return ol != null && op != null && ol.isItemEqual( op );
	}

	public static ItemStack cloneItemStack(ItemStack a)
	{
		return a.copy();
	}

	public static boolean isSameItemPrecise(ItemStack is, ItemStack filter)
	{
		return isSameItem( is, filter );
	}

	public static boolean isSameItemFuzzy(ItemStack a, ItemStack b, FuzzyMode Mode)
	{
		if ( a == null && b == null )
		{
			return true;
		}

		if ( a == null )
		{
			return false;
		}

		if ( b == null )
		{
			return false;
		}

		/*
		 * if ( a.itemID != 0 && b.itemID != 0 && a.isItemStackDamageable() && ! a.getHasSubtypes() && a.itemID ==
		 * b.itemID ) { return (a.getItemDamage() > 0) == (b.getItemDamage() > 0); }
		 */

		// test damageable items..
		if ( a.itemID != 0 && b.itemID != 0 && a.getItem().isDamageable() && a.itemID == b.itemID )
		{
			try
			{
				if ( Mode == FuzzyMode.IGNORE_ALL )
				{
					return true;
				}
				else if ( Mode == FuzzyMode.PERCENT_99 )
				{
					return (a.getItemDamageForDisplay() > 1) == (b.getItemDamageForDisplay() > 1);
				}
				else
				{
					float APercentDamaged = 1.0f - (float) a.getItemDamageForDisplay() / (float) a.getMaxDamage();
					float BPercentDamaged = 1.0f - (float) b.getItemDamageForDisplay() / (float) b.getMaxDamage();

					return (APercentDamaged > Mode.breakPoint) == (BPercentDamaged > Mode.breakPoint);
				}
			}
			catch (Throwable e)
			{
				if ( Mode == FuzzyMode.IGNORE_ALL )
				{
					return true;
				}
				else if ( Mode == FuzzyMode.PERCENT_99 )
				{
					return (a.getItemDamage() > 1) == (b.getItemDamage() > 1);
				}
				else
				{
					float APercentDamaged = (float) a.getItemDamage() / (float) a.getMaxDamage();
					float BPercentDamaged = (float) b.getItemDamage() / (float) b.getMaxDamage();

					return (APercentDamaged > Mode.breakPoint) == (BPercentDamaged > Mode.breakPoint);
				}
			}
		}

		/*
		 * // test ore dictionary.. int OreID = getOreID( a ); if ( OreID != -1 ) return OreID == getOreID( b );
		 * 
		 * if ( Mode != FuzzyMode.IGNORE_ALL ) { if ( a.hasTagCompound() && !isShared( a.getTagCompound() ) ) { a =
		 * Platform.getSharedItemStack( AEItemStack.create( a ) ); }
		 * 
		 * if ( b.hasTagCompound() && !isShared( b.getTagCompound() ) ) { b = Platform.getSharedItemStack(
		 * AEItemStack.create( b ) ); }
		 * 
		 * // test regular items with damage values and what not... if ( isShared( a.getTagCompound() ) && isShared(
		 * b.getTagCompound() ) && a.itemID == b.itemID ) { return ((AppEngSharedNBTTagCompound)
		 * a.getTagCompound()).compareFuzzyWithRegistry( (AppEngSharedNBTTagCompound) b.getTagCompound() ); } }
		 */

		return a.isItemEqual( b );
	}

	public static LookDirection getPlayerRay(EntityPlayer player)
	{
		float f = 1.0F;
		float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
		float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
		double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double) f
				+ (double) (player.worldObj.isRemote ? player.getEyeHeight() - player.getDefaultEyeHeight() : player.getEyeHeight()); // isRemote

		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
		Vec3 vec3 = player.worldObj.getWorldVec3Pool().getVecFromPool( d0, d1, d2 );
		float f3 = MathHelper.cos( -f2 * 0.017453292F - (float) Math.PI );
		float f4 = MathHelper.sin( -f2 * 0.017453292F - (float) Math.PI );
		float f5 = -MathHelper.cos( -f1 * 0.017453292F );
		float f6 = MathHelper.sin( -f1 * 0.017453292F );
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 5.0D;

		if ( player instanceof EntityPlayerMP )
		{
			d3 = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
		}
		Vec3 vec31 = vec3.addVector( (double) f7 * d3, (double) f6 * d3, (double) f8 * d3 );
		return new LookDirection( vec3, vec31 );
	}

	public static long nanoTime()
	{
		if ( Configuration.instance.enableNetworkProfiler )
			return System.nanoTime();
		return 0;
	}

	public static IAEItemStack poweredExtraction(IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack request)
	{
		IAEItemStack possible = cell.extractItems( request.copy(), Actionable.SIMULATE );

		long retrieved = 0;
		if ( possible != null )
			retrieved = possible.getStackSize();

		double availablePower = energy.extractAEPower( retrieved, Actionable.SIMULATE, PowerMultiplier.CONFIG );

		long itemToExtract = Math.min( (long) (availablePower + 0.9), retrieved );

		if ( itemToExtract > 0 )
		{
			energy.extractAEPower( retrieved, Actionable.MODULATE, PowerMultiplier.CONFIG );

			possible.setStackSize( itemToExtract );
			return cell.extractItems( possible, Actionable.MODULATE );
		}

		return null;
	}

	public static IAEItemStack poweredInsert(IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack input)
	{
		IAEItemStack possible = cell.injectItems( input.copy(), Actionable.SIMULATE );

		long stored = input.getStackSize();
		if ( possible != null )
			stored -= possible.getStackSize();

		double availablePower = energy.extractAEPower( stored, Actionable.SIMULATE, PowerMultiplier.CONFIG );

		long itemToAdd = Math.min( (long) (availablePower + 0.9), stored );

		if ( itemToAdd > 0 )
		{
			energy.extractAEPower( stored, Actionable.MODULATE, PowerMultiplier.CONFIG );

			if ( itemToAdd < input.getStackSize() )
			{
				IAEItemStack split = input.copy();
				split.decStackSize( itemToAdd );
				input.setStackSize( itemToAdd );
				split.add( cell.injectItems( input, Actionable.MODULATE ) );
				return split;
			}
			return cell.injectItems( input, Actionable.MODULATE );
		}

		return input;
	}

	public static void postChanges(IStorageGrid gs, ItemStack removed, ItemStack added)
	{
		if ( removed != null )
		{
			IMEInventory<IAEItemStack> myItems = AEApi.instance().registries().cell().getCellInventory( removed, StorageChannel.ITEMS );

			if ( myItems != null )
			{
				for (IAEItemStack is : myItems.getAvailableItems( new ItemList() ))
				{
					is.setStackSize( -is.getStackSize() );
					gs.postAlterationOfStoredItems( StorageChannel.ITEMS, is );
				}
			}

			IMEInventory<IAEFluidStack> myFluids = AEApi.instance().registries().cell().getCellInventory( removed, StorageChannel.FLUIDS );

			if ( myFluids != null )
			{
				for (IAEFluidStack is : myFluids.getAvailableItems( new ItemList() ))
				{
					is.setStackSize( -is.getStackSize() );
					gs.postAlterationOfStoredItems( StorageChannel.ITEMS, is );
				}
			}
		}

		if ( added != null )
		{
			IMEInventory<IAEItemStack> myItems = AEApi.instance().registries().cell().getCellInventory( added, StorageChannel.ITEMS );

			if ( myItems != null )
			{
				for (IAEItemStack is : myItems.getAvailableItems( new ItemList() ))
				{
					gs.postAlterationOfStoredItems( StorageChannel.ITEMS, is );
				}
			}

			IMEInventory<IAEFluidStack> myFluids = AEApi.instance().registries().cell().getCellInventory( added, StorageChannel.FLUIDS );

			if ( myFluids != null )
			{
				for (IAEFluidStack is : myFluids.getAvailableItems( new ItemList() ))
				{
					gs.postAlterationOfStoredItems( StorageChannel.ITEMS, is );
				}
			}
		}
	}

	static public <T extends IAEStack<T>> void postListChanges(IItemList<T> before, IItemList<T> after, IMEMonitorHandlerReciever<T> meMonitorPassthu)
	{
		for (T is : before)
			is.setStackSize( -is.getStackSize() );

		for (T is : after)
			before.add( is );

		for (T is : before)
		{
			if ( is.getStackSize() != 0 )
			{
				meMonitorPassthu.postChange( is );
			}
		}
	}

	public static int generateTileHash(TileEntity target)
	{
		if ( target == null )
			return 0;

		int hash = target.hashCode();

		if ( target instanceof ITileStorageMonitorable )
			return 0;
		else if ( target instanceof TileEntityChest )
		{
			TileEntityChest targ = (TileEntityChest) target;
			targ.checkForAdjacentChests();
			if ( targ.adjacentChestZNeg != null )
				hash ^= targ.adjacentChestZNeg.hashCode();
			else if ( targ.adjacentChestZPosition != null )
				hash ^= targ.adjacentChestZPosition.hashCode();
			else if ( targ.adjacentChestXPos != null )
				hash ^= targ.adjacentChestXPos.hashCode();
			else if ( targ.adjacentChestXNeg != null )
				hash ^= targ.adjacentChestXNeg.hashCode();
		}
		else if ( target instanceof IInventory )
		{
			hash ^= ((IInventory) target).getSizeInventory();

			if ( target instanceof ISidedInventory )
			{
				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				{
					int offset = 0;
					for (Integer Side : ((ISidedInventory) target).getAccessibleSlotsFromSide( dir.ordinal() ))
					{
						hash ^= Side << (offset++ % 20);
					}
				}
			}
		}

		return hash;
	}
}
