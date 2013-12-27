package appeng.core.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;

public class ApiStorage implements IStorageHelper
{

	@Override
	public IAEItemStack createItemStack(ItemStack is)
	{
		return AEItemStack.create( is );
	}

	@Override
	public IAEFluidStack createFluidStack(FluidStack is)
	{
		return AEFluidStack.create( is );
	}

	@Override
	public IItemList createItemList()
	{
		return new ItemList();
	}

}
