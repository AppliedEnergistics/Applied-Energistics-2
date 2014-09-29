package appeng.client.gui.implementations;

import java.io.IOException;

import appeng.api.config.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerStorageBus;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.parts.misc.PartStorageBus;

public class GuiStorageBus extends GuiUpgradeable
{

	GuiImgButton rwMode;
	GuiImgButton storageFilter;
	GuiTabButton priority;
	GuiImgButton partition;
	GuiImgButton clear;

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
		fontRendererObj.drawString( getGuiDisplayName( GuiText.StorageBus.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		if ( fuzzyMode != null )
			fuzzyMode.set( cvb.fzMode );

		if ( modMode != null )
			modMode.set( cvb.mmMode );

		if ( storageFilter != null )
			storageFilter.set( ((ContainerStorageBus) cvb).storageFilter );

		if ( rwMode != null )
			rwMode.set( ((ContainerStorageBus) cvb).rwMode );
	}

	@Override
	protected void addButtons()
	{
		clear = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.ACTIONS, ActionItems.CLOSE );
		partition = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH );
		rwMode = new GuiImgButton( this.guiLeft - 18, guiTop + 48, Settings.ACCESS, AccessRestriction.READ_WRITE );
		storageFilter = new GuiImgButton( this.guiLeft - 18, guiTop + 68, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY );
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 88, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		modMode = new GuiImgButton( this.guiLeft - 18, guiTop + 108, Settings.MOD_MODE, ModMode.FILTER_BY_ITEM );

		buttonList.add( priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender ) );

		buttonList.add( storageFilter );
		buttonList.add( fuzzyMode );
		buttonList.add( modMode );
		buttonList.add( rwMode );
		buttonList.add( partition );
		buttonList.add( clear );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		try
		{
			if ( btn == partition )
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "StorageBus.Action", "Partition" ) );

			else if ( btn == clear )
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "StorageBus.Action", "Clear" ) );

			else if ( btn == priority )
				NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_PRIORITY ) );

			else if ( btn == fuzzyMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( fuzzyMode.getSetting(), backwards ) );

			else if ( btn == rwMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( rwMode.getSetting(), backwards ) );

			else if ( btn == storageFilter )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( storageFilter.getSetting(), backwards ) );

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
