package appeng.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class Security
{

	public static boolean hasPermissions(TileEntity myTile, EntityPlayer player, AccessType blockAccess)
	{
		return true;
	}

}
