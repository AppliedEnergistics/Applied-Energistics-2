package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerDrive;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.tile.storage.TileDrive;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiDrive extends AEBaseGui
{

	GuiTabButton priority;

	@Override
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		super.actionPerformed( par1GuiButton );

		if ( par1GuiButton == priority )
		{
			try
			{
				PacketDispatcher.sendPacketToServer( (new PacketSwitchGuis( GuiBridge.GUI_PRIORITY )).getPacket() );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		buttonList.add( priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRenderer ) );
	}

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
