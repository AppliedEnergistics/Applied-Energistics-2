package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerInterface;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.IInterfaceHost;

public class GuiInterface extends GuiUpgradeable
{

	GuiTabButton priority;
	GuiImgButton BlockMode;
	GuiToggleButton interfaceMode;

	public GuiInterface(InventoryPlayer inventoryPlayer, IInterfaceHost te) {
		super( new ContainerInterface( inventoryPlayer, te ) );
		this.ySize = 211;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == priority )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_PRIORITY ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}

		try
		{
			if ( btn == interfaceMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( Settings.INTERFACE_TERMINAL, backwards ) );

			if ( btn == BlockMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( BlockMode.getSetting(), backwards ) );

		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

	@Override
	protected void addButtons()
	{
		priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender );
		buttonList.add( priority );

		BlockMode = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.BLOCK, YesNo.NO );
		buttonList.add( BlockMode );

		interfaceMode = new GuiToggleButton( this.guiLeft - 18, guiTop + 26, 84, 85, GuiText.InterfaceTerminal.getLocal(),
				GuiText.InterfaceTerminalHint.getLocal() );
		buttonList.add( interfaceMode );
	}

	@Override
	protected String getBackground()
	{
		return "guis/interface.png";
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		if ( BlockMode != null )
			BlockMode.set( ((ContainerInterface) cvb).bMode );

		if ( interfaceMode != null )
			interfaceMode.setState( ((ContainerInterface) cvb).iTermMode == YesNo.YES );

		fontRendererObj.drawString( getGuiDisplayName( GuiText.Interface.getLocal() ), 8, 6, 4210752 );

		fontRendererObj.drawString( GuiText.Config.getLocal(), 18, 6 + 11 + 7, 4210752 );
		fontRendererObj.drawString( GuiText.StoredItems.getLocal(), 18, 6 + 60 + 7, 4210752 );
		fontRendererObj.drawString( GuiText.Patterns.getLocal(), 8, 6 + 73 + 7, 4210752 );

		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );
	}
}
