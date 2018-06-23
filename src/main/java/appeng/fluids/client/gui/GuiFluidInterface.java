
package appeng.fluids.client.gui;


import net.minecraft.entity.player.InventoryPlayer;

import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;
import appeng.fluids.container.ContainerFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;


public class GuiFluidInterface extends AEBaseGui
{

	public GuiFluidInterface( final InventoryPlayer ip, final IFluidInterfaceHost te )
	{
		super( new ContainerFluidInterface( ip, te ) );
		this.ySize = 231;
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
