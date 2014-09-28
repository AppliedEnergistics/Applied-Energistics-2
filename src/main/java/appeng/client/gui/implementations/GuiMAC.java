package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiProgressBar;
import appeng.client.gui.widgets.GuiProgressBar.Direction;
import appeng.container.implementations.ContainerMAC;
import appeng.core.localization.GuiText;
import appeng.tile.crafting.TileMolecularAssembler;

public class GuiMAC extends GuiUpgradeable
{

	ContainerMAC container;
	GuiProgressBar pb;

	@Override
	public void initGui()
	{
		super.initGui();

		pb = new GuiProgressBar( "guis/mac.png", 139, 36, 148, 201, 6, 18, Direction.VERTICAL );
		this.buttonList.add( pb );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		pb.xPosition = 148 + guiLeft;
		pb.yPosition = 48 + guiTop;
		super.drawBG( offsetX, offsetY, mouseX, mouseY );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		pb.max = 100;
		pb.current = container.craftProgress;
		pb.FullMsg = pb.current + "%";

		super.drawFG( offsetX, offsetY, mouseX, mouseY );
	}

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
		this.container = (ContainerMAC) this.inventorySlots;
	}

	protected GuiText getName()
	{
		return GuiText.MolecularAssembler;
	}
}
