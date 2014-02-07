package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerStorageBus;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.parts.misc.PartStorageBus;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiStorageBus extends GuiUpgradeable
{

	GuiImgButton rwMode;
	GuiTabButton priority;

	public GuiStorageBus(InventoryPlayer inventoryPlayer, PartStorageBus te) {
		super( new ContainerStorageBus( inventoryPlayer, te ) );
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
		fontRenderer.drawString( GuiText.StorageBus.getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		if ( fuzzyMode != null )
			fuzzyMode.set( cvb.fzMode );

		if ( rwMode != null )
			rwMode.set( ((ContainerStorageBus) cvb).rwMode );
	}

	@Override
	protected void addButtons()
	{
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		rwMode = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.ACCESS, AccessRestriction.READ_WRITE );

		buttonList.add( priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRenderer ) );

		buttonList.add( fuzzyMode );
		buttonList.add( rwMode );
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
				PacketDispatcher.sendPacketToServer( (new PacketSwitchGuis( GuiBridge.GUI_PRIORITY )).getPacket() );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}
		try
		{
			if ( btn == fuzzyMode )
				PacketDispatcher.sendPacketToServer( (new PacketConfigButton( fuzzyMode.getSetting(), backwards )).getPacket() );

			if ( btn == rwMode )
				PacketDispatcher.sendPacketToServer( (new PacketConfigButton( rwMode.getSetting(), backwards )).getPacket() );
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
