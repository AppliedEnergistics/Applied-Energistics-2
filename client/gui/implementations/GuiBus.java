package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

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
		this( new ContainerBus( inventoryPlayer, te ) );
	}

	public GuiBus(ContainerBus te) {
		super( te );
		cvb = (ContainerBus) te;

		bc = (IBusCommon) te.getTarget();
		this.xSize = hasToolbox() ? 246 : 211;
		this.ySize = 184;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		addButtons();
	}

	protected void addButtons()
	{
		redstoneMode = new GuiImgButton( 122 + guiLeft, 31 + guiTop, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		fuzzyMode = new GuiImgButton( 122 + guiLeft, 49 + guiTop, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		buttonList.add( redstoneMode );
		buttonList.add( fuzzyMode );
	}

	protected void mouseClicked(int par1, int par2, int par3)
	{
		if ( par3 == 1 )
			super.mouseClicked( par1, par2, 0 );
		else
			super.mouseClicked( par1, par2, par3 );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		try
		{
			if ( btn == redstoneMode )
				PacketDispatcher.sendPacketToServer( (new PacketConfigButton( redstoneMode.getSetting(), backwards )).getPacket() );

			if ( btn == fuzzyMode )
				PacketDispatcher.sendPacketToServer( (new PacketConfigButton( fuzzyMode.getSetting(), backwards )).getPacket() );

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
		handleButtonVisiblity();

		bindTexture( getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize - 34, ySize );
		this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 86 );
		if ( hasToolbox() )
			this.drawTexturedModalRect( offsetX + 178, offsetY + 94, 178, 94, 68, 68 );
	}

	protected String getBackground()
	{
		return "guis/bus.png";
	}

	protected void handleButtonVisiblity()
	{
		redstoneMode.setVisibility( bc.getInstalledUpgrades( Upgrades.REDSTONE ) > 0 );
		fuzzyMode.setVisibility( bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRenderer.drawString( getName().getLocal(), 8, 6, 4210752 );
		fontRenderer.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		if ( redstoneMode != null )
			redstoneMode.set( cvb.rsMode );

		if ( fuzzyMode != null )
			fuzzyMode.set( cvb.fzMode );
	}

	protected GuiText getName()
	{
		return bc instanceof PartImportBus ? GuiText.ImportBus : GuiText.ExportBus;
	}

}
