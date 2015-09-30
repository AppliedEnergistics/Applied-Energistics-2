
package appeng.client.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import appeng.client.texture.IAESprite;


public class IconUnwrapper extends TextureAtlasSprite
{
	
	int width;
	int height;
	
	float max_u;
	float min_u;
	float min_v;
	float max_v;
	
	protected IconUnwrapper(
			final IAESprite src )
	{
		super( src.getIconName() );
		width = src.getIconWidth();
		height = src.getIconHeight();
		min_u = src.getMinU();
		max_u = src.getMaxU();
		min_v = src.getMinV();
		max_v = src.getMaxV();
	}

	@Override
	public int getIconWidth()
	{
		return width;
	}

	@Override
	public int getIconHeight()
	{
		return height;
	}

	@Override
	public float getMaxU()
	{
		return max_u;
	}

	@Override
	public float getMinV()
	{
		return min_v;
	}

	@Override
	public float getMaxV()
	{
		return max_v;
	}

	@Override
	public String getIconName()
	{
		return super.getIconName();
	}

	@Override
	public float getMinU()
	{
		return min_u;
	}

	@Override
    public float getInterpolatedU( final double d)
    {
        final float f = this.max_u - this.min_u;
        return this.min_u + f * (float)d / 16.0F;
    }

	@Override
    public float getInterpolatedV( final double d)
    {
        final float f = this.max_v - this.min_v;
        return this.min_v + f * ((float)d / 16.0F);
    }

}
