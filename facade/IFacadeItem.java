package appeng.facade;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public interface IFacadeItem
{

	FacadePart createPartFromItemStack(ItemStack is, ForgeDirection side);

	ItemStack getTextureItem(ItemStack is);

	int getMeta(ItemStack is);

	Block getBlock(ItemStack is);

}
