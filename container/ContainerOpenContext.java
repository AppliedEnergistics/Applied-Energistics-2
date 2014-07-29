package appeng.container;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPart;

public class ContainerOpenContext
{

	public World w;
	public int x, y, z;
	public ForgeDirection side;
	final public boolean isItem;

	public ContainerOpenContext(Object myItem) {
		boolean isWorld = myItem instanceof IPart || myItem instanceof TileEntity;
		isItem = !isWorld;
	}

	public TileEntity getTile()
	{
		if ( isItem )
			return null;
		return w.getTileEntity( x, y, z );
	}

}
