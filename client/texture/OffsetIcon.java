package appeng.client.texture;

import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class OffsetIcon implements IIcon
{

	final float offsetX;
	final float offsetY;

	private IIcon p;

	public OffsetIcon(IIcon o, float x, float y) {
		p = o;
		offsetX = x;
		offsetY = y;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinU()
	{
		return u( 0 - offsetX );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxU()
	{
		return u( 16 - offsetX );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedU(double d0)
	{
		return u( d0 - offsetX );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinV()
	{
		return v( 0 - offsetY );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxV()
	{
		return v( 16 - offsetY );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedV(double d0)
	{
		return v( d0 - offsetY );
	}

	private float v(double d)
	{
		return p.getInterpolatedV( Math.min( 16.0, Math.max( 0.0, d ) ) );
	}

	private float u(double d)
	{
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
