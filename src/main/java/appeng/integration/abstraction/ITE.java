package appeng.integration.abstraction;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface ITE
{

	void addPulverizerRecipe(int i, ItemStack blkQuartz, ItemStack blockDust);

	void addPulverizerRecipe(int i, ItemStack blkQuartzOre, ItemStack matQuartz, ItemStack matQuartzDust);

	boolean isPipe(TileEntity te, ForgeDirection opposite);

	ItemStack addItemsToPipe(TileEntity ad, ItemStack itemstack, ForgeDirection dir);

}
