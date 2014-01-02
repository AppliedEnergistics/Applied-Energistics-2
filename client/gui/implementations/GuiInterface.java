package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerInterface;
import appeng.core.localization.GuiText;
import appeng.tile.misc.TileInterface;

public class GuiInterface extends AEBaseGui
{

	public GuiInterface(InventoryPlayer inventoryPlayer, TileInterface te) {
		super( new ContainerInterface( inventoryPlayer, te ) );
		this.ySize = 211;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/interface.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( GuiText.Interface.getLocal(), 8, 6, 4210752 );

		fontRenderer.drawString( GuiText.Config.getLocal(), 18, 6 + 11 + 7, 4210752 );
		fontRenderer.drawString( GuiText.StoredItems.getLocal(), 18, 6 + 60 + 7, 4210752 );
		fontRenderer.drawString( GuiText.Patterns.getLocal(), 8, 6 + 73 + 7, 4210752 );

		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
