package appeng.me.storage;

import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class CellInventory implements ICellInventory
{

	static final String ITEM_TYPE_TAG = "it";
	static final String ITEM_COUNT_TAG = "ic";
	static final String ITEM_SLOT = "#";
	static final String ITEM_SLOTCOUNT = "@";
	static final String ITEM_PRE_FORMATTED_COUNT = "PF";
	static final String ITEM_PRE_FORMATTED_SLOT = "PF#";
	static final String ITEM_PRE_FORMATTED_NAME = "PN";
	static final String ITEM_PRE_FORMATTED_FUZZY = "FP";

	static protected String[] ITEM_SLOT_ARR;
	static protected String[] ITEM_SLOTCOUNT_ARR;

	final protected NBTTagCompound tagCompound;
	protected int MAX_ITEM_TYPES = 63;
	protected short storedItems = 0;
	protected int storedItemCount = 0;
	protected IItemList<IAEItemStack> cellItems;

	protected ItemStack i;
	protected IStorageCell CellType;

	final protected ISaveProvider container;

	protected CellInventory(NBTTagCompound data, ISaveProvider container) {
		tagCompound = data;
		this.container = container;
	}

	protected void loadCellItems()
	{
		if ( cellItems == null )
			cellItems = AEApi.instance().storage().createItemList();

		cellItems.resetStatus(); // clears totals and stuff.

		int types = (int) getStoredItemTypes();

		for (int x = 0; x < types; x++)
		{
			ItemStack t = ItemStack.loadItemStackFromNBT( tagCompound.getCompoundTag( ITEM_SLOT_ARR[x] ) );
			if ( t != null )
			{
				t.stackSize = tagCompound.getInteger( ITEM_SLOTCOUNT_ARR[x] );

				if ( t.stackSize > 0 )
				{
					cellItems.add( AEItemStack.create( t ) );
				}
			}
		}

		// cellItems.clean();
	}

	void saveChanges()
	{
		// cellItems.clean();
		int itemCount = 0;

		// add new pretty stuff...
		int x = 0;
		Iterator<IAEItemStack> i = cellItems.iterator();
		while (i.hasNext())
		{
			IAEItemStack v = i.next();
			itemCount += v.getStackSize();

			NBTBase c = tagCompound.getTag( ITEM_SLOT_ARR[x] );
			if ( c instanceof NBTTagCompound )
				v.writeToNBT( (NBTTagCompound) c );
			else
			{
				NBTTagCompound g = new NBTTagCompound();
				v.writeToNBT( g );
				tagCompound.setTag( ITEM_SLOT_ARR[x], g );
			}

			/*
			 * NBTBase tagSlotCount = tagCompound.getTag( ITEM_SLOTCOUNT_ARR[x] ); if ( tagSlotCount instanceof
			 * NBTTagInt ) ((NBTTagInt) tagSlotCount).data = (int) v.getStackSize(); else
			 */
			tagCompound.setInteger( ITEM_SLOTCOUNT_ARR[x], (int) v.getStackSize() );

			x++;
		}

		// NBTBase tagType = tagCompound.getTag( ITEM_TYPE_TAG );
		// NBTBase tagCount = tagCompound.getTag( ITEM_COUNT_TAG );
		short oldStoreditems = storedItems;

		/*
		 * if ( tagType instanceof NBTTagShort ) ((NBTTagShort) tagType).data = storedItems = (short) cellItems.size();
		 * else
		 */
		tagCompound.setShort( ITEM_TYPE_TAG, storedItems = (short) cellItems.size() );

		/*
		 * if ( tagCount instanceof NBTTagInt ) ((NBTTagInt) tagCount).data = storedItemCount = itemCount; else
		 */
		tagCompound.setInteger( ITEM_COUNT_TAG, storedItemCount = itemCount );

		// clean any old crusty stuff...
		for (; x < oldStoreditems && x < MAX_ITEM_TYPES; x++)
		{
			tagCompound.removeTag( ITEM_SLOT_ARR[x] );
			tagCompound.removeTag( ITEM_SLOTCOUNT_ARR[x] );
		}

		if ( container != null )
			container.saveChanges( this );
	}

	protected CellInventory(ItemStack o, ISaveProvider container) throws AppEngException {
		if ( ITEM_SLOT_ARR == null )
		{
			ITEM_SLOT_ARR = new String[MAX_ITEM_TYPES];
			ITEM_SLOTCOUNT_ARR = new String[MAX_ITEM_TYPES];

			for (int x = 0; x < MAX_ITEM_TYPES; x++)
			{
				ITEM_SLOT_ARR[x] = ITEM_SLOT + x;
				ITEM_SLOTCOUNT_ARR[x] = ITEM_SLOTCOUNT + x;
			}
		}

		if ( o == null )
		{
			throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
		}

		CellType = null;
		i = o;

		Item type = i.getItem();
		if ( type instanceof IStorageCell )
		{
			CellType = (IStorageCell) i.getItem();
			MAX_ITEM_TYPES = CellType.getTotalTypes( i );
		}

		if ( CellType == null )
		{
			throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
		}

		if ( !CellType.isStorageCell( i ) )
		{
			throw new AppEngException( "ItemStack was used as a cell, but was not a cell!" );
		}

		if ( MAX_ITEM_TYPES > 63 )
			MAX_ITEM_TYPES = 63;
		if ( MAX_ITEM_TYPES < 1 )
			MAX_ITEM_TYPES = 1;

		this.container = container;
		tagCompound = Platform.openNbtData( o );
		storedItems = tagCompound.getShort( ITEM_TYPE_TAG );
		storedItemCount = tagCompound.getInteger( ITEM_COUNT_TAG );
		cellItems = null;
	}

	IItemList<IAEItemStack> getCellItems()
	{
		if ( cellItems == null )
		{
			cellItems = AEApi.instance().storage().createItemList();
			loadCellItems();
		}

		return cellItems;
	}

	@Override
	public int getBytesPerType()
	{
		return CellType.BytePerType( i );
	}

	@Override
	public boolean canHoldNewItem()
	{
		long bytesFree = getFreeBytes();
		return (bytesFree > getBytesPerType() || (bytesFree == getBytesPerType() && getUnusedItemCount() > 0)) && getRemainingItemTypes() > 0;
	}

	public static IMEInventoryHandler getCell(ItemStack o, ISaveProvider container2)
	{
		try
		{
			return new CellInventoryHandler( new CellInventory( o, container2 ) );
		}
		catch (AppEngException e)
		{
			return null;
		}
	}

	private static boolean isStorageCell(ItemStack i)
	{
		if ( i == null )
		{
			return false;
		}

		try
		{
			Item type = i.getItem();
			if ( type instanceof IStorageCell )
			{
				return !((IStorageCell) type).storableInStorageCell();
			}
		}
		catch (Throwable err)
		{
			return true;
		}

		return false;
	}

	public static boolean isCell(ItemStack i)
	{
		if ( i == null )
		{
			return false;
		}

		Item type = i.getItem();
		if ( type instanceof IStorageCell )
		{
			return ((IStorageCell) type).isStorageCell( i );
		}

		return false;
	}

	@Override
	public long getTotalBytes()
	{
		return CellType.getBytes( i );
	}

	@Override
	public long getFreeBytes()
	{
		return getTotalBytes() - getUsedBytes();
	}

	@Override
	public long getUsedBytes()
	{
		long bytesForItemCount = (getStoredItemCount() + getUnusedItemCount()) / 8;
		return getStoredItemTypes() * getBytesPerType() + bytesForItemCount;
	}

	@Override
	public long getTotalItemTypes()
	{
		return MAX_ITEM_TYPES;
	}

	@Override
	public long getStoredItemTypes()
	{
		return storedItems;
	}

	@Override
	public long getStoredItemCount()
	{
		return storedItemCount;
	}

	private void updateItemCount(long delta)
	{
		tagCompound.setInteger( ITEM_COUNT_TAG, storedItemCount = (int) (storedItemCount + delta) );
	}

	@Override
	public long getRemainingItemTypes()
	{
		long basedOnStorage = getFreeBytes() / getBytesPerType();
		long baseOnTotal = getTotalItemTypes() - getStoredItemTypes();
		return basedOnStorage > baseOnTotal ? baseOnTotal : basedOnStorage;
	}

	@Override
	public long getRemainingItemCount()
	{
		long remaining = getFreeBytes() * 8 + getUnusedItemCount();
		return remaining > 0 ? remaining : 0;
	}

	@Override
	public int getUnusedItemCount()
	{
		int div = (int) (getStoredItemCount() % 8);

		if ( div == 0 )
		{
			return 0;
		}

		return 8 - div;
	}

	private static HashSet<Integer> blackList = new HashSet();

	public static void addBasicBlackList(int itemID, int Meta)
	{
		blackList.add( (Meta << Platform.DEF_OFFSET) | itemID );
	}

	public static boolean isBlackListed(IAEItemStack input)
	{
		if ( blackList.contains( (OreDictionary.WILDCARD_VALUE << Platform.DEF_OFFSET) | Item.getIdFromItem( input.getItem() ) ) )
			return true;
		return blackList.contains( (input.getItemDamage() << Platform.DEF_OFFSET) | Item.getIdFromItem( input.getItem() ) );
	}

	private boolean isEmpty(IMEInventory meinv)
	{
		return meinv.getAvailableItems( AEApi.instance().storage().createItemList() ).isEmpty();
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src)
	{
		if ( input == null )
			return null;
		if ( input.getStackSize() == 0 )
			return null;

		if ( isBlackListed( input ) || CellType.isBlackListed( i, input ) )
			return input;

		ItemStack sharedItemStack = input.getItemStack();

		if ( CellInventory.isStorageCell( sharedItemStack ) )
		{
			IMEInventory meinv = getCell( sharedItemStack, null );
			if ( meinv != null && !isEmpty( meinv ) )
				return input;
		}

		IAEItemStack l = getCellItems().findPrecise( input );
		if ( l != null )
		{
			long remainingItemSlots = getRemainingItemCount();
			if ( remainingItemSlots < 0 )
				return input;

			if ( input.getStackSize() > remainingItemSlots )
			{
				IAEItemStack r = input.copy();
				r.setStackSize( r.getStackSize() - remainingItemSlots );
				if ( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() + remainingItemSlots );
					updateItemCount( remainingItemSlots );
					saveChanges();
				}
				return r;
			}
			else
			{
				if ( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() + input.getStackSize() );
					updateItemCount( input.getStackSize() );
					saveChanges();
				}
				return null;
			}
		}

		if ( canHoldNewItem() ) // room for new type, and for at least one item!
		{
			int remainingItemCount = (int) getRemainingItemCount() - getBytesPerType() * 8;
			if ( remainingItemCount > 0 )
			{
				if ( input.getStackSize() > remainingItemCount )
				{
					ItemStack toReturn = Platform.cloneItemStack( sharedItemStack );
					toReturn.stackSize = sharedItemStack.stackSize - remainingItemCount;
					if ( mode == Actionable.MODULATE )
					{
						ItemStack toWrite = Platform.cloneItemStack( sharedItemStack );
						toWrite.stackSize = remainingItemCount;

						cellItems.add( AEItemStack.create( toWrite ) );
						updateItemCount( toWrite.stackSize );

						saveChanges();
					}
					return AEItemStack.create( toReturn );
				}

				if ( mode == Actionable.MODULATE )
				{
					updateItemCount( input.getStackSize() );
					cellItems.add( input );
					saveChanges();
				}

				return null;
			}
		}

		return input;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		if ( request == null )
			return null;

		ItemStack sharedItem = request.getItemStack();
		int size = sharedItem.stackSize;

		IAEItemStack Results = null;

		IAEItemStack l = getCellItems().findPrecise( request );
		if ( l != null )
		{
			Results = l.copy();

			if ( l.getStackSize() <= size )
			{
				Results.setStackSize( l.getStackSize() );
				if ( mode == Actionable.MODULATE )
				{
					updateItemCount( -l.getStackSize() );
					l.setStackSize( 0 );
					saveChanges();
				}
			}
			else
			{
				Results.setStackSize( size );
				if ( mode == Actionable.MODULATE )
				{
					l.setStackSize( l.getStackSize() - size );
					updateItemCount( -size );
					saveChanges();
				}
			}
		}

		return Results;
	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		for (IAEItemStack i : getCellItems())
			out.add( i );

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public double getIdleDrain()
	{
		return CellType.getIdleDrain();
	}

	@Override
	public FuzzyMode getFuzzyMode()
	{
		return CellType.getFuzzyMode( this.i );
	}

	@Override
	public IInventory getConfigInventory()
	{
		return CellType.getConfigInventory( this.i );
	}

	@Override
	public IInventory getUpgradesInventory()
	{
		return CellType.getUpgradesInventory( this.i );
	}

	@Override
	public int getStatusForCell()
	{
		if ( canHoldNewItem() )
			return 1;
		if ( getRemainingItemCount() > 0 )
			return 2;
		return 3;
	}

	@Override
	public ItemStack getItemStack()
	{
		return i;
	}

}
