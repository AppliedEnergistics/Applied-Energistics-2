package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.container.implementations.ContainerMEPortableCell;

public class GuiMEPortableCell extends GuiMEMonitorable
{

	public GuiMEPortableCell(InventoryPlayer inventoryPlayer, IPortableCell te) {
		super( inventoryPlayer, te, new ContainerMEPortableCell( inventoryPlayer, null ) );
	}

	int defaultGetMaxRows()
	{
		return super.getMaxRows();
	}

	@Override
	int getMaxRows()
	{
		return 3;
	}
}
