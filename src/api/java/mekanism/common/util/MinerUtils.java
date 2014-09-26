package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.ListUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class MinerUtils
{
	public static List<Block> specialSilkIDs = ListUtils.asList(Blocks.ice);

	public static List<ItemStack> getDrops(World world, Coord4D obj, boolean silk)
	{
		Block block = obj.getBlock(world);

		if(block == null)
		{
			return new ArrayList<ItemStack>();
		}

		if(block.isAir(world, obj.xCoord, obj.yCoord, obj.zCoord))
		{
			return new ArrayList<ItemStack>();
		}

		int meta = obj.getMetadata(world);

		if(!silk)
		{
			return block.getDrops(world, obj.xCoord, obj.yCoord, obj.zCoord, meta, 0);
		}
		else {
			List<ItemStack> ret = new ArrayList<ItemStack>();
			ret.add(new ItemStack(block, 1, meta));

			if(specialSilkIDs.contains(block) || (block.getDrops(world, obj.xCoord, obj.yCoord, obj.zCoord, meta, 0) != null && block.getDrops(world, obj.xCoord, obj.yCoord, obj.zCoord, meta, 0).size() > 0))
			{
				return ret;
			}
		}

		return new ArrayList<ItemStack>();
	}
}
