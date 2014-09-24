package appeng.integration.abstraction;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.storage.IMEInventory;

public interface IFZ
{

	ItemStack barrelGetItem(TileEntity te);

	int barrelGetMaxItemCount(TileEntity te);

	int barrelGetItemCount(TileEntity te);

	void setItemType(TileEntity te, ItemStack input);

	void barrelSetCount(TileEntity te, int max);

	IMEInventory getFactorizationBarrel(TileEntity te);

	boolean isBarrel(TileEntity te);

	void grinderRecipe(ItemStack is, ItemStack itemStack);

}
