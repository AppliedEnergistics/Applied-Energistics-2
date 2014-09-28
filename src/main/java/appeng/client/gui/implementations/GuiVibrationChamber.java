package appeng.client.gui.implementations;

import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiProgressBar;
import appeng.client.gui.widgets.GuiProgressBar.Direction;
import appeng.container.implementations.ContainerVibrationChamber;
import appeng.core.localization.GuiText;
import appeng.tile.misc.TileVibrationChamber;

public class GuiVibrationChamber extends AEBaseGui
{

	ContainerVibrationChamber cvc;
	GuiProgressBar pb;

	public GuiVibrationChamber(InventoryPlayer inventoryPlayer, TileVibrationChamber te)
	{
		super( new ContainerVibrationChamber( inventoryPlayer, te ) );
		cvc = (ContainerVibrationChamber) inventorySlots;
		this.ySize = 166;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		pb = new GuiProgressBar( cvc, "guis/vibchamber.png", 99, 36, 176, 14, 6, 18, Direction.VERTICAL );
		this.buttonList.add( pb );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/vibchamber.png" );
		pb.xPosition = 99 + guiLeft;
		pb.yPosition = 36 + guiTop;
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.VibrationChamber.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		int k = 25;
		int l = -15;

		pb.setFullMsg( cvc.aePerTick * cvc.getCurrentProgress() / 100 + " ae/t" );

		if ( cvc.getCurrentProgress() > 0 )
		{
			int i1 = cvc.getCurrentProgress();
			bindTexture( "guis/vibchamber.png" );
			GL11.glColor3f( 1, 1, 1 );
			this.drawTexturedModalRect( k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 2 );
		}
	}

}
