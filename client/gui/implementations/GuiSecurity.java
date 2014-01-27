package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerSecurity;
import appeng.core.localization.GuiText;
import appeng.tile.misc.TileSecurity;

public class GuiSecurity extends AEBaseGui
{

	public GuiSecurity(InventoryPlayer inventoryPlayer, TileSecurity te) {
		super( new ContainerSecurity( inventoryPlayer, te ) );
		this.ySize = 199;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/security.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( GuiText.Security.getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
