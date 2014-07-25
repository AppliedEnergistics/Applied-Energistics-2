package appeng.integration.abstraction;

import net.minecraft.item.ItemStack;

public interface IMekanism
{

	void addCrusherRecipe(ItemStack in, ItemStack out);

	void addEnrichmentChamberRecipe(ItemStack in, ItemStack out);

}
