
package appeng.client.render;


import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import appeng.client.texture.IAESprite;


public class IconUnwrapper extends TextureAtlasSprite
{

	private int width;
	private int height;

	private float max_u;
	private float min_u;
	private float min_v;
	private float max_v;

	protected IconUnwrapper(
			final IAESprite src )
	{
		super( src.getIconName() );
		this.width = src.getIconWidth();
		this.height = src.getIconHeight();
		this.min_u = src.getMinU();
		this.max_u = src.getMaxU();
		this.min_v = src.getMinV();
		this.max_v = src.getMaxV();
	}

	@Override
	public int getIconWidth()
	{
		return this.width;
	}

	@Override
	public int getIconHeight()
	{
		return this.height;
	}

	@Override
	public float getMaxU()
	{
		return this.max_u;
	}

	@Override
	public float getMinV()
	{
		return this.min_v;
	}

	@Override
	public float getMaxV()
	{
		return this.max_v;
	}

	@Override
	public String getIconName()
	{
		return super.getIconName();
	}

	@Override
	public float getMinU()
	{
		return this.min_u;
	}

	@Override
	public float getInterpolatedU( final double d )
	{
		final float f = this.max_u - this.min_u;
		return this.min_u + f * (float) d / 16.0F;
	}

	@Override
	public float getInterpolatedV( final double d )
	{
		final float f = this.max_v - this.min_v;
		return this.min_v + f * ( (float) d / 16.0F );
	}

}
