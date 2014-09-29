package appeng.me.storage;

import appeng.api.config.ModMode;
import appeng.util.prioitylist.ModPriorityList;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.PrecisePriorityList;

public class CellInventoryHandler extends MEInventoryHandler<IAEItemStack> implements ICellInventoryHandler
{

	NBTTagCompound openNbtData()
	{
		return Platform.openNbtData( getCellInv().getItemStack() );
	}

	@Override
	public ICellInventory getCellInv()
	{
		Object o = this.internal;

		if ( o instanceof MEPassthru )
			o = ((MEPassthru) o).getInternal();

		return (ICellInventory) (o instanceof ICellInventory ? o : null);
	}

	CellInventoryHandler(IMEInventory c) {
		super( c, StorageChannel.ITEMS );

		ICellInventory ci = getCellInv();
		if ( ci != null )
		{
			IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();

			IInventory upgrades = ci.getUpgradesInventory();
			IInventory config = ci.getConfigInventory();
			FuzzyMode fzMode = ci.getFuzzyMode();
			ModMode mmMode = ci.getModMode();

			boolean hasInverter = false;
			boolean hasFuzzy = false;

			for (int x = 0; x < upgrades.getSizeInventory(); x++)
			{
				ItemStack is = upgrades.getStackInSlot( x );
				if ( is != null && is.getItem() instanceof IUpgradeModule )
				{
					Upgrades u = ((IUpgradeModule) is.getItem()).getType( is );
					if ( u != null )
					{
						switch (u)
						{
						case FUZZY:
							hasFuzzy = true;
							break;
						case INVERTER:
							hasInverter = true;
							break;
						default:
						}
					}
				}
			}

			for (int x = 0; x < config.getSizeInventory(); x++)
			{
				ItemStack is = config.getStackInSlot( x );
				if ( is != null )
					priorityList.add( AEItemStack.create( is ) );
			}

			myWhitelist = hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST;

			if ( !priorityList.isEmpty() )
			{
				if ( hasFuzzy )
					myPartitionList = mmMode == ModMode.FILTER_BY_MOD ? new ModPriorityList<IAEItemStack>( priorityList ) : new FuzzyPriorityList<IAEItemStack>( priorityList, fzMode );
				else
					myPartitionList = new PrecisePriorityList<IAEItemStack>( priorityList );
			}
		}
	}

	public boolean isPreformatted()
	{
		return ! myPartitionList.isEmpty();
	}

	public boolean isFuzzy()
	{
		return myPartitionList instanceof FuzzyPriorityList;
	}

	@Override
	public IncludeExclude getIncludeExcludeMode()
	{
		return myWhitelist;
	}

	public int getStatusForCell()
	{
			int val = getCellInv().getStatusForCell();

			if ( val == 1 && isPreformatted() )
				val = 2;

			return val;
	}

}
