package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Mouse;

import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.core.localization.GuiText;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.tile.misc.TileCellWorkbench;
import appeng.util.Platform;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiCellWorkbench extends GuiUpgradeable
{

	ContainerCellWorkbench ccwb;
	TileCellWorkbench tcw;

	GuiImgButton clear;
	GuiImgButton partition;

	public GuiCellWorkbench(InventoryPlayer inventoryPlayer, TileCellWorkbench te) {
		super( new ContainerCellWorkbench( inventoryPlayer, te ) );
		ccwb = (ContainerCellWorkbench) inventorySlots;
		ySize = 251;
		tcw = te;
	}

	@Override
	protected boolean drawUpgrades()
	{
		return ccwb.availableUpgrades() > 0;
	}

	@Override
	public void initGui()
	{
		super.initGui();
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		handleButtonVisiblity();

		bindTexture( getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, 211 - 34, ySize );
		if ( drawUpgrades() )
		{
			if ( ccwb.availableUpgrades() <= 8 )
			{
				this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + ccwb.availableUpgrades() * 18 );
				this.drawTexturedModalRect( offsetX + 177, offsetY + (7 + (ccwb.availableUpgrades()) * 18), 177, 151, 35, 7 );
			}
			else if ( ccwb.availableUpgrades() <= 16 )
			{
				this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18 );
				this.drawTexturedModalRect( offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7 );

				int dx = ccwb.availableUpgrades() - 8;
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

				int dx = ccwb.availableUpgrades() - 16;
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
			if ( btn == partition )
			{
				PacketDispatcher.sendPacketToServer( (new PacketValueConfig( "CellWorkbench.Action", "Partition" )).getPacket() );
			}
			else if ( btn == clear )
			{
				PacketDispatcher.sendPacketToServer( (new PacketValueConfig( "CellWorkbench.Action", "Clear" )).getPacket() );
			}
			else if ( btn == fuzzyMode )
			{
				boolean backwards = Mouse.isButtonDown( 1 );

				FuzzyMode fz = (FuzzyMode) fuzzyMode.getCurrentValue();
				fz = Platform.rotateEnum( fz, backwards, Settings.FUZZY_MODE.getPossibleValues() );

				PacketDispatcher.sendPacketToServer( (new PacketValueConfig( "CellWorkbench.Fuzzy", fz.name() )).getPacket() );
			}
			else
				super.actionPerformed( btn );
		}
		catch (IOException err)
		{
		}
	}

	@Override
	protected void addButtons()
	{
		clear = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.ACTIONS, ActionItems.CLOSE );
		partition = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH );
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 48, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		buttonList.add( fuzzyMode );
		buttonList.add( partition );
		buttonList.add( clear );
	}

	protected void handleButtonVisiblity()
	{
		boolean hasFuzzy = false;
		IInventory inv = ccwb.getCellUpgradeInventory();
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

	protected String getBackground()
	{
		return "guis/cellworkbench.png";
	}

	protected GuiText getName()
	{
		return GuiText.CellWorkbench;
	}
}
