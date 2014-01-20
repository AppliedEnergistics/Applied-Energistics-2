package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.helpers.ICellItemViewer;

public class GuiMEPortableCell extends GuiMEMonitorable
{

	public GuiMEPortableCell(InventoryPlayer inventoryPlayer, ICellItemViewer te) {
		super( inventoryPlayer, te );
		maxRows = 3;
	}

}
