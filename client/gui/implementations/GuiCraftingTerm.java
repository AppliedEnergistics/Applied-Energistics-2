package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftingTerm;
import appeng.core.localization.GuiText;

public class GuiCraftingTerm extends GuiMEMonitorable
{

	public GuiCraftingTerm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( inventoryPlayer, te, new ContainerCraftingTerm( inventoryPlayer, te ) );
		reservedSpace = 73;
	}

	protected String getBackground()
	{
		return "guis/crafting.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		super.drawFG( offsetX, offsetY, mouseX, mouseY );
		fontRendererObj.drawString( GuiText.CraftingTerminal.getLocal(), 8, ySize - 96 + 1 - reservedSpace, 4210752 );
	}

}
