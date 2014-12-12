package appeng.api.implementations.items;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;

public interface IGrowableCrystal
{

	ItemStack triggerGrowth(ItemStack is);

	float getMultiplier(Block blk, Material mat);

}
