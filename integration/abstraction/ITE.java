package appeng.integration.abstraction;

import net.minecraft.item.ItemStack;

public interface ITE
{

	void addPulverizerRecipe(int i, ItemStack blkQuartz, ItemStack blockDust);

	void addPulverizerRecipe(int i, ItemStack blkQuartzOre, ItemStack matQuartz, ItemStack matQuartzDust);

}
