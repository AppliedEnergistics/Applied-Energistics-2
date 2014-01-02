package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IBusCommon;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerBus;
import appeng.core.localization.GuiText;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.parts.automation.PartImportBus;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiBus extends AEBaseGui
{

	ContainerBus cvb;
	IBusCommon bc;

	GuiImgButton redstoneMode;
	GuiImgButton fuzzyMode;

	public GuiBus(InventoryPlayer inventoryPlayer, IBusCommon te) {
		super( new ContainerBus( inventoryPlayer, te ) );
		cvb = (ContainerBus) inventorySlots;

		bc = te;
		this.xSize = hasToolbox() ? 246 : 211;
		this.ySize = 184;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		redstoneMode = new GuiImgButton( 122 + guiLeft, 31 + guiTop, Settings.REDSTONE_OUTPUT, RedstoneMode.IGNORE );
		fuzzyMode = new GuiImgButton( 122 + guiLeft, 49 + guiTop, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		buttonList.add( redstoneMode );
		buttonList.add( fuzzyMode );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		try
		{
			if ( btn == redstoneMode )
				PacketDispatcher.sendPacketToServer( (new PacketConfigButton( Settings.REDSTONE_OUTPUT )).getPacket() );

			if ( btn == fuzzyMode )
				PacketDispatcher.sendPacketToServer( (new PacketConfigButton( Settings.FUZZY_MODE )).getPacket() );

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private boolean hasToolbox()
	{
		return ((ContainerBus) inventorySlots).hasToolbox();
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		redstoneMode.setVisibility( bc.getInstalledUpgrades( Upgrades.REDSTONE ) > 0 );
		fuzzyMode.setVisibility( bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );

		bindTexture( "guis/bus.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize - 34, ySize );
		this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 86 );
		if ( hasToolbox() )
			this.drawTexturedModalRect( offsetX + 178, offsetY + 94, 178, 94, 68, 68 );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( (bc instanceof PartImportBus ? GuiText.ImportBus : GuiText.ExportBus).getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		redstoneMode.set( cvb.rsMode );
		fuzzyMode.set( cvb.fzMode );
	}

}
