package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.implementations.IBusCommon;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerBus;
import appeng.core.localization.GuiText;
import appeng.parts.automation.PartImportBus;

public class GuiBus extends AEBaseGui
{

	IBusCommon bc;

	public GuiBus(InventoryPlayer inventoryPlayer, IBusCommon te) {
		super( new ContainerBus( inventoryPlayer, te ) );
		bc = te;
		this.xSize = hasToolbox() ? 246 : 211;
		this.ySize = 184;
	}

	private boolean hasToolbox()
	{
		return ((ContainerBus) inventorySlots).hasToolbox();
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/bus.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize - 34, ySize );
		this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 86 );
		if ( hasToolbox() )
			this.drawTexturedModalRect( offsetX + 178, offsetY + 94, 178, 94, 68, 68 );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( (bc instanceof PartImportBus ? GuiText.ImportBus : GuiText.ExportBus).getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
