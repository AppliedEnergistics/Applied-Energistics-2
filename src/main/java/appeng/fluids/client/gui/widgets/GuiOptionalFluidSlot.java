
package appeng.fluids.client.gui.widgets;


import net.minecraft.client.renderer.GlStateManager;

import appeng.api.storage.data.IAEFluidStack;
import appeng.container.slot.IOptionalSlotHost;
import appeng.fluids.util.IAEFluidTank;


public class GuiOptionalFluidSlot extends GuiFluidSlot
{
	private final IOptionalSlotHost containerBus;
	private final int groupNum;
	private final int srcX;
	private final int srcY;

	public GuiOptionalFluidSlot( IAEFluidTank fluids, final IOptionalSlotHost containerBus, int slot, int id, int groupNum, int x, int y, int xoffs, int yoffs )
	{
		super( fluids, slot, id, x + xoffs * 18, y + yoffs * 18 );
		this.containerBus = containerBus;
		this.groupNum = groupNum;
		this.srcX = x;
		this.srcY = y;
	}

	@Override
	public boolean isSlotEnabled()
	{
		if( this.containerBus == null )
		{
			return false;
		}
		return this.containerBus.isSlotEnabled( this.groupNum );
	}

	@Override
	public IAEFluidStack getFluidStack()
	{
		if( !this.isSlotEnabled() && super.getFluidStack() != null )
		{
			this.setFluidStack( null );
		}
		return super.getFluidStack();
	}

	@Override
	public void drawBackground( int guileft, int guitop )
	{
		GlStateManager.enableBlend();
		if( this.isSlotEnabled() )
		{
			GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
		}
		else
		{
			GlStateManager.color( 1.0F, 1.0F, 1.0F, 0.4F );
		}
		this.drawTexturedModalRect( guileft + this.xPos() - 1, guitop + this.yPos() - 1, this.srcX - 1, this.srcY - 1, this.getWidth() + 2,
				this.getHeight() + 2 );
	}
}
