
package appeng.worldgen.meteorite;


import net.minecraft.init.Blocks;

import appeng.api.definitions.IBlockDefinition;
import appeng.util.Platform;


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
			this.putter.put( w, x, y, z, Blocks.STONE );
		}
		else if( a > 0.8 )
		{
			this.putter.put( w, x, y, z, Blocks.COBBLESTONE );
		}
		else if( a > 0.7 )
		{
			this.putter.put( w, x, y, z, Blocks.DIRT );
		}
		else
		{
			this.putter.put( w, x, y, z, Blocks.GRAVEL );
		}
	}

	public void getRandomInset( final IMeteoriteWorld w, final int x, final int y, final int z )
	{
		final double a = Math.random();
		if( a > 0.9 )
		{
			this.putter.put( w, x, y, z, Blocks.COBBLESTONE );
		}
		else if( a > 0.8 )
		{
			this.putter.put( w, x, y, z, Blocks.STONE );
		}
		else if( a > 0.7 )
		{
			this.putter.put( w, x, y, z, Blocks.GRASS );
		}
		else if( a > 0.6 )
		{
			skyStoneDefinition.maybeBlock().ifPresent( block -> this.putter.put( w, x, y, z, block ) );
		}
		else if( a > 0.5 )
		{
			this.putter.put( w, x, y, z, Blocks.GRAVEL );
		}
		else
		{
			this.putter.put( w, x, y, z, Platform.AIR_BLOCK );
		}
	}
}
