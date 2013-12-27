package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerChest;
import appeng.core.localization.GuiText;
import appeng.tile.storage.TileChest;

public class GuiChest extends AEBaseGui
{

	public GuiChest(InventoryPlayer inventoryPlayer, TileChest te) {
		super( new ContainerChest( inventoryPlayer, te ) );
		this.ySize = 166;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/chest.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( GuiText.Chest.getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
