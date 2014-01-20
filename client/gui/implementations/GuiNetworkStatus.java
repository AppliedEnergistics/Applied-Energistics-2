package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import appeng.client.gui.AEBaseGui;
import appeng.container.implementations.ContainerNetworkStatus;
import appeng.core.localization.GuiText;

public class GuiNetworkStatus extends AEBaseGui
{

	public GuiNetworkStatus(InventoryPlayer inventoryPlayer, TileEntity te) {
		super( new ContainerNetworkStatus( inventoryPlayer, (TileEntity) te ) );
		this.ySize = 199;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/networkstatus.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( GuiText.NetworkDetails.getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
