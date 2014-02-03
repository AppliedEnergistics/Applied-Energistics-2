package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
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
			getPlayerInv().player.closeScreen();
			getPlayerInv().player.sendChatToPlayer( PlayerMessages.OutOfRange.get() );
		}
	}
}
