package appeng.client.texture;

import net.minecraft.util.IIcon;

public class MissingIcon implements IIcon
{

	@Override
	public int getIconWidth()
	{
		return ExtraTextures.getMissing().getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return ExtraTextures.getMissing().getIconHeight();
	}

	@Override
	public float getMinU()
	{
		return ExtraTextures.getMissing().getMinU();
	}

	@Override
	public float getMaxU()
	{
		return ExtraTextures.getMissing().getMaxU();
	}

	@Override
	public float getInterpolatedU(double var1)
	{
		return ExtraTextures.getMissing().getInterpolatedU( var1 );
	}

	@Override
	public float getMinV()
	{
		return ExtraTextures.getMissing().getMinV();
	}

	@Override
	public float getMaxV()
	{
		return ExtraTextures.getMissing().getMaxV();
	}

	@Override
	public float getInterpolatedV(double var1)
	{
		return ExtraTextures.getMissing().getInterpolatedV( var1 );
	}

	@Override
	public String getIconName()
	{
		return ExtraTextures.getMissing().getIconName();
	}

}
