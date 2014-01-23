package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.implementations.guiobjects.IPortableCell;

public class GuiMEPortableCell extends GuiMEMonitorable
{

	public GuiMEPortableCell(InventoryPlayer inventoryPlayer, IPortableCell te) {
		super( inventoryPlayer, te );
		maxRows = 3;
	}

}
