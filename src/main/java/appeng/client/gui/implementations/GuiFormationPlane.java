package appeng.client.gui.implementations;

import java.io.IOException;

import appeng.api.config.ModMode;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerFormationPlane;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
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
		fontRendererObj.drawString( getGuiDisplayName( GuiText.FormationPlane.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		if ( fuzzyMode != null )
			fuzzyMode.set( cvb.fzMode );

		if ( modMode != null )
			modMode.set( cvb.mmMode );
	}

	@Override
	protected void addButtons()
	{
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		modMode = new GuiImgButton( this.guiLeft - 18, guiTop + 48, Settings.MOD_MODE, ModMode.FILTER_BY_ITEM );

		buttonList.add( priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender ) );

		buttonList.add( fuzzyMode );
		buttonList.add( modMode );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

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
	}

	protected String getBackground()
	{
		return "guis/storagebus.png";
	}

}
