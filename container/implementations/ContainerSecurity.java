package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.SecurityPermissions;
import appeng.api.storage.IStorageMonitorable;

public class ContainerSecurity extends ContainerMEMonitorable
{

	public ContainerSecurity(InventoryPlayer ip, IStorageMonitorable montiorable) {
		super( ip, montiorable );
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.SECURITY, true );

		super.detectAndSendChanges();
	}
}
