package appeng.container.implementations;

import java.util.Iterator;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.parts.misc.PartStorageBus;
import appeng.util.Platform;
import appeng.util.iterators.NullIterator;

public class ContainerStorageBus extends ContainerUpgradeable
{

	final PartStorageBus storageBus;

	@GuiSync(3)
	public AccessRestriction rwMode = AccessRestriction.READ_WRITE;

	@GuiSync(4)
	public StorageFilter storageFilter = StorageFilter.EXTRACTABLE_ONLY;

	public ContainerStorageBus(InventoryPlayer ip, PartStorageBus te) {
		super( ip, te );
		storageBus = te;
	}

	@Override
	protected int getHeight()
	{
		return 251;
	}

	@Override
	public int availableUpgrades()
	{
		return 5;
	}

	@Override
	protected boolean supportCapacity()
	{
		return true;
	}

	@Override
	public boolean isSlotEnabled(int idx)
	{
		int upgrades = upgradeable.getInstalledUpgrades( Upgrades.CAPACITY );

		return upgrades > idx;
	}

	@Override
	protected void setupConfig()
	{
		int xo = 8;
		int yo = 23 + 6;

		IInventory config = upgradeable.getInventoryByName( "config" );
		for (int y = 0; y < 7; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				if ( y < 2 )
					addSlotToContainer( new SlotFakeTypeOnly( config, y * 9 + x, xo + x * 18, yo + y * 18 ) );
				else
					addSlotToContainer( new OptionalSlotFakeTypeOnly( config, this, y * 9 + x, xo, yo, x, y, y - 2 ) );
			}
		}

		IInventory upgrades = upgradeable.getInventoryByName( "upgrades" );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 4, 187, 8 + 18 * 4, invPlayer )).setNotDraggable() );
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			this.fzMode = (FuzzyMode) this.upgradeable.getConfigManager().getSetting( Settings.FUZZY_MODE );
			this.rwMode = (AccessRestriction) this.upgradeable.getConfigManager().getSetting( Settings.ACCESS );
			this.storageFilter = (StorageFilter) this.upgradeable.getConfigManager().getSetting( Settings.STORAGE_FILTER );
		}

		standardDetectAndSendChanges();
	}

	public void clear()
	{
		IInventory inv = upgradeable.getInventoryByName( "config" );
		for (int x = 0; x < inv.getSizeInventory(); x++)
			inv.setInventorySlotContents( x, null );
		detectAndSendChanges();
	}

	public void partition()
	{
		IInventory inv = upgradeable.getInventoryByName( "config" );

		IMEInventory<IAEItemStack> cellInv = storageBus.getInternalHandler();

		Iterator<IAEItemStack> i = new NullIterator<IAEItemStack>();
		if ( cellInv != null )
		{
			IItemList<IAEItemStack> list = cellInv.getAvailableItems( AEApi.instance().storage().createItemList() );
			i = list.iterator();
		}

		for (int x = 0; x < inv.getSizeInventory(); x++)
		{
			if ( i.hasNext() && isSlotEnabled( (x / 9) - 2 ) )
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
