
package appeng.client.gui.widgets;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEColor;


@SideOnly( Side.CLIENT )
public class GuiFluidTank extends GuiButton implements ITooltip
{
	private final IFluidTank tank;

	public GuiFluidTank( int buttonId, IFluidTank tank, int x, int y, int w, int h )
	{
		super( buttonId, x, y, w, h, "" );
		this.tank = tank;
	}

	@Override
	public void drawButton( final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks )
	{
		if( this.visible )
		{
			GlStateManager.color( 1.0F, 1.0F, 1.0F, 1.0F );
			GlStateManager.disableBlend();

			drawRect( this.x, this.y, this.x + this.width, this.y + this.height, AEColor.GRAY.blackVariant | 0xFF000000 );

			if( this.tank != null )
			{
				final FluidStack fluid = this.tank.getFluid();
				if( fluid != null && fluid.amount > 0 )
				{
					mc.getTextureManager().bindTexture( TextureMap.LOCATION_BLOCKS_TEXTURE );

					TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite( fluid.getFluid().getStill().toString() );
					final int scaledHeight = (int) ( this.height * ( (float) fluid.amount / this.tank.getCapacity() ) );

					int iconHeightRemainder = scaledHeight % 16;
					if( iconHeightRemainder > 0 )
					{
						drawTexturedModalRect( this.x, this.y + this.height - iconHeightRemainder, sprite, 16, iconHeightRemainder );
					}
					for( int i = 0; i < scaledHeight / 16; i++ )
					{
						drawTexturedModalRect( this.x, this.y + this.height - iconHeightRemainder - ( i + 1 ) * 16, sprite, 16, 16 );
					}
				}
			}
		}
	}

	@Override
	public String getMessage()
	{
		if( this.tank != null && this.tank.getFluid() != null && this.tank.getFluid().amount > 0 )
		{
			String desc = this.tank.getFluid().getFluid().getLocalizedName( this.tank.getFluid() );
			String amountToText = this.tank.getFluid().amount + "mB";

			return desc + "\n" + amountToText;
		}
		return null;
	}

	@Override
	public int xPos()
	{
		return this.x - 2;
	}

	@Override
	public int yPos()
	{
		return this.y - 2;
	}

	@Override
	public int getWidth()
	{
		return this.width + 4;
	}

	@Override
	public int getHeight()
	{
		return this.height + 4;
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

}
