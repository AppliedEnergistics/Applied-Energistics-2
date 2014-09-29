package appeng.items.storage;

import java.util.EnumSet;

import appeng.api.config.ModMode;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;

public class ItemCreativeStorageCell extends AEBaseItem implements ICellWorkbenchItem
{

	public ItemCreativeStorageCell() {
		super( ItemCreativeStorageCell.class );
		setFeature( EnumSet.of( AEFeature.StorageCells, AEFeature.Creative ) );
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
		return null;
	}

	@Override
	public IInventory getConfigInventory(ItemStack is)
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is)
	{
		return FuzzyMode.IGNORE_ALL;
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode)
	{

	}

	@Override
	public ModMode getModMode(ItemStack is)
	{
		return ModMode.FILTER_BY_ITEM;
	}

	@Override
	public void setModMode(ItemStack is, ModMode mmMode)
	{

	}
}
