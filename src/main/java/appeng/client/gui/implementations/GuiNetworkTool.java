package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerNetworkTool;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;

public class GuiNetworkTool extends AEBaseGui
{

	GuiToggleButton tFacades;

	public GuiNetworkTool(InventoryPlayer inventoryPlayer, INetworkTool te) {
		super( new ContainerNetworkTool( inventoryPlayer, te ) );
		this.ySize = 166;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		try
		{
			if ( btn == tFacades )
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "NetworkTool", "Toggle" ) );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		tFacades = new GuiToggleButton( this.guiLeft - 18, guiTop + 8, 23, 22, GuiText.TransparentFacades.getLocal(), GuiText.TransparentFacadesHint.getLocal() );

		buttonList.add( tFacades );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/toolbox.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		if ( tFacades != null )
			tFacades.setState( ((ContainerNetworkTool) inventorySlots).facadeMode );

		fontRendererObj.drawString( getGuiDisplayName( GuiText.NetworkTool.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}

}
