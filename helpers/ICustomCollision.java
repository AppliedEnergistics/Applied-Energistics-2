package appeng.helpers;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public interface ICustomCollision
{

	Iterable<AxisAlignedBB> getSelectedBoundingBoxsFromPool(World w, int x, int y, int z);

	void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e);

}
