package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerDrive;
import appeng.core.localization.GuiText;
import appeng.tile.storage.TileDrive;

public class GuiDrive extends AEBaseGui
{

	public GuiDrive(InventoryPlayer inventoryPlayer, TileDrive te) {
		super( new ContainerDrive( inventoryPlayer, te ) );
		this.ySize = 199;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/drive.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( GuiText.Drive.getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
