package appeng.tile.misc;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.tile.grid.AENetworkTile;

public class TileNetworkEmitter extends AENetworkTile
{

	public TileNetworkEmitter() {
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	public void activate(EntityPlayer player)
	{
		// TODO Auto-generated method stub

	}

}
