package appeng.container.implementations;

import invtweaks.api.container.ChestContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;
import appeng.tile.storage.TileSkyChest;

@ChestContainer
public class ContainerSkyChest extends AEBaseContainer
{

	TileSkyChest chest;

	public ContainerSkyChest(InventoryPlayer ip, TileSkyChest chest) {
		super( ip, chest, null );
		this.chest = chest;

		for (int y = 0; y < 4; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				addSlotToContainer( new SlotNormal( this.chest, y * 9 + x, 8 + 18 * x, 24 + 18 * y ) );
			}
		}

		this.chest.openInventory();

		bindPlayerInventory( ip, 0, 195 - /* height of player inventory */82 );
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed( par1EntityPlayer );
		chest.closeInventory();
	}
}
