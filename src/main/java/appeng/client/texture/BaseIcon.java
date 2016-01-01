
package appeng.client.texture;


import net.minecraft.client.renderer.texture.TextureAtlasSprite;


public class BaseIcon implements IAESprite
{
	private final TextureAtlasSprite spite;

	public BaseIcon( final TextureAtlasSprite src )
	{
		this.spite = src;
	}

	@Override
	public int getIconWidth()
	{
		return this.spite.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return this.spite.getIconHeight();
	}

	@Override
	public float getMaxU()
	{
		return this.spite.getMaxU();
	}

	@Override
	public float getInterpolatedU( final double px )
	{
		return this.spite.getInterpolatedU( px );
	}

	@Override
	public float getMinV()
	{
		return this.spite.getMinV();
	}

	@Override
	public float getMaxV()
	{
		return this.spite.getMaxV();
	}

	@Override
	public String getIconName()
	{
		return this.spite.getIconName();
	}

	@Override
	public float getInterpolatedV( final double px )
	{
		return this.spite.getInterpolatedV( px );
	}

	@Override
	public float getMinU()
	{
		return this.spite.getMinU();
	}

	@Override
	public TextureAtlasSprite getAtlas()
	{
		return this.spite;
	}

}
