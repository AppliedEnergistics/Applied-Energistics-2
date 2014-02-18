package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerFormationPlane;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.parts.automation.PartFormationPlane;

public class GuiFormationPlane extends GuiUpgradeable
{

	GuiTabButton priority;

	public GuiFormationPlane(InventoryPlayer inventoryPlayer, PartFormationPlane te) {
		super( new ContainerFormationPlane( inventoryPlayer, te ) );
		this.ySize = 251;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		super.drawBG( offsetX, offsetY, mouseX, mouseY );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( GuiText.FormationPlane.getLocal(), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		if ( fuzzyMode != null )
			fuzzyMode.set( cvb.fzMode );
	}

	@Override
	protected void addButtons()
	{
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		buttonList.add( priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender ) );

		buttonList.add( fuzzyMode );
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
			if ( btn == fuzzyMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( fuzzyMode.getSetting(), backwards ) );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

	protected String getBackground()
	{
		return "guis/storagebus.png";
	}

}
