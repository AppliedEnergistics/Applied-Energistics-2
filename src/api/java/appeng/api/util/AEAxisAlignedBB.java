
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
		return AxisAlignedBB.fromBounds( this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ );
	}

	public AEAxisAlignedBB( final double a, final double b, final double c, final double d, final double e, final double f )
	{
		this.minX = a;
		this.minY = b;
		this.minZ = c;
		this.maxX = d;
		this.maxY = e;
		this.maxZ = f;
	}

	public static AEAxisAlignedBB fromBounds( final double a, final double b, final double c, final double d, final double e, final double f )
	{
		return new AEAxisAlignedBB( a, b, c, d, e, f );
	}

	public static AEAxisAlignedBB fromBounds( final AxisAlignedBB bb )
	{
		return new AEAxisAlignedBB( bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ );
	}
}
