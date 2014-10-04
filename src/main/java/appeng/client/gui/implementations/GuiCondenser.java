package appeng.client.gui.implementations;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.api.config.Settings;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiProgressBar;
import appeng.client.gui.widgets.GuiProgressBar.Direction;
import appeng.container.implementations.ContainerCondenser;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.tile.misc.TileCondenser;

public class GuiCondenser extends AEBaseGui
{

	final ContainerCondenser cvc;
	GuiProgressBar pb;
	GuiImgButton mode;

	public GuiCondenser(InventoryPlayer inventoryPlayer, TileCondenser te)
	{
		super( new ContainerCondenser( inventoryPlayer, te ) );
		cvc = (ContainerCondenser) inventorySlots;
		this.ySize = 197;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( mode == btn )
		{
			NetworkHandler.instance.sendToServer( new PacketConfigButton( Settings.CONDENSER_OUTPUT, backwards ) );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		pb = new GuiProgressBar( cvc, "guis/condenser.png", 120 + guiLeft, 25 + guiTop, 178, 25, 6, 18, Direction.VERTICAL, GuiText.StoredEnergy.getLocal() );

		mode = new GuiImgButton( 128 + guiLeft, 52 + guiTop, Settings.CONDENSER_OUTPUT, cvc.output );

		this.buttonList.add( pb );
		this.buttonList.add( mode );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/condenser.png" );

		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.Condenser.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		mode.set( cvc.output );
		mode.FillVar = "" + cvc.output.requiredPower;

	}

}
