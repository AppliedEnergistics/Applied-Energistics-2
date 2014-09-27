package appeng.integration.abstraction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import appeng.api.parts.IPartHost;

public interface IImmibisMicroblocks
{

	IPartHost getOrCreateHost(EntityPlayer player, int side, TileEntity te);

	/**
	 * @param te to be left tile entity
	 * @return true if this worked..
	 */
	boolean leaveParts(TileEntity te);

}
