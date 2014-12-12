package appeng.api.util;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;

public interface ICommonTile
{

	/**
	 * implemented on AE's Tile Entities, Gets a list of drops that the entity will normally drop, this doesn't include
	 * the block itself.
	 * 
	 * @param world world of tile entity
	 * @param x x pos of tile entity
	 * @param y y pos of tile entity
	 * @param z z pos of tile entity
	 * @param drops drops of tile entity
	 */
	void getDrops(World world, int x, int y, int z, ArrayList<ItemStack> drops);

}