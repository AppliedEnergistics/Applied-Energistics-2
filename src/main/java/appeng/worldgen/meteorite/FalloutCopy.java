package appeng.worldgen.meteorite;


import net.minecraft.block.state.IBlockState;
import appeng.api.definitions.IBlockDefinition;
import appeng.util.Platform;


public class FalloutCopy extends Fallout
{
	public static final double SPECIFIED_BLOCK_THRESHOLD = 0.9;
	public static final double AIR_BLOCK_THRESHOLD = 0.8;
	public static final double BLOCK_THRESHOLD_STEP = 0.1;

	private final IBlockState block;
	private final MeteoriteBlockPutter putter;

	public FalloutCopy( final IMeteoriteWorld w, final int x, final int y, final int z, final MeteoriteBlockPutter putter, final IBlockDefinition skyStoneDefinition )
	{
		super( putter, skyStoneDefinition );
		this.putter = putter;
		this.block = w.getBlockState( x, y, z );
	}

	@Override
	public void getRandomFall( final IMeteoriteWorld w, final int x, final int y, final int z )
	{
		final double a = Math.random();
		if( a > SPECIFIED_BLOCK_THRESHOLD )
		{
			this.putter.put( w, x, y, z, this.block, 3 );
		}
		else
		{
			this.getOther( w, x, y, z, a );
		}
	}

	public void getOther( final IMeteoriteWorld w, final int x, final int y, final int z, final double a )
	{

	}

	@Override
	public void getRandomInset( final IMeteoriteWorld w, final int x, final int y, final int z )
	{
		final double a = Math.random();
		if( a > SPECIFIED_BLOCK_THRESHOLD )
		{
			this.putter.put( w, x, y, z, this.block, 3 );
		}
		else if( a > AIR_BLOCK_THRESHOLD )
		{
			this.putter.put( w, x, y, z, Platform.AIR_BLOCK );
		}
		else
		{
			this.getOther( w, x, y, z, a - BLOCK_THRESHOLD_STEP );
		}
	}
}