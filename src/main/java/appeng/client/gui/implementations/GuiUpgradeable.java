package appeng.client.gui.implementations;

import java.io.IOException;

import appeng.api.config.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.parts.automation.PartImportBus;

public class GuiUpgradeable extends AEBaseGui
{

	ContainerUpgradeable cvb;
	IUpgradeableHost bc;

	GuiImgButton redstoneMode;
	GuiImgButton fuzzyMode;
	GuiImgButton modMode;
	GuiImgButton craftMode;

	public GuiUpgradeable(InventoryPlayer inventoryPlayer, IUpgradeableHost te) {
		this( new ContainerUpgradeable( inventoryPlayer, te ) );
	}

	public GuiUpgradeable(ContainerUpgradeable te) {
		super( te );
		cvb = (ContainerUpgradeable) te;

		bc = (IUpgradeableHost) te.getTarget();
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
		redstoneMode = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		modMode = new GuiImgButton( this.guiLeft - 18, guiTop + 48, Settings.MOD_MODE, ModMode.FILTER_BY_ITEM );
		craftMode = new GuiImgButton( this.guiLeft - 18, guiTop + 68, Settings.CRAFT_ONLY, YesNo.NO );

		buttonList.add( craftMode );
		buttonList.add( modMode );
		buttonList.add( fuzzyMode );
		buttonList.add( redstoneMode );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		try
		{
			if ( btn == redstoneMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( redstoneMode.getSetting(), backwards ) );

			if ( btn == craftMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( craftMode.getSetting(), backwards ) );

			if ( btn == fuzzyMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( fuzzyMode.getSetting(), backwards ) );

			if ( btn == modMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( modMode.getSetting(), backwards ) );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

	protected boolean hasToolbox()
	{
		return ((ContainerUpgradeable) inventorySlots).hasToolbox();
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		handleButtonVisibility();

		bindTexture( getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, 211 - 34, ySize );
		if ( drawUpgrades() )
			this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 14 + cvb.availableUpgrades() * 18 );
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

	protected void handleButtonVisibility()
	{
		if ( redstoneMode != null )
			redstoneMode.setVisibility( bc.getInstalledUpgrades( Upgrades.REDSTONE ) > 0 );
		if ( fuzzyMode != null )
			fuzzyMode.setVisibility( bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );
		if ( modMode != null )
			modMode.setVisibility( bc.getInstalledUpgrades( Upgrades.FUZZY ) > 0 );
		if ( craftMode != null )
			craftMode.setVisibility( bc.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( getName().getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		if ( redstoneMode != null )
			redstoneMode.set( cvb.rsMode );

		if ( fuzzyMode != null )
			fuzzyMode.set( cvb.fzMode );

		if ( modMode != null )
			modMode.set( cvb.mmMode );

		if ( craftMode != null )
			craftMode.set( cvb.cMode );
	}

	protected GuiText getName()
	{
		return bc instanceof PartImportBus ? GuiText.ImportBus : GuiText.ExportBus;
	}

}
