package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.implementations.guiobjects.IPortableCell;

public class GuiWirelessTerm extends GuiMEPortableCell
{

	public GuiWirelessTerm(InventoryPlayer inventoryPlayer, IPortableCell te) {
		super( inventoryPlayer, te );
		maxRows = Integer.MAX_VALUE;
	}

}
