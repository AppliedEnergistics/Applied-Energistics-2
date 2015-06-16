package appeng.api.util;

import net.minecraft.util.EnumFacing;

/**
 * Stand in for previous Forge Direction, Several uses of this class are simply legacy where as some uses of this class are intended.
 */
public enum AEPartLocation
{
    /**
     * Negative Y
     */
	DOWN(0, -1, 0),

	/**
	 * Positive Y
	 */
    UP(0, 1, 0),

    /**
     * Negative Z
     */
    NORTH(0, 0, -1),

	/**
	 * Positive Z
	 */
    SOUTH(0, 0, 1),
    
    /**
     * Negative X
     */
    WEST(-1, 0, 0),

    /**
     * Posative X
     */
    EAST(1, 0, 0),

    /**
     * Center or inside of the block.
     */
    INTERNAL(0, 0, 0);

    public final int xOffset;
    public final int yOffset;
    public final int zOffset;
    
    private static final EnumFacing[] facings = { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST, null };
    private static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 6};

    public static final AEPartLocation[] SIDE_LOCATIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};
    
    private AEPartLocation(int x, int y, int z)
    {
        xOffset = x;
        yOffset = y;
        zOffset = z;
    }

    /**
     * @return Part Location 
     */
    public static AEPartLocation fromOrdinal(int id)
    {
        if (id >= 0 && id < SIDE_LOCATIONS.length)
            return SIDE_LOCATIONS[id];
        
        return INTERNAL;
    }

    /**
     * 100% chance of success.
     * 
     * @param side
     * @return proper Part Location for a facing enum.
     */
    public static AEPartLocation fromFacing(EnumFacing side)
    {
    	if ( side == null ) return INTERNAL;
    	return values()[side.ordinal()];
    }

    /**
     * @return Opposite Part Location, INTERNAL remains INTERNAL.
     */
    public AEPartLocation getOpposite()
    {
        return fromOrdinal(OPPOSITES[ordinal()]);
    }

    /**
     * @return EnumFacing Equivalence, if Center returns null.
     */
	public EnumFacing getFacing()
	{
		return facings[ordinal()];
	}
	
}
