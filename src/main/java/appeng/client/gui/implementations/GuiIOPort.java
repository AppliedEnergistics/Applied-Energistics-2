package appeng.client.gui.implementations;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.api.AEApi;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerIOPort;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.tile.storage.TileIOPort;

public class GuiIOPort extends GuiUpgradeable
{

	GuiImgButton fullMode;
	GuiImgButton operationMode;

	public GuiIOPort(InventoryPlayer inventoryPlayer, TileIOPort te) {
		super( new ContainerIOPort( inventoryPlayer, te ) );
		this.ySize = 166;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		super.drawBG( offsetX, offsetY, mouseX, mouseY );
		this.drawItem( offsetX + 66 - 8, offsetY + 17, AEApi.instance().items().itemCell1k.stack( 1 ) );
		this.drawItem( offsetX + 94 + 8, offsetY + 17, AEApi.instance().blocks().blockDrive.stack( 1 ) );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.IOPort.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		if ( redstoneMode != null )
			redstoneMode.set( cvb.rsMode );

		if ( operationMode != null )
			operationMode.set( ((ContainerIOPort) cvb).opMode );

		if ( fullMode != null )
			fullMode.set( ((ContainerIOPort) cvb).fMode );
	}

	@Override
	protected void addButtons()
	{
		redstoneMode = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		fullMode = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.FULLNESS_MODE, FullnessMode.EMPTY );
		operationMode = new GuiImgButton( this.guiLeft + 80, guiTop + 17, Settings.OPERATION_MODE, OperationMode.EMPTY );

		buttonList.add( operationMode );
		buttonList.add( redstoneMode );
		buttonList.add( fullMode );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == fullMode )
			NetworkHandler.instance.sendToServer( new PacketConfigButton( fullMode.getSetting(), backwards ) );

		if ( btn == operationMode )
			NetworkHandler.instance.sendToServer( new PacketConfigButton( operationMode.getSetting(), backwards ) );
	}

	@Override
	protected String getBackground()
	{
		return "guis/ioport.png";
	}

}
