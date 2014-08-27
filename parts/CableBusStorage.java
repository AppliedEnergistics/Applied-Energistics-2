package appeng.parts;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;

/**
 * Thin data storage to optimize memory usage for cables.
 */
public class CableBusStorage
{

	private IPartCable center;
	private IPart sides[];
	private IFacadePart facades[];

	protected IPartCable getCenter()
	{
		return center;
	}

	protected void setCenter(IPartCable center)
	{
		this.center = center;
	}

	protected IPart getSide(ForgeDirection side)
	{
		int x = side.ordinal();
		if ( sides != null && sides.length > x )
			return sides[x];

		return null;
	}

	protected void setSide(ForgeDirection side, IPart part)
	{
		int x = side.ordinal();

		if ( sides != null && sides.length > x && part == null )
		{
			sides[x] = null;
			sides = shrink( sides, true );
		}
		else if ( part != null )
		{
			sides = grow( sides, x, true );
			sides[x] = part;
		}
	}

	public IFacadePart getFacade(int x)
	{
		if ( facades != null && facades.length > x )
			return facades[x];

		return null;
	}

	public void setFacade(int x, IFacadePart facade)
	{
		if ( facades != null && facades.length > x && facade == null )
		{
			facades[x] = null;
			facades = shrink( facades, false );
		}
		else
		{
			facades = grow( facades, x, false );
			facades[x] = facade;
		}
	}

	private <T> T[] grow(T[] in, int new_value, boolean parts)
	{
		if ( in != null && in.length > new_value )
			return in;

		int newSize = new_value + 1;

		T[] newArray = (T[]) (parts ? new IPart[newSize] : new IFacadePart[newSize]);
		if ( in != null )
			System.arraycopy( in, 0, newArray, 0, in.length );

		return newArray;
	}

	private <T> T[] shrink(T[] in, boolean parts)
	{
		int newSize = 0;
		for (int x = 0; x < in.length; x++)
			if ( in[x] != null )
				newSize = x;

		if ( newSize == 0 )
			return null;

		newSize++;
		if ( newSize == in.length )
			return in;

		T[] newArray = (T[]) (parts ? new IPart[newSize] : new IFacadePart[newSize]);
		System.arraycopy( in, 0, newArray, 0, newSize );

		return newArray;
	}
}
