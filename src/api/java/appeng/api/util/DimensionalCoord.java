package appeng.api.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Represents a location in the Minecraft Universe
 */
public class DimensionalCoord extends WorldCoord
{

	private final World w;
	private final int dimId;

	public DimensionalCoord(DimensionalCoord s) {
		super( s.x, s.y, s.z );
		w = s.w;
		dimId = s.dimId;
	}

	public DimensionalCoord(TileEntity s) {
		super( s );
		w = s.getWorldObj();
		dimId = w.provider.dimensionId;
	}

	public DimensionalCoord(World _w, int _x, int _y, int _z) {
		super( _x, _y, _z );
		w = _w;
		dimId = _w.provider.dimensionId;
	}

	@Override
	public DimensionalCoord copy()
	{
		return new DimensionalCoord( this );
	}

	public boolean isEqual(DimensionalCoord c)
	{
		return x == c.x && y == c.y && z == c.z && c.w == this.w;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof DimensionalCoord && isEqual((DimensionalCoord) obj);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ dimId;
	}

	public boolean isInWorld(World world)
	{
		return w == world;
	}

	@Override
	public String toString()
	{
		return dimId + "," + super.toString();
	}

	public World getWorld()
	{
		return w;
	}
}
