package appeng.parts.automation;


import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;


/**
 * Models in which directions - looking at the front face - a plane (annihilation, formation, etc.) is connected to other planes of the same type.
 */
public final class PlaneConnections
{

	private final boolean up;
	private final boolean right;
	private final boolean down;
	private final boolean left;

	private static final int BITMASK_UP = 8;
	private static final int BITMASK_RIGHT = 4;
	private static final int BITMASK_DOWN = 2;
	private static final int BITMASK_LEFT = 1;

	public static final List<PlaneConnections> PERMUTATIONS = generatePermutations();

	private static List<PlaneConnections> generatePermutations()
	{
		List<PlaneConnections> connections = new ArrayList<>( 16 );

		for( int i = 0; i < 16; i++ )
		{
			boolean up = ( i & BITMASK_UP ) != 0;
			boolean right = ( i & BITMASK_RIGHT ) != 0;
			boolean down = ( i & BITMASK_DOWN ) != 0;
			boolean left = ( i & BITMASK_LEFT ) != 0;

			connections.add( new PlaneConnections( up, right, down, left ) );
		}

		return connections;
	}

	private PlaneConnections( boolean up, boolean right, boolean down, boolean left )
	{
		this.up = up;
		this.right = right;
		this.down = down;
		this.left = left;
	}

	public static PlaneConnections of( boolean up, boolean right, boolean down, boolean left )
	{
		return PERMUTATIONS.get( getIndex( up, right, down, left ) );
	}

	public boolean isUp()
	{
		return up;
	}

	public boolean isRight()
	{
		return right;
	}

	public boolean isDown()
	{
		return down;
	}

	public boolean isLeft()
	{
		return left;
	}

	// The combination of connections expressed as a number ranging from [0,15]
	public int getIndex()
	{
		return getIndex( up, right, down, left );
	}

	private static int getIndex( boolean up, boolean right, boolean down, boolean left )
	{
		return ( up ? BITMASK_UP : 0 ) + ( right ? BITMASK_RIGHT : 0 ) + ( left ? BITMASK_LEFT : 0 ) + ( down ? BITMASK_DOWN : 0 );
	}

	// Returns a suffix that expresses the connection states as a string
	public String getFilenameSuffix()
	{
		String suffix = Integer.toBinaryString( getIndex() );
		return Strings.padStart(suffix, 4, '0');
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o )
		{
			return true;
		}
		if( o == null || getClass() != o.getClass() )
		{
			return false;
		}

		PlaneConnections that = (PlaneConnections) o;
		return up == that.up && right == that.right && down == that.down && left == that.left;
	}

	@Override
	public int hashCode()
	{
		int result = ( up ? 1 : 0 );
		result = 31 * result + ( right ? 1 : 0 );
		result = 31 * result + ( down ? 1 : 0 );
		result = 31 * result + ( left ? 1 : 0 );
		return result;
	}
}
