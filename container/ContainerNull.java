package appeng.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/*
 * Totaly useless container that does nothing.
 */
public class ContainerNull extends Container
{

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return false;
	}

}
