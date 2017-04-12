package appeng.worldgen.meteorite;


import appeng.api.definitions.IBlockDefinition;
import net.minecraft.init.Blocks;


public class FalloutSand extends FalloutCopy
{
	private static final double GLASS_THRESHOLD = 0.66;
	private final MeteoriteBlockPutter putter;

	public FalloutSand( final IMeteoriteWorld w, final int x, final int y, final int z, final MeteoriteBlockPutter putter, final IBlockDefinition skyStoneDefinition )
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
		if( a > GLASS_THRESHOLD )
		{
			this.putter.put( w, x, y, z, Blocks.glass );
		}
	}
}