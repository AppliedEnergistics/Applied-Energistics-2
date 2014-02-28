package appeng.container.implementations;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;
import appeng.tile.storage.TileSkyChest;

public class ContainerSkyChest extends AEBaseContainer
{

	TileSkyChest myte;

	public ContainerSkyChest(InventoryPlayer ip, TileSkyChest te) {
		super( ip, te, null );
		myte = te;

		for (int y = 0; y < 4; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				addSlotToContainer( new SlotNormal( myte, y * 9 + x, 8 + 18 * x, 24 + 18 * y ) );
			}
		}

		myte.openInventory();

		bindPlayerInventory( ip, 0, 195 - /* height of playerinventory */82 );
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed( par1EntityPlayer );
		myte.closeInventory();
	}
}
