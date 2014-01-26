package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerChest;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.tile.storage.TileChest;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiChest extends AEBaseGui
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
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		buttonList.add( priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRenderer ) );
	}

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
