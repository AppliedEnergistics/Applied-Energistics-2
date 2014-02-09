package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.core.Configuration;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;

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
			isContainerValid = false;
			getPlayerInv().player.addChatMessage( PlayerMessages.OutOfRange.get() );
		}
		else
		{
			powerMultiplier = Configuration.instance.wireless_getDrainRate( wtgo.getRange() );
		}
	}
}
