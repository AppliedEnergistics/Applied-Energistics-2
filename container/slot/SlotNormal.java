package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

public class SlotNormal extends AppEngSlot
{

	public SlotNormal(Container _c, EntityPlayer _p, IInventory par1iInventory, int par2, int par3, int par4) {
		super( par1iInventory, par2, par3, par4 );
	}

}
