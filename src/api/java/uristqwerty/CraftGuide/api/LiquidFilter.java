package uristqwerty.CraftGuide.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public class LiquidFilter implements ItemFilter
{
	private static NamedTexture containerTexture = null;
	private FluidStack liquid;
	private String liquidName;
	private List<String> tooltip = new ArrayList<String>();

	public LiquidFilter(FluidStack liquid)
	{
		if(containerTexture == null)
		{
			containerTexture = Util.instance.getTexture("liquidFilterContainer");
		}

		setLiquid(liquid);
	}

	public void setLiquid(FluidStack liquid)
	{
		String name = liquid.getFluid().getLocalizedName();
		this.liquid = liquid;
		liquidName = name.toLowerCase();
		tooltip.clear();
		tooltip.add(name);
	}

	@Override
	public boolean matches(Object item)
	{
		if(item instanceof ItemStack)
		{
			return liquid.isFluidEqual((ItemStack)item);
		}
		else if(item instanceof FluidStack)
		{
			return liquid.isFluidEqual((FluidStack)item);
		}
		else if(item instanceof String)
		{
			return liquidName.contains(((String)item).toLowerCase());
		}
		else if(item instanceof List)
		{
			for(Object object: ((List)item))
			{
				if(matches(object))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void draw(Renderer renderer, int x, int y)
	{
		if(liquid != null)
		{
			TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

			Fluid fluid = liquid.getFluid();
			IIcon icon = fluid.getStillIcon();

			if(icon != null)
			{
				textureManager.bindTexture(TextureMap.locationBlocksTexture);

                double u = icon.getInterpolatedU(3.0);
                double u2 = icon.getInterpolatedU(13.0);
                double v = icon.getInterpolatedV(1.0);
                double v2 = icon.getInterpolatedV(15.0);

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glColor4d(1.0, 1.0, 1.0, 1.0);

        		GL11.glBegin(GL11.GL_QUADS);
        	        GL11.glTexCoord2d(u, v);
        	        GL11.glVertex2i(x + 3, y + 1);

        	        GL11.glTexCoord2d(u, v2);
        	        GL11.glVertex2i(x + 3, y + 15);

        	        GL11.glTexCoord2d(u2, v2);
        	        GL11.glVertex2i(x + 13, y + 15);

        	        GL11.glTexCoord2d(u2, v);
        	        GL11.glVertex2i(x + 13, y + 1);
        		GL11.glEnd();
			}
		}

		renderer.renderRect(x - 1, y - 1, 18, 18, containerTexture);
	}

	@Override
	public List<String> getTooltip()
	{
		return tooltip;
	}
}
