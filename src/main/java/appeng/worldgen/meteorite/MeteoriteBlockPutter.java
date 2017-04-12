package appeng.worldgen.meteorite;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;


public class MeteoriteBlockPutter
{
	public boolean put( final IMeteoriteWorld w, final int i, final int j, final int k, final Block blk )
	{
		final Block original = w.getBlock( i, j, k );

		if( original == Blocks.bedrock || original == blk )
		{
			return false;
		}

		w.setBlock( i, j, k, blk );
		return true;
	}

	void put( final IMeteoriteWorld w, final int i, final int j, final int k, final Block blk, final int meta )
	{
		if( w.getBlock( i, j, k ) == Blocks.bedrock )
		{
			return;
		}

		w.setBlock( i, j, k, blk, meta, 3 );
	}
}
