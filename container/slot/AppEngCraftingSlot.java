package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotCrafting;

public class AppEngCraftingSlot extends SlotCrafting
{

	public AppEngCraftingSlot(EntityPlayer par1EntityPlayer, IInventory par2iInventory, IInventory par3iInventory, int par4,
			int par5, int par6) {
		super( par1EntityPlayer, par2iInventory, par3iInventory, par4, par5, par6 );
	}

}
