package appeng.me.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.PrecisePriorityList;

public class CellInventoryHandler extends MEInventoryHandler<IAEItemStack>
{

	NBTTagCompound openNbtData()
	{
		return Platform.openNbtData( getCellInv().i );
	}

	public CellInventory getCellInv()
	{
		Object o = this.internal;

		if ( o instanceof MEPassthru )
			o = ((MEPassthru) o).getInternal();

		return (CellInventory) (o instanceof CellInventory ? o : null);
	}

	CellInventoryHandler(IMEInventory c) {
		super( c, IAEItemStack.class );

		CellInventory ci = getCellInv();
		if ( ci != null )
		{
			IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();

			IInventory upgrades = ci.getUpgradesInventory();
			IInventory config = ci.getConfigInventory();
			FuzzyMode fzMode = ci.getFuzzyMode();

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
					myPartitionList = new FuzzyPriorityList<IAEItemStack>( priorityList, fzMode );
				else
					myPartitionList = new PrecisePriorityList<IAEItemStack>( priorityList );
			}
		}
	}
}
