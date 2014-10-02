package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Mouse;

import appeng.api.config.ActionItems;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.tile.misc.TileCellWorkbench;
import appeng.util.Platform;

public class GuiCellWorkbench extends GuiUpgradeable
{

	final ContainerCellWorkbench workbench;
	final TileCellWorkbench tcw;

	GuiImgButton clear;
	GuiImgButton partition;
	GuiToggleButton copyMode;

	public GuiCellWorkbench(InventoryPlayer inventoryPlayer, TileCellWorkbench te) {
		super( new ContainerCellWorkbench( inventoryPlayer, te ) );
		workbench = (ContainerCellWorkbench) inventorySlots;
		ySize = 251;
		tcw = te;
	}

	@Override
	protected boolean drawUpgrades()
	{
		return workbench.availableUpgrades() > 0;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		handleButtonVisibility();

		bindTexture( getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, 211 - 34, ySize );
		if ( drawUpgrades() )
		{
			if ( workbench.availableUpgrades() <= 8 )
			{
				this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + workbench.availableUpgrades() * 18 );
				this.drawTexturedModalRect( offsetX + 177, offsetY + (7 + (workbench.availableUpgrades()) * 18), 177, 151, 35, 7 );
			}
			else if ( workbench.availableUpgrades() <= 16 )
			{
				this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18 );
				this.drawTexturedModalRect( offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7 );

				int dx = workbench.availableUpgrades() - 8;
				this.drawTexturedModalRect( offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + dx * 18 );
				if ( dx == 8 )
					this.drawTexturedModalRect( offsetX + 177 + 27, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8, 7 );
				else
					this.drawTexturedModalRect( offsetX + 177 + 27 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151, 35 - 8, 7 );

			}
			else
			{
				this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18 );
				this.drawTexturedModalRect( offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7 );

				this.drawTexturedModalRect( offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + 8 * 18 );
				this.drawTexturedModalRect( offsetX + 177 + 27, offsetY + (7 + (8) * 18), 186, 151, 35 - 8, 7 );

				int dx = workbench.availableUpgrades() - 16;
				this.drawTexturedModalRect( offsetX + 177 + 27 + 18, offsetY, 186, 0, 35 - 8, 7 + dx * 18 );
				if ( dx == 8 )
					this.drawTexturedModalRect( offsetX + 177 + 27 + 18, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8, 7 );
				else
					this.drawTexturedModalRect( offsetX + 177 + 27 + 18 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151, 35 - 8, 7 );
			}
		}
		if ( hasToolbox() )
			this.drawTexturedModalRect( offsetX + 178, offsetY + ySize - 90, 178, 161, 68, 68 );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		try
		{
			if ( btn == copyMode )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "CellWorkbench.Action", "CopyMode" ) );
			}
			else if ( btn == partition )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "CellWorkbench.Action", "Partition" ) );
			}
			else if ( btn == clear )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "CellWorkbench.Action", "Clear" ) );
			}
			else if ( btn == fuzzyMode )
			{
				boolean backwards = Mouse.isButtonDown( 1 );

				FuzzyMode fz = (FuzzyMode) fuzzyMode.getCurrentValue();
				fz = Platform.rotateEnum( fz, backwards, Settings.FUZZY_MODE.getPossibleValues() );

				NetworkHandler.instance.sendToServer( new PacketValueConfig( "CellWorkbench.Fuzzy", fz.name() ) );
			}
			else
				super.actionPerformed( btn );
		}
		catch (IOException ignored)
		{
		}
	}

	@Override
	protected void addButtons()
	{
		clear = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.ACTIONS, ActionItems.CLOSE );
		partition = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH );
		copyMode = new GuiToggleButton( this.guiLeft - 18, guiTop + 48, 11 * 16 + 5, 12 * 16 + 5, GuiText.CopyMode.getLocal(), GuiText.CopyModeDesc.getLocal() );
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 68, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		buttonList.add( fuzzyMode );
		buttonList.add( partition );
		buttonList.add( clear );
		buttonList.add( copyMode );
	}

	@Override
	protected void handleButtonVisibility()
	{
		copyMode.setState( workbench.copyMode == CopyMode.CLEAR_ON_REMOVE );

		boolean hasFuzzy = false;
		IInventory inv = workbench.getCellUpgradeInventory();
		for (int x = 0; x < inv.getSizeInventory(); x++)
		{
			ItemStack is = inv.getStackInSlot( x );
			if ( is != null && is.getItem() instanceof IUpgradeModule )
			{
				if ( ((IUpgradeModule) is.getItem()).getType( is ) == Upgrades.FUZZY )
					hasFuzzy = true;
			}
		}
		fuzzyMode.setVisibility( hasFuzzy );
	}

	@Override
	protected String getBackground()
	{
		return "guis/cellworkbench.png";
	}

	@Override
	protected GuiText getName()
	{
		return GuiText.CellWorkbench;
	}
}
