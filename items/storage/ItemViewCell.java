package appeng.items.storage;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.IPartitionList;
import appeng.util.prioitylist.MergedPriorityList;
import appeng.util.prioitylist.PrecisePriorityList;

public class ItemViewCell extends AEBaseItem implements ICellWorkbenchItem
{

	public ItemViewCell()
	{
		super( ItemViewCell.class );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setMaxStackSize( 1 );
	}

	@Override
	public boolean isEditable(ItemStack is)
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory(ItemStack is)
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory(ItemStack is)
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is)
	{
		String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch (Throwable t)
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode)
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	public static IPartitionList<IAEItemStack> createFilter(ItemStack[] list)
	{
		IPartitionList<IAEItemStack> myPartitionList = null;

		MergedPriorityList<IAEItemStack> myMergedList = new MergedPriorityList<IAEItemStack>();

		for (ItemStack currentViewCell : list)
		{
			if ( currentViewCell == null )
				continue;

			if ( (currentViewCell.getItem() instanceof ItemViewCell) )
			{
				boolean hasInverter = false;
				boolean hasFuzzy = false;
				IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();

				ItemViewCell vc = (ItemViewCell) currentViewCell.getItem();
				IInventory upgrades = vc.getUpgradesInventory( currentViewCell );
				IInventory config = vc.getConfigInventory( currentViewCell );
				FuzzyMode fzMode = vc.getFuzzyMode( currentViewCell );

				hasInverter = false;
				hasFuzzy = false;

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

				if ( !priorityList.isEmpty() )
				{
					if ( hasFuzzy )
						myMergedList.addNewList( new FuzzyPriorityList<IAEItemStack>( priorityList, fzMode ), !hasInverter );
					else
						myMergedList.addNewList( new PrecisePriorityList<IAEItemStack>( priorityList ), !hasInverter );

					myPartitionList = myMergedList;
				}
			}
		}

		return myPartitionList;
	}
}
