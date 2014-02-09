package appeng.client.texture;

import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FullIcon implements IIcon
{

	private IIcon p;

	public FullIcon(IIcon o) {
		p = o;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinU()
	{
		return p.getMinU();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxU()
	{
		return p.getMaxU();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedU(double d0)
	{
		if ( d0 > 8.0 )
			return p.getMaxU();
		return p.getMinU();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMinV()
	{
		return p.getMinV();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getMaxV()
	{
		return p.getMaxV();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getInterpolatedV(double d0)
	{
		if ( d0 > 8.0 )
			return p.getMaxV();
		return p.getMinV();
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
