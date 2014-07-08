package appeng.util;

import java.util.concurrent.Callable;

import net.minecraft.world.World;

public class BlockUpdate implements Callable
{

	final World w;
	final int x, y, z;

	public BlockUpdate(World w, int x, int y, int z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public Object call() throws Exception
	{
		w.notifyBlocksOfNeighborChange( x, y, z, Platform.air );
		return true;
	}

}
