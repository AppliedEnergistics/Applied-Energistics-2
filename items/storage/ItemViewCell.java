package appeng.items.storage;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;

public class ItemViewCell extends AEBaseItem implements ICellWorkbenchItem
{

	public ItemViewCell() {
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
}
