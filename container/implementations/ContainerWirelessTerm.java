package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.Platform;

public class ContainerWirelessTerm extends ContainerMEPortableCell
{

	WirelessTerminalGuiObject wtgo;

	public ContainerWirelessTerm(InventoryPlayer ip, WirelessTerminalGuiObject montiorable) {
		super( ip, montiorable );
		wtgo = montiorable;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if ( !wtgo.rangeCheck() )
		{
			if ( Platform.isServer() && isContainerValid )
				getPlayerInv().player.addChatMessage( PlayerMessages.OutOfRange.get() );

			isContainerValid = false;
		}
		else
		{
			powerMultiplier = AEConfig.instance.wireless_getDrainRate( wtgo.getRange() );
		}
	}
}
