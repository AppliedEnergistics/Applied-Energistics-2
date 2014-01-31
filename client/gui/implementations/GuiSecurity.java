package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.storage.IStorageMonitorable;

public class GuiSecurity extends GuiMEMonitorable
{

	public GuiSecurity(InventoryPlayer inventoryPlayer, IStorageMonitorable te) {
		super( inventoryPlayer, te );
		perRow = 5;
		xoffset = 81;
	}

	protected String getBackground()
	{
		return "guis/security.png";
	}

}
