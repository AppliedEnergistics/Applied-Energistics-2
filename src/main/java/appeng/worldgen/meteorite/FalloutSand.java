package appeng.worldgen.meteorite;


import net.minecraft.init.Blocks;

import appeng.api.definitions.IBlockDefinition;


public class FalloutSand extends FalloutCopy
{
	public static final double GLASS_THRESHOLD = 0.66;
	private final MeteoriteBlockPutter putter;

	public FalloutSand( IMeteoriteWorld w, int x, int y, int z, MeteoriteBlockPutter putter, IBlockDefinition skyStoneDefinition )
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
	public void getOther( IMeteoriteWorld w, int x, int y, int z, double a )
	{
		if( a > GLASS_THRESHOLD )
		{
			this.putter.put( w, x, y, z, Blocks.glass );
		}
	}
}