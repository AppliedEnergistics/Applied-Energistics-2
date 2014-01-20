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
import appeng.container.implementations.ContainerUpgradeable;
import appeng.core.localization.GuiText;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.parts.automation.PartImportBus;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiUpgradeable extends AEBaseGui
{

	ContainerUpgradeable cvb;
	IBusCommon bc;

	GuiImgButton redstoneMode;
	GuiImgButton fuzzyMode;

	public GuiUpgradeable(InventoryPlayer inventoryPlayer, IBusCommon te) {
		this( new ContainerUpgradeable( inventoryPlayer, te ) );
	}

	public GuiUpgradeable(ContainerUpgradeable te) {
		super( te );
		cvb = (ContainerUpgradeable) te;

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

	protected void mouseClicked(int xCoord, int yCoord, int btn)
	{
		if ( btn == 1 )
		{
			for (Object o : this.buttonList)
			{
				GuiButton guibutton = (GuiButton) o;
				if ( guibutton.mousePressed( this.mc, xCoord, yCoord ) )
				{
					super.mouseClicked( xCoord, yCoord, 0 );
					return;
				}
			}
		}
		super.mouseClicked( xCoord, yCoord, btn );
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

	protected boolean hasToolbox()
	{
		return ((ContainerUpgradeable) inventorySlots).hasToolbox();
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		handleButtonVisiblity();

		bindTexture( getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, 211 - 34, ySize );
		if ( drawUpgrades() )
			this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 10 + cvb.availableUpgrades() * 18 );
		if ( hasToolbox() )
			this.drawTexturedModalRect( offsetX + 178, offsetY + ySize - 90, 178, ySize - 90, 68, 68 );
	}

	protected boolean drawUpgrades()
	{
		return true;
	}

	protected String getBackground()
	{
		return "guis/bus.png";
	}

	protected void handleButtonVisiblity()
	{
		if ( redstoneMode != null )
			redstoneMode.setVisibility( bc.getInstalledUpgrades( Upgrades.REDSTONE ) > 0 );
		if ( fuzzyMode != null )
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
