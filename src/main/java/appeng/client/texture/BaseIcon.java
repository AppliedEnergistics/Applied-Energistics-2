package appeng.client.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BaseIcon implements IAESprite
{
	final private TextureAtlasSprite spite;
	
	public BaseIcon( final TextureAtlasSprite src )
	{
		spite = src;
	}

	@Override
	public int getIconWidth()
	{
		return spite.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return spite.getIconHeight();
	}

	@Override
	public float getMaxU()
	{
		return spite.getMaxU();
	}

	@Override
	public float getInterpolatedU(
			final double px )
	{
		return spite.getInterpolatedU( px );
	}

	@Override
	public float getMinV()
	{
		return spite.getMinV();
	}

	@Override
	public float getMaxV()
	{
		return spite.getMaxV();
	}

	@Override
	public String getIconName()
	{
		return spite.getIconName();
	}

	@Override
	public float getInterpolatedV(
			final double px )
	{
		return spite.getInterpolatedV( px );
	}

	@Override
	public float getMinU()
	{
		return spite.getMinU();
	}

	@Override
	public TextureAtlasSprite getAtlas()
	{
		return spite;
	}

}
