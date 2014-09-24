package appeng.client.texture;

import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TaughtIcon implements IIcon
{

	final float tightness;

	private IIcon p;

	public TaughtIcon(IIcon o, float tightness) {
		
		if ( o == null )
			throw new RuntimeException("Cannot create a wrapper icon with a null icon.");
		
		p = o;
		this.tightness = tightness * 0.4f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinU()
	{
		return u( 0 );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxU()
	{
		return u( 16 );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedU(double d0)
	{
		return u( d0 );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinV()
	{
		return v( 0 );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxV()
	{
		return v( 16 );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedV(double d0)
	{
		return v( d0 );
	}

	private float v(double d)
	{
		if ( d < 8 )
			d -= tightness;
		if ( d > 8 )
			d += tightness;
		return p.getInterpolatedV( Math.min( 16.0, Math.max( 0.0, d ) ) );
	}

	private float u(double d)
	{
		if ( d < 8 )
			d -= tightness;
		if ( d > 8 )
			d += tightness;
		return p.getInterpolatedU( Math.min( 16.0, Math.max( 0.0, d ) ) );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getIconName()
	{
		return p.getIconName();
	}

	@Override
	public int getIconWidth()
	{
		return p.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return p.getIconHeight();
	}

}
