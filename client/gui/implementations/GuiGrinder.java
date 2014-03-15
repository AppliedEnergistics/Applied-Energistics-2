package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerGrinder;
import appeng.core.localization.GuiText;
import appeng.tile.grindstone.TileGrinder;

public class GuiGrinder extends AEBaseGui
{

	public GuiGrinder(InventoryPlayer inventoryPlayer, TileGrinder te) {
		super( new ContainerGrinder( inventoryPlayer, te ) );
		this.ySize = 176;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/grinder.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.GrindStone.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
