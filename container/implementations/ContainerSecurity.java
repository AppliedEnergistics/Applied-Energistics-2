package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.tile.misc.TileSecurity;

public class ContainerSecurity extends AEBaseContainer
{

	TileSecurity myte;

	public ContainerSecurity(InventoryPlayer ip, TileSecurity te) {
		super( ip, te, null );
		myte = te;

		bindPlayerInventory( ip, 0, 199 - /* height of playerinventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.SECURITY, true );

		super.detectAndSendChanges();
	}
}
