package appeng.worldgen.meteorite;


import appeng.api.definitions.IBlockDefinition;
import net.minecraft.init.Blocks;


public class FalloutSnow extends FalloutCopy
{
	private static final double SNOW_THRESHOLD = 0.7;
	private static final double ICE_THRESHOLD = 0.5;
	private final MeteoriteBlockPutter putter;

	public FalloutSnow( final IMeteoriteWorld w, final int x, final int y, final int z, final MeteoriteBlockPutter putter, final IBlockDefinition skyStoneDefinition )
	{
		super( w, x, y, z, putter, skyStoneDefinition );
		this.putter = putter;
	}

	@Override
	public int adjustCrater()
	{
		return 2;
	}

	@Override
	public void getOther( final IMeteoriteWorld w, final int x, final int y, final int z, final double a )
	{
		if( a > SNOW_THRESHOLD )
		{
			this.putter.put( w, x, y, z, Blocks.snow );
		}
		else if( a > ICE_THRESHOLD )
		{
			this.putter.put( w, x, y, z, Blocks.ice );
		}
	}
}