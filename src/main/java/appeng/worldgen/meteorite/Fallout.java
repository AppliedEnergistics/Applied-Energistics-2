package appeng.worldgen.meteorite;


import appeng.api.definitions.IBlockDefinition;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;


public class Fallout
{
	private final MeteoriteBlockPutter putter;
	private final IBlockDefinition skyStoneDefinition;

	public Fallout( final MeteoriteBlockPutter putter, final IBlockDefinition skyStoneDefinition )
	{
		this.putter = putter;
		this.skyStoneDefinition = skyStoneDefinition;
	}

	public int adjustCrater()
	{
		return 0;
	}

	public void getRandomFall( final IMeteoriteWorld w, final int x, final int y, final int z )
	{
		final double a = Math.random();
		if( a > 0.9 )
		{
			this.putter.put( w, x, y, z, Blocks.stone );
		}
		else if( a > 0.8 )
		{
			this.putter.put( w, x, y, z, Blocks.cobblestone );
		}
		else if( a > 0.7 )
		{
			this.putter.put( w, x, y, z, Blocks.dirt );
		}
		else
		{
			this.putter.put( w, x, y, z, Blocks.gravel );
		}
	}

	public void getRandomInset( final IMeteoriteWorld w, final int x, final int y, final int z )
	{
		final double a = Math.random();
		if( a > 0.9 )
		{
			this.putter.put( w, x, y, z, Blocks.cobblestone );
		}
		else if( a > 0.8 )
		{
			this.putter.put( w, x, y, z, Blocks.stone );
		}
		else if( a > 0.7 )
		{
			this.putter.put( w, x, y, z, Blocks.grass );
		}
		else if( a > 0.6 )
		{
			for( final Block skyStoneBlock : this.skyStoneDefinition.maybeBlock().asSet() )
			{
				this.putter.put( w, x, y, z, skyStoneBlock );
			}
		}
		else if( a > 0.5 )
		{
			this.putter.put( w, x, y, z, Blocks.gravel );
		}
		else
		{
			this.putter.put( w, x, y, z, Platform.AIR_BLOCK );
		}
	}
}
