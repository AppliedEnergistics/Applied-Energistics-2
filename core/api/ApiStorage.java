package appeng.core.api;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
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

	@Override
	public IAEItemStack poweredExtraction(IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack request, BaseActionSource src)
	{
		return Platform.poweredExtraction( energy, cell, request, src );
	}

	@Override
	public IAEItemStack poweredInsert(IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack input, BaseActionSource src)
	{
		return Platform.poweredInsert( energy, cell, input, src );
	}

	@Override
	public IAEItemStack readItemFromPacket(DataInputStream input) throws IOException
	{
		return AEItemStack.loadItemStackFromPacket( input );
	}

	@Override
	public IAEFluidStack readFluidFromPacket(DataInputStream input) throws IOException
	{
		return AEFluidStack.loadFluidStackFromPacket( input );
	}
}
