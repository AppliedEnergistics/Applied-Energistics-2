package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerInscriber;
import appeng.core.localization.GuiText;
import appeng.tile.misc.TileInscriber;

public class GuiInscriber extends AEBaseGui
{

	public GuiInscriber(InventoryPlayer inventoryPlayer, TileInscriber te) {
		super( new ContainerInscriber( inventoryPlayer, te ) );
		this.ySize = 176;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/inscriber.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( GuiText.Inscriber.getLocal(), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
