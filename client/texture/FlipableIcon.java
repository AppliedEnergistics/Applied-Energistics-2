package appeng.client.texture;

import net.minecraft.util.IIcon;

public class FlipableIcon implements IIcon
{

	protected IIcon original;
	boolean flip_u;
	boolean flip_v;

	public FlipableIcon(IIcon o) {
		
		if ( o == null )
			throw new RuntimeException("Cannot create a wrapper icon with a null icon.");
		
		original = o;
		flip_u = false;
		flip_v = false;
	}

	@Override
	public int getIconWidth()
	{
		return original.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return original.getIconHeight();
	}

	@Override
	public float getMinU()
	{
		if ( flip_u )
			return original.getMaxU();
		return original.getMinU();
	}

	@Override
	public float getMaxU()
	{
		if ( flip_u )
			return original.getMinU();
		return original.getMaxU();
	}

	@Override
	public float getInterpolatedU(double px)
	{
		if ( flip_u )
			return original.getInterpolatedU( 16 - px );
		return original.getInterpolatedU( px );
	}

	@Override
	public float getMinV()
	{
		if ( flip_v )
			return original.getMaxV();
		return original.getMinV();
	}

	@Override
	public float getMaxV()
	{
		if ( flip_v )
			return original.getMinV();
		return original.getMaxV();
	}

	@Override
	public float getInterpolatedV(double px)
	{
		if ( flip_v )
			return original.getInterpolatedV( 16 - px );
		return original.getInterpolatedV( px );
	}

	@Override
	public String getIconName()
	{
		return original.getIconName();
	}

	public IIcon getOriginal()
	{
		return original;
	}

	public void setFlip(boolean u, boolean v)
	{
		flip_u = u;
		flip_v = v;
	}

	public int setFlip(int orientation)
	{
		flip_u = (orientation & 8) == 8;
		flip_v = (orientation & 16) == 16;
		return orientation & 7;
	}

}
