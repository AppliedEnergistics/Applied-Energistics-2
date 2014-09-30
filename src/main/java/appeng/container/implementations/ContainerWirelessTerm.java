package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.Platform;

public class ContainerWirelessTerm extends ContainerMEPortableCell
{

	WirelessTerminalGuiObject wirelessTerminalGUIObject;

	public ContainerWirelessTerm(InventoryPlayer ip, WirelessTerminalGuiObject wirelessTerminalGUIObject) {
		super( ip, wirelessTerminalGUIObject );
		this.wirelessTerminalGUIObject = wirelessTerminalGUIObject;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if ( !wirelessTerminalGUIObject.rangeCheck() )
		{
			if ( Platform.isServer() && isContainerValid )
				getPlayerInv().player.addChatMessage( PlayerMessages.OutOfRange.get() );

			isContainerValid = false;
		}
		else
		{
			powerMultiplier = AEConfig.instance.wireless_getDrainRate( wirelessTerminalGUIObject.getRange() );
		}
	}
}
