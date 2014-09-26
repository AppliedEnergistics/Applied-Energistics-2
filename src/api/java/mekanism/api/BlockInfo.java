package mekanism.api;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class BlockInfo
{
	public Block block;
	public int meta;

	public BlockInfo(Block b, int j)
	{
		block = b;
		meta = j;
	}

	public static BlockInfo get(ItemStack stack)
	{
		return new BlockInfo(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage());
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof BlockInfo &&
				((BlockInfo)obj).block == block &&
				((BlockInfo)obj).meta == meta;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + block.getUnlocalizedName().hashCode();
		code = 31 * code + meta;
		return code;
	}
}