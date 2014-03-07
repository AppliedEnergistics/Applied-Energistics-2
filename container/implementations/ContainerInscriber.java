package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.container.AEBaseContainer;
import appeng.tile.misc.TileInscriber;

public class ContainerInscriber extends AEBaseContainer
{

	TileInscriber myte;

	public ContainerInscriber(InventoryPlayer ip, TileInscriber te) {
		super( ip, te, null );
		myte = te;

		bindPlayerInventory( ip, 0, 176 - /* height of playerinventory */82 );
	}

}
