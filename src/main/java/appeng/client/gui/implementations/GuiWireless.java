package appeng.client.gui.implementations;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.api.config.Settings;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerWireless;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;

public class GuiWireless extends AEBaseGui
{

	GuiImgButton units;

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == units )
		{
			AEConfig.instance.nextPowerUnit( backwards );
			units.set( AEConfig.instance.selectedPowerUnit() );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		units = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.POWER_UNITS, AEConfig.instance.selectedPowerUnit() );
		buttonList.add( units );
	}

	public GuiWireless(InventoryPlayer inventoryPlayer, TileWireless te) {
		super( new ContainerWireless( inventoryPlayer, te ) );
		this.ySize = 166;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/wireless.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.Wireless.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		ContainerWireless cw = (ContainerWireless) inventorySlots;

		if ( cw.range > 0 )
		{
			String msga = GuiText.Range.getLocal() + ": " + ((double) cw.range / 10.0) + " m";
			String msgb = GuiText.PowerUsageRate.getLocal() + ": " + Platform.formatPowerLong( cw.drain, true );

			int strWidth = Math.max( fontRendererObj.getStringWidth( msga ), fontRendererObj.getStringWidth( msgb ) );
			int cOffset = (this.xSize / 2) - (strWidth / 2);
			fontRendererObj.drawString( msga, cOffset, 20, 4210752 );
			fontRendererObj.drawString( msgb, cOffset, 20 + 12, 4210752 );
		}
	}

}
