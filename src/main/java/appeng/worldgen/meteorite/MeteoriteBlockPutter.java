package appeng.worldgen.meteorite;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;


public class MeteoriteBlockPutter
{
	public boolean put( IMeteoriteWorld w, int i, int j, int k, Block blk )
	{
		Block original = w.getBlock( i, j, k );

		if( original == Blocks.bedrock || original == blk )
		{
			return false;
		}

		w.setBlock( i, j, k, blk );
		return true;
	}

	public void put( IMeteoriteWorld w, int i, int j, int k, Block blk, int meta )
	{
		if( w.getBlock( i, j, k ) == Blocks.bedrock )
		{
			return;
		}

		w.setBlock( i, j, k, blk, meta, 3 );
	}
}
