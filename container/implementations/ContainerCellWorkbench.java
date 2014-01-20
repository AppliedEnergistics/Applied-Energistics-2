package appeng.container.implementations;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.slot.OptionalSlotRestrictedInput;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.inventory.AppEngNullInventory;
import appeng.tile.misc.TileCellWorkbench;
import appeng.util.Platform;
import appeng.util.item.ItemList;
import appeng.util.iterators.NullIterator;

public class ContainerCellWorkbench extends ContainerUpgradeable
{

	TileCellWorkbench workBench;
	AppEngNullInventory ni = new AppEngNullInventory();

	public IInventory getCellUpgradeInventory()
	{
		IInventory ri = workBench.getCellUpgradeInventory();
		return ri == null ? ni : ri;
	}

	public void setFuzzy(FuzzyMode valueOf)
	{
		ICellWorkbenchItem cwi = workBench.getCell();
		if ( cwi != null )
			cwi.setFuzzyMode( workBench.getInventoryByName( "cell" ).getStackInSlot( 0 ), valueOf );
	}

	private FuzzyMode getFuzzyMode()
	{
		ICellWorkbenchItem cwi = workBench.getCell();
		if ( cwi != null )
			return cwi.getFuzzyMode( workBench.getInventoryByName( "cell" ).getStackInSlot( 0 ) );
		return FuzzyMode.IGNORE_ALL;
	}

	class Upgrades implements IInventory
	{

		@Override
		public int getSizeInventory()
		{
			return getCellUpgradeInventory().getSizeInventory();
		}

		@Override
		public ItemStack getStackInSlot(int i)
		{
			return getCellUpgradeInventory().getStackInSlot( i );
		}

		@Override
		public ItemStack decrStackSize(int i, int j)
		{
			IInventory inv = getCellUpgradeInventory();
			ItemStack is = inv.decrStackSize( i, j );
			inv.onInventoryChanged();
			return is;
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int i)
		{
			IInventory inv = getCellUpgradeInventory();
			ItemStack is = inv.getStackInSlotOnClosing( i );
			inv.onInventoryChanged();
			return is;
		}

		@Override
		public void setInventorySlotContents(int i, ItemStack itemstack)
		{
			IInventory inv = getCellUpgradeInventory();
			inv.setInventorySlotContents( i, itemstack );
			inv.onInventoryChanged();
		}

		@Override
		public String getInvName()
		{
			return "Upgrades";
		}

		@Override
		public boolean isInvNameLocalized()
		{
			return false;
		}

		@Override
		public int getInventoryStackLimit()
		{
			return 1;
		}

		@Override
		public void onInventoryChanged()
		{

		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer entityplayer)
		{
			return false;
		}

		@Override
		public void openChest()
		{
		}

		@Override
		public void closeChest()
		{
		}

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return getCellUpgradeInventory().isItemValidForSlot( i, itemstack );
		}
	};

	IInventory UpgradeInventoryWrapper;

	ItemStack prevStack = null;
	int lastUpgrades = 0;

	public ContainerCellWorkbench(InventoryPlayer ip, TileCellWorkbench te) {
		super( ip, te );
		workBench = te;
	}

	@Override
	protected int getHeight()
	{
		return 251;
	}

	@Override
	public int availableUpgrades()
	{
		ItemStack is = workBench.getInventoryByName( "cell" ).getStackInSlot( 0 );
		if ( prevStack != is )
		{
			prevStack = is;
			return lastUpgrades = getCellUpgradeInventory().getSizeInventory();
		}
		return lastUpgrades;
	}

	@Override
	public boolean isSlotEnabled(int idx)
	{
		return idx < availableUpgrades();
	}

	@Override
	protected void setupConfig()
	{
		int x = 8;
		int y = 29;
		int offset = 0;

		IInventory cell = myte.getInventoryByName( "cell" );
		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.WORKBENCH_CELL, cell, 0, 152, 8 ) );

		IInventory inv = myte.getInventoryByName( "config" );
		UpgradeInventoryWrapper = new Upgrades();// Platform.isServer() ? new Upgrades() : new AppEngInternalInventory(
													// null, 3 * 8 );

		for (int w = 0; w < 7; w++)
			for (int z = 0; z < 9; z++)
				addSlotToContainer( new SlotFakeTypeOnly( inv, offset++, x + z * 18, y + w * 18 ) );

		for (int zz = 0; zz < 3; zz++)
			for (int z = 0; z < 8; z++)
			{
				int iSLot = zz * 8 + z;
				addSlotToContainer( new OptionalSlotRestrictedInput( PlaceableItemType.UPGRADES, UpgradeInventoryWrapper, this, iSLot, 187 + zz * 18,
						8 + 18 * z, iSLot ) );
			}
		/*
		 * if ( supportCapacity() ) { for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new
		 * OptionalSlotFakeTypeOnly( inv, this, offset++, x, y, z, w, 1 ) );
		 * 
		 * for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new OptionalSlotFakeTypeOnly(
		 * inv, this, offset++, x, y, z, w + 2, 2 ) );
		 * 
		 * for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new OptionalSlotFakeTypeOnly(
		 * inv, this, offset++, x, y, z, w + 4, 3 ) ); }
		 */
	}

	ItemStack LastCell;

	@Override
	public void detectAndSendChanges()
	{
		ItemStack is = workBench.getInventoryByName( "cell" ).getStackInSlot( 0 );
		if ( Platform.isServer() )
		{
			for (int i = 0; i < this.crafters.size(); ++i)
			{
				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				if ( this.fzMode != getFuzzyMode() )
				{
					icrafting.sendProgressBarUpdate( this, 1, (int) getFuzzyMode().ordinal() );
				}

				if ( prevStack != is )
				{
					// if the bars changed an item was probably made, so just send shit!
					for (Object s : inventorySlots)
					{
						if ( s instanceof OptionalSlotRestrictedInput )
						{
							OptionalSlotRestrictedInput sri = (OptionalSlotRestrictedInput) s;
							icrafting.sendSlotContents( this, sri.slotNumber, sri.getStack() );
						}
					}
					((EntityPlayerMP) icrafting).playerInventoryBeingManipulated = false;
				}
			}

			this.fzMode = (FuzzyMode) getFuzzyMode();
		}

		prevStack = is;
		standardDetectAndSendChanges();
	}

	public void clear()
	{
		IInventory inv = myte.getInventoryByName( "config" );
		for (int x = 0; x < inv.getSizeInventory(); x++)
			inv.setInventorySlotContents( x, null );
		detectAndSendChanges();
	}

	public void partition()
	{
		IInventory inv = myte.getInventoryByName( "config" );

		IMEInventory<IAEItemStack> cellInv = AEApi.instance().registries().cell()
				.getCellInventory( myte.getInventoryByName( "cell" ).getStackInSlot( 0 ), StorageChannel.ITEMS );

		Iterator<IAEItemStack> i = new NullIterator<IAEItemStack>();
		if ( cellInv != null )
		{
			IItemList<IAEItemStack> list = cellInv.getAvailableItems( new ItemList() );
			i = list.iterator();
		}

		for (int x = 0; x < inv.getSizeInventory(); x++)
		{
			if ( i.hasNext() )
			{
				ItemStack g = i.next().getItemStack();
				g.stackSize = 1;
				inv.setInventorySlotContents( x, g );
			}
			else
				inv.setInventorySlotContents( x, null );
		}

		detectAndSendChanges();
	}

}
