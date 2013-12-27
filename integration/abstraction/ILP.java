package appeng.integration.abstraction;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.storage.IMEInventory;

public interface ILP
{

	List<ItemStack> getCraftedItems(TileEntity te);

	List<ItemStack> getProvidedItems(TileEntity te);

	boolean isRequestPipe(TileEntity te);

	List<ItemStack> performRequest(TileEntity te, ItemStack wanted);

	IMEInventory getInv(TileEntity te);

	Object getGetPowerPipe(TileEntity te);

	boolean isPowerSource(TileEntity tt);

	boolean canUseEnergy(Object pp, int ceil, List<Object> providersToIgnore);

	boolean useEnergy(Object pp, int ceil, List<Object> providersToIgnore);

}
