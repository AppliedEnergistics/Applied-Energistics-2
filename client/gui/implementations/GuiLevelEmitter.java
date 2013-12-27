package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.core.localization.GuiText;
import appeng.parts.automation.PartLevelEmitter;

public class GuiLevelEmitter extends AEBaseGui
{

	public GuiLevelEmitter(InventoryPlayer inventoryPlayer, PartLevelEmitter te) {
		super( new ContainerLevelEmitter( inventoryPlayer, te ) );
		this.ySize = 199;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/lvlemitter.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( GuiText.LevelEmitter.getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
