package appeng.util;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
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
import appeng.api.util.AEItemDefinition;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.server.AccessType;
import appeng.util.item.AEItemStack;
import appeng.util.item.AESharedNBT;
import appeng.util.item.ItemList;
import appeng.util.item.OreHelper;
import appeng.util.item.OreRefrence;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Platform
{

	public static Block air = Blocks.air;

	public static final int DEF_OFFSET = 16;

	/*
	 * random source, use it for item drop locations...
	 */
	static private Random rdnSrc = new Random();

	public static Random getRandom()
	{
		return rdnSrc;
	}

	public static int getRandomInt()
	{
		return Math.abs( rdnSrc.nextInt() );
	}

	public static float getRandomFloat()
	{
		return rdnSrc.nextFloat();
	}

	/**
	 * This displays the value for encoded longs ( double *100 )
	 * 
	 * @param n
	 * @param isRate
	 * @return
	 */
	public static String formatPowerLong(long n, boolean isRate)
	{
		double p = ((double) n) / 100;

		PowerUnits displayUnits = AEConfig.instance.selectedPowerUnit();
		p = PowerUnits.AE.convertTo( displayUnits, p );

		int offset = 0;
		String Lvl = "";
		String preFixes[] = new String[] { "k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y" };
		String unitName = displayUnits.name();

		if ( displayUnits == PowerUnits.WA )
			unitName = "J";

		if ( displayUnits == PowerUnits.KJ )
		{
			Lvl = preFixes[offset];
			unitName = "J";
			offset++;
		}

		while (p > 1000 && offset < preFixes.length)
		{
			p /= 1000;
			Lvl = preFixes[offset];
			offset++;
		}

		DecimalFormat df = new DecimalFormat( "#.##" );
		return df.format( p ) + " " + Lvl + unitName + (isRate ? "/t" : "");
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

	public static <T extends Enum> T rotateEnum(T ce, boolean backwards, EnumSet ValidOptions)
	{
		do
		{
			if ( backwards )
				ce = prevEnum( ce );
			else
				ce = nextEnum( ce );
		}
		while (!ValidOptions.contains( ce ) || isNotValidSetting( ce ));

		return ce;
	}

	private static boolean isNotValidSetting(Enum e)
	{
		if ( e == SortOrder.INVTWEAKS && !AppEng.instance.isIntegrationEnabled( "InvTweaks" ) )
			return true;

		if ( e == SearchBoxMode.NEI_AUTOSEARCH && !AppEng.instance.isIntegrationEnabled( "NEI" ) )
			return true;

		if ( e == SearchBoxMode.NEI_MANUAL_SEARCH && !AppEng.instance.isIntegrationEnabled( "NEI" ) )
			return true;

		return false;
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

		int x = (int) p.posX, y = (int) p.posY, z = (int) p.posZ;
		if ( tile != null )
		{
			x = tile.xCoord;
			y = tile.yCoord;
			z = tile.zCoord;
		}

		if ( (type.getType().isItem() && tile == null) || type.hasPermissions( tile, x, y, z, side, p ) )
		{
			if ( tile == null )
				p.openGui( AppEng.instance, type.ordinal() << 3, p.getEntityWorld(), x, y, z );
			else
				p.openGui( AppEng.instance, type.ordinal() << 3 | (side.ordinal()), tile.getWorldObj(), x, y, z );
		}
	}

	public static boolean hasPermissions(int x, int y, int z, EntityPlayer player, AccessType blockAccess)
	{
		return true;
	}

	/*
	 * Checks to see if a block is air?
	 */
	public static boolean isBlockAir(World w, int x, int y, int z)
	{
		try
		{
			return w.getBlock( x, y, z ).isAir( w, x, y, z );
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

				Set<String> cA = ctA.func_150296_c();
				Set<String> cB = ctB.func_150296_c();

				if ( cA.size() != cB.size() )
					return false;

				Iterator<String> i = cA.iterator();
				while (i.hasNext())
				{
					String name = i.next();
					NBTBase tag = ctA.getTag( name );
					NBTBase aTag = ctB.getTag( name );
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

				List<NBTBase> tag = tagList( lA );
				List<NBTBase> aTag = tagList( lB );
				if ( tag.size() != aTag.size() )
					return false;

				for (int x = 0; x < tag.size(); x++)
				{
					if ( aTag.get( x ) == null )
						return false;

					if ( !NBTEqualityTest( tag.get( x ), aTag.get( x ) ) )
						return false;
				}

				return true;
			}

			case 1: // ( A instanceof NBTTagByte )
				return ((NBTTagByte) A).func_150287_d() == ((NBTTagByte) B).func_150287_d();

			case 4: // else if ( A instanceof NBTTagLong )
				return ((NBTTagLong) A).func_150291_c() == ((NBTTagLong) B).func_150291_c();

			case 8: // else if ( A instanceof NBTTagString )
				return ((NBTTagString) A).func_150285_a_() == ((NBTTagString) B).func_150285_a_()
						|| ((NBTTagString) A).func_150285_a_().equals( ((NBTTagString) B).func_150285_a_() );

			case 6: // else if ( A instanceof NBTTagDouble )
				return ((NBTTagDouble) A).func_150286_g() == ((NBTTagDouble) B).func_150286_g();

			case 5: // else if ( A instanceof NBTTagFloat )
				return ((NBTTagFloat) A).func_150288_h() == ((NBTTagFloat) B).func_150288_h();

			case 3: // else if ( A instanceof NBTTagInt )
				return ((NBTTagInt) A).func_150287_d() == ((NBTTagInt) B).func_150287_d();

			default:
				return A.equals( B );
			}
		}

		return false;
	}

	private static Field tagList;

	private static List<NBTBase> tagList(NBTTagList lB)
	{
		if ( tagList == null )
		{
			try
			{
				tagList = lB.getClass().getDeclaredField( "tagList" );
			}
			catch (Throwable t)
			{
				try
				{
					tagList = lB.getClass().getDeclaredField( "field_74747_a" );
				}
				catch (Throwable z)
				{
					AELog.error( t );
					AELog.error( z );
				}
			}
		}

		try
		{
			tagList.setAccessible( true );
			return (List<NBTBase>) tagList.get( lB );
		}
		catch (Throwable t)
		{
			AELog.error( t );
		}

		return new ArrayList();
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

			Set<String> cA = ctA.func_150296_c();

			Iterator<String> i = cA.iterator();
			while (i.hasNext())
			{
				String name = i.next();
				hash += name.hashCode() ^ NBTOrderlessHash( ctA.getTag( name ) );
			}

			return hash;
		}

		case 9: // ) // A instanceof NBTTagList )
		{
			NBTTagList lA = (NBTTagList) A;
			hash += 9 * lA.tagCount();

			List<NBTBase> l = tagList( lA );
			for (int x = 0; x < l.size(); x++)
			{
				hash += ((Integer) x).hashCode() ^ NBTOrderlessHash( l.get( x ) );
			}

			return hash;
		}

		case 1: // ( A instanceof NBTTagByte )
			return hash + ((NBTTagByte) A).func_150290_f();

		case 4: // else if ( A instanceof NBTTagLong )
			return hash + (int) ((NBTTagLong) A).func_150291_c();

		case 8: // else if ( A instanceof NBTTagString )
			return hash + ((NBTTagString) A).func_150285_a_().hashCode();

		case 6: // else if ( A instanceof NBTTagDouble )
			return hash + (int) ((NBTTagDouble) A).func_150286_g();

		case 5: // else if ( A instanceof NBTTagFloat )
			return hash + (int) ((NBTTagFloat) A).func_150288_h();

		case 3: // else if ( A instanceof NBTTagInt )
			return hash + ((NBTTagInt) A).func_150287_d();

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
		Block which = w.getBlock( x, y, z );

		if ( which != null )
		{
			out = which.getDrops( w, x, y, z, w.getBlockMetadata( x, y, z ), 0 );
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
		Block myBlockID = teA.getWorldObj().getBlock( teA.xCoord, teA.yCoord, teA.zCoord );

		if ( teA.getWorldObj().getBlock( teA.xCoord + 1, teA.yCoord, teA.zCoord ) == myBlockID )
		{
			teB = teA.getWorldObj().getTileEntity( teA.xCoord + 1, teA.yCoord, teA.zCoord );
			if ( !(teB instanceof TileEntityChest) )
				teB = null;
		}

		if ( teB == null )
		{
			if ( teA.getWorldObj().getBlock( teA.xCoord - 1, teA.yCoord, teA.zCoord ) == myBlockID )
			{
				teB = teA.getWorldObj().getTileEntity( teA.xCoord - 1, teA.yCoord, teA.zCoord );
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
			if ( teA.getWorldObj().getBlock( teA.xCoord, teA.yCoord, teA.zCoord + 1 ) == myBlockID )
			{
				teB = teA.getWorldObj().getTileEntity( teA.xCoord, teA.yCoord, teA.zCoord + 1 );
				if ( !(teB instanceof TileEntityChest) )
					teB = null;
			}
		}

		if ( teB == null )
		{
			if ( teA.getWorldObj().getBlock( teA.xCoord, teA.yCoord, teA.zCoord - 1 ) == myBlockID )
			{
				teB = teA.getWorldObj().getTileEntity( teA.xCoord, teA.yCoord, teA.zCoord - 1 );
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

			}

			if ( eq.getItem() instanceof IAEWrench )
			{
				IAEWrench wrench = (IAEWrench) eq.getItem();
				return wrench.canWrench( eq, player, x, y, z );
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

	public static EntityPlayer getPlayer(WorldServer w)
	{
		EntityPlayer wrp = fakePlayers.get( w );
		if ( wrp != null )
			return wrp;

		EntityPlayer p = FakePlayerFactory.getMinecraft( w );
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
		while (i.hasNext() && index-- > 0)
			i.next();
		if ( i.hasNext() )
			return i.next();
		return null; // wtf?
	}

	public static boolean blockAtLocationIs(IBlockAccess w, int x, int y, int z, AEItemDefinition def)
	{
		return def.block() == w.getBlock( x, y, z );
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
		return StatCollector.translateToLocal( string );
	}

	public static boolean isSameItemType(ItemStack ol, ItemStack op)
	{
		if ( ol != null && op != null && ol.getItem() == op.getItem() )
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
		return isSameItem( is, filter ) && sameStackStags( is, filter );
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
		if ( a.getItem() != null && b.getItem() != null && a.getItem().isDamageable() && a.getItem() == b.getItem() )
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

		OreRefrence aOR = OreHelper.instance.isOre( a );
		OreRefrence bOR = OreHelper.instance.isOre( b );

		if ( OreHelper.instance.sameOre( aOR, bOR ) )
			return true;

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
		Vec3 vec3 = Vec3.createVectorHelper( d0, d1, d2 );
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

	public static MovingObjectPosition rayTrace(EntityPlayer p, boolean hitBlocks, boolean hitEntities)
	{
		World w = p.getEntityWorld();

		float f = 1.0F;
		float f1 = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * f;
		float f2 = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * f;
		double d0 = p.prevPosX + (p.posX - p.prevPosX) * (double) f;
		double d1 = p.prevPosY + (p.posY - p.prevPosY) * (double) f + 1.62D - (double) p.yOffset;
		double d2 = p.prevPosZ + (p.posZ - p.prevPosZ) * (double) f;
		Vec3 vec3 = Vec3.createVectorHelper( d0, d1, d2 );
		float f3 = MathHelper.cos( -f2 * 0.017453292F - (float) Math.PI );
		float f4 = MathHelper.sin( -f2 * 0.017453292F - (float) Math.PI );
		float f5 = -MathHelper.cos( -f1 * 0.017453292F );
		float f6 = MathHelper.sin( -f1 * 0.017453292F );
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 32.0D;

		Vec3 vec31 = vec3.addVector( (double) f7 * d3, (double) f6 * d3, (double) f8 * d3 );

		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ),
				Math.min( vec3.zCoord, vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ), Math.max( vec3.yCoord, vec31.yCoord ),
				Math.max( vec3.zCoord, vec31.zCoord ) ).expand( 16, 16, 16 );

		Entity entity = null;
		double Closeest = 9999999.0D;
		if ( hitEntities )
		{
			List list = w.getEntitiesWithinAABBExcludingEntity( p, bb );
			int l;

			for (l = 0; l < list.size(); ++l)
			{
				Entity entity1 = (Entity) list.get( l );

				if ( entity1.isDead == false && entity1 != p && !(entity1 instanceof EntityItem) )
				{
					if ( entity1.isEntityAlive() )
					{
						// prevent killing / flying of mounts.
						if ( entity1.riddenByEntity == p )
							continue;

						f1 = 0.3F;
						AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand( (double) f1, (double) f1, (double) f1 );
						MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept( vec3, vec31 );

						if ( movingobjectposition1 != null )
						{
							double nd = vec3.squareDistanceTo( movingobjectposition1.hitVec );

							if ( nd < Closeest )
							{
								entity = entity1;
								Closeest = nd;
							}
						}
					}
				}
			}
		}

		MovingObjectPosition pos = null;
		Vec3 Srec = null;

		if ( hitBlocks )
		{
			Srec = Vec3.createVectorHelper( d0, d1, d2 );
			pos = w.rayTraceBlocks( vec3, vec31, true );
		}

		if ( entity != null && pos != null && pos.hitVec.squareDistanceTo( Srec ) > Closeest )
		{
			pos = new MovingObjectPosition( entity );
		}
		else if ( entity != null && pos == null )
		{
			pos = new MovingObjectPosition( entity );
		}

		return pos;
	}

	public static long nanoTime()
	{
		// if ( Configuration.instance.enableNetworkProfiler )
		// return System.nanoTime();
		return 0;
	}

	public static <StackType extends IAEStack> StackType poweredExtraction(IEnergySource energy, IMEInventory<StackType> cell, StackType request,
			BaseActionSource src)
	{
		StackType possible = cell.extractItems( (StackType) request.copy(), Actionable.SIMULATE, src );

		long retrieved = 0;
		if ( possible != null )
			retrieved = possible.getStackSize();

		double availablePower = energy.extractAEPower( retrieved, Actionable.SIMULATE, PowerMultiplier.CONFIG );

		long itemToExtract = Math.min( (long) (availablePower + 0.9), retrieved );

		if ( itemToExtract > 0 )
		{
			energy.extractAEPower( retrieved, Actionable.MODULATE, PowerMultiplier.CONFIG );

			possible.setStackSize( itemToExtract );
			StackType ret = cell.extractItems( possible, Actionable.MODULATE, src );

			return ret;
		}

		return null;
	}

	public static <StackType extends IAEStack> StackType poweredInsert(IEnergySource energy, IMEInventory<StackType> cell, StackType input, BaseActionSource src)
	{
		StackType possible = cell.injectItems( (StackType) input.copy(), Actionable.SIMULATE, src );

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
				StackType split = (StackType) input.copy();
				split.decStackSize( itemToAdd );
				input.setStackSize( itemToAdd );
				split.add( cell.injectItems( input, Actionable.MODULATE, src ) );
				return split;
			}

			StackType ret = cell.injectItems( input, Actionable.MODULATE, src );

			return ret;
		}

		return input;
	}

	public static void postChanges(IStorageGrid gs, ItemStack removed, ItemStack added, BaseActionSource src)
	{
		if ( removed != null )
		{
			IMEInventory<IAEItemStack> myItems = AEApi.instance().registries().cell().getCellInventory( removed, StorageChannel.ITEMS );

			if ( myItems != null )
			{
				for (IAEItemStack is : myItems.getAvailableItems( AEApi.instance().storage().createItemList() ))
				{
					is.setStackSize( -is.getStackSize() );
					gs.postAlterationOfStoredItems( StorageChannel.ITEMS, is, src );
				}
			}

			IMEInventory<IAEFluidStack> myFluids = AEApi.instance().registries().cell().getCellInventory( removed, StorageChannel.FLUIDS );

			if ( myFluids != null )
			{
				for (IAEFluidStack is : myFluids.getAvailableItems( AEApi.instance().storage().createFluidList() ))
				{
					is.setStackSize( -is.getStackSize() );
					gs.postAlterationOfStoredItems( StorageChannel.FLUIDS, is, src );
				}
			}
		}

		if ( added != null )
		{
			IMEInventory<IAEItemStack> myItems = AEApi.instance().registries().cell().getCellInventory( added, StorageChannel.ITEMS );

			if ( myItems != null )
			{
				for (IAEItemStack is : myItems.getAvailableItems( new ItemList( IAEItemStack.class ) ))
				{
					gs.postAlterationOfStoredItems( StorageChannel.ITEMS, is, src );
				}
			}

			IMEInventory<IAEFluidStack> myFluids = AEApi.instance().registries().cell().getCellInventory( added, StorageChannel.FLUIDS );

			if ( myFluids != null )
			{
				for (IAEFluidStack is : myFluids.getAvailableItems( new ItemList( IAEFluidStack.class ) ))
				{
					gs.postAlterationOfStoredItems( StorageChannel.FLUIDS, is, src );
				}
			}
		}
	}

	static public <T extends IAEStack<T>> void postListChanges(IItemList<T> before, IItemList<T> after, IMEMonitorHandlerReceiver<T> meMonitorPassthu,
			BaseActionSource source)
	{
		for (T is : before)
			is.setStackSize( -is.getStackSize() );

		for (T is : after)
			before.add( is );

		for (T is : before)
		{
			if ( is.getStackSize() != 0 )
			{
				meMonitorPassthu.postChange( null, is, source );
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
			else if ( targ.adjacentChestZPos != null )
				hash ^= targ.adjacentChestZPos.hashCode();
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
						int c = (Side << (offset++ % 8)) ^ (1 << dir.ordinal());
						hash = c + (hash << 6) + (hash << 16) - hash;
					}
				}
			}
		}

		return hash;
	}

	public static boolean securityCheck(GridNode a, GridNode b)
	{
		if ( a.lastSecurityKey == -1 && b.lastSecurityKey == -1 )
			return false;
		else if ( a.lastSecurityKey == b.lastSecurityKey )
			return false;

		boolean a_isSecure = isPowered( a.getGrid() ) && a.lastSecurityKey != -1;
		boolean b_isSecure = isPowered( b.getGrid() ) && b.lastSecurityKey != -1;

		// can't do that son...
		if ( a_isSecure && b_isSecure )
			return true;

		if ( !a_isSecure && b_isSecure )
			return checkPlayerPermissions( b.getGrid(), a.playerID );

		if ( a_isSecure && !b_isSecure )
			return checkPlayerPermissions( a.getGrid(), b.playerID );

		return false;
	}

	private static boolean isPowered(IGrid grid)
	{
		if ( grid == null )
			return false;

		IEnergyGrid eg = (IEnergyGrid) grid.getCache( IEnergyGrid.class );
		return eg.isNetworkPowered();
	}

	private static boolean checkPlayerPermissions(IGrid grid, int playerID)
	{
		if ( grid == null )
			return false;

		ISecurityGrid gs = (ISecurityGrid) grid.getCache( ISecurityGrid.class );

		if ( gs == null )
			return false;

		if ( !gs.isAvailable() )
			return false;

		return !gs.hasPermission( playerID, SecurityPermissions.BUILD );
	}

	public static boolean isDrawing(Tessellator tess)
	{
		return false;
	}

	public static void configurePlayer(EntityPlayer player, ForgeDirection side, TileEntity tile)
	{
		float pitch = 0.0f, yaw = 0.0f;
		player.yOffset = 1.8f;

		switch (side)
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

		player.posX = (float) tile.xCoord + 0.5;
		player.posY = (float) tile.yCoord + 0.5;
		player.posZ = (float) tile.zCoord + 0.5;

		player.rotationPitch = player.prevCameraPitch = player.cameraPitch = pitch;
		player.rotationYaw = player.prevCameraYaw = player.cameraYaw = yaw;
	}

	public static boolean canAccess(AENetworkProxy gridProxy, BaseActionSource src)
	{
		try
		{
			if ( src.isPlayer() )
			{
				return gridProxy.getSecurity().hasPermission( ((PlayerSource) src).player, SecurityPermissions.BUILD );
			}
			else if ( src.isMachine() )
			{
				IActionHost te = ((MachineSource) src).via;
				IGridNode n = te.getActionableNode();
				if ( n == null )
					return false;

				int playerID = n.getPlayerID();
				return gridProxy.getSecurity().hasPermission( playerID, SecurityPermissions.BUILD );
			}
			else
				return false;
		}
		catch (GridAccessException gae)
		{
			return false;
		}
	}

	public static ItemStack extractItemsByRecipe(IEnergySource energySrc, BaseActionSource mySrc, IMEMonitor<IAEItemStack> src, World w, IRecipe r,
			ItemStack output, InventoryCrafting ci, ItemStack providedTemplate, int slot, IItemList<IAEItemStack> aitems)
	{
		if ( energySrc.extractAEPower( 1, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > 0.9 )
		{
			if ( providedTemplate == null )
				return null;

			AEItemStack ae_req = AEItemStack.create( providedTemplate );
			ae_req.setStackSize( 1 );

			IAEItemStack ae_ext = src.extractItems( ae_req, Actionable.MODULATE, mySrc );
			if ( ae_ext != null )
			{
				ItemStack extracted = ae_ext.getItemStack();
				if ( extracted != null )
				{
					energySrc.extractAEPower( 1, Actionable.MODULATE, PowerMultiplier.CONFIG );
					return extracted;
				}
			}

			if ( aitems != null && (ae_req.isOre() || providedTemplate.hasTagCompound() || providedTemplate.isItemStackDamageable()) )
			{
				for (IAEItemStack x : aitems)
				{
					ItemStack sh = x.getItemStack();
					if ( (Platform.isSameItemType( providedTemplate, sh ) || ae_req.sameOre( x )) && !Platform.isSameItem( sh, output ) )
					{ // Platform.isSameItemType( sh, providedTemplate )
						ItemStack cp = Platform.cloneItemStack( sh );
						cp.stackSize = 1;
						ci.setInventorySlotContents( slot, cp );
						if ( r.matches( ci, w ) && Platform.isSameItem( r.getCraftingResult( ci ), output ) )
						{
							IAEItemStack ex = src.extractItems( AEItemStack.create( cp ), Actionable.MODULATE, mySrc );
							if ( ex != null )
							{
								energySrc.extractAEPower( 1, Actionable.MODULATE, PowerMultiplier.CONFIG );
								return ex.getItemStack();
							}
						}
						ci.setInventorySlotContents( slot, providedTemplate );
					}
				}
			}

		}
		return null;
	}

	public static ItemStack getContainerItem(ItemStack stackInSlot)
	{
		if ( stackInSlot == null )
			return null;

		Item i = stackInSlot.getItem();
		if ( i == null || !i.hasContainerItem( stackInSlot ) )
		{
			if ( stackInSlot.stackSize > 1 )
			{
				stackInSlot.stackSize--;
				return stackInSlot;
			}
			return null;
		}

		ItemStack ci = i.getContainerItem( stackInSlot.copy() );
		if ( ci.isItemStackDamageable() && ci.getItemDamage() == ci.getMaxDamage() )
			ci = null;

		return ci;
	}

}
