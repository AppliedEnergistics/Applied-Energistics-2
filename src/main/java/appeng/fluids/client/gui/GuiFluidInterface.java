
package appeng.fluids.client.gui;


import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiFluidTank;
import appeng.core.localization.GuiText;
import appeng.fluids.container.ContainerFluidInterface;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;


public class GuiFluidInterface extends AEBaseGui
{
	public final static int ID_BUTTON_TANK = 222;

	private final IFluidInterfaceHost host;

	public GuiFluidInterface( final InventoryPlayer ip, final IFluidInterfaceHost te )
	{
		super( new ContainerFluidInterface( ip, te ) );
		this.ySize = 231;
		this.host = te;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		for( int i = 0; i < DualityFluidInterface.NUMBER_OF_TANKS; ++i )
		{
			this.buttonList.add( new GuiFluidTank( ID_BUTTON_TANK + i, this.host.getDualityFluidInterface()
					.getTank( i ), this.getGuiLeft() + 7 + 18 * i, this.getGuiTop() + 16 + 8, 16, 80 ) );
		}
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.fontRenderer.drawString( this.getGuiDisplayName( GuiText.FluidInterface.getLocal() ), 8, 6, 4210752 );
		this.fontRenderer.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.bindTexture( "guis/interfacefluid.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
	}
}
