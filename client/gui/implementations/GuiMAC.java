package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerMAC;
import appeng.core.localization.GuiText;
import appeng.tile.crafting.TileMolecularAssembler;

public class GuiMAC extends GuiUpgradeable
{

	@Override
	protected void addButtons()
	{
		redstoneMode = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		buttonList.add( redstoneMode );
	}

	protected String getBackground()
	{
		return "guis/mac.png";
	}

	public GuiMAC(InventoryPlayer inventoryPlayer, TileMolecularAssembler te) {
		super( new ContainerMAC( inventoryPlayer, te ) );
		this.ySize = 197;
	}

	protected GuiText getName()
	{
		return GuiText.MolecularAssembler;
	}
}
