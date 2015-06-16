package appeng.api.util;

import net.minecraft.util.AxisAlignedBB;

/**
 * Mutable stand in for Axis Aligned BB, this was used to prevent GC Thrashing.. Related code could also be re-written.
 */
public class AEAxisAlignedBB
{
	public double minX;
	public double minY;
	public double minZ;
	public double maxX;
	public double maxY;
	public double maxZ;

	public AxisAlignedBB getBoundingBox()
	{
		return AxisAlignedBB.fromBounds( minX, minY, minZ, maxX, maxY, maxZ );
	}
	
	public AEAxisAlignedBB(double a,double b, double c, double d, double e, double f)
	{
		minX=a;
		minY=b;
		minZ=c;
		maxX=d;
		maxY=e;
		maxZ=f;
	}

	public static AEAxisAlignedBB fromBounds(
			double a,
			double b,
			double c,
			double d,
			double e,
			double f )
	{
		return new AEAxisAlignedBB(a,b,c,d,e,f);
	}

	public static AEAxisAlignedBB fromBounds(
			AxisAlignedBB bb )
	{
		return new AEAxisAlignedBB( bb.minX,
		bb.minY,
		bb.minZ,
		bb.maxX,
		bb.maxY,
		bb.maxZ );
	}
}
