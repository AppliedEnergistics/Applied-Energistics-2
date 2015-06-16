package appeng.worldgen.meteorite;


import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import appeng.util.Platform;


public class ChunkOnly extends StandardWorld
{

	final Chunk target;
	final int cx;
	final int cz;
	int verticalBits = 0;

	public ChunkOnly( World w, int cx, int cz )
	{
		super( w );
		this.target = w.getChunkFromChunkCoords( cx, cz );
		this.cx = cx;
		this.cz = cz;
	}

	@Override
	public int minX( int in )
	{
		return Math.max( in, this.cx << 4 );
	}

	@Override
	public int minZ( int in )
	{
		return Math.max( in, this.cz << 4 );
	}

	@Override
	public int maxX( int in )
	{
		return Math.min( in, ( this.cx + 1 ) << 4 );
	}

	@Override
	public int maxZ( int in )
	{
		return Math.min( in, ( this.cz + 1 ) << 4 );
	}

	@Override
	public Block getBlock( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.target.getBlock( x, y, z );
		}
		return Platform.AIR_BLOCK;
	}

	@Override
	public void setBlock( int x, int y, int z, Block blk )
	{
		if( this.range( x, y, z ) )
		{
			this.verticalBits |= 1 << ( y >> 4 );
			this.w.setBlockState( new BlockPos( x, y, z), blk.getDefaultState() );
		}
	}

	@Override
	public void setBlock( int x, int y, int z, IBlockState state, int flags )
	{
		if( this.range( x, y, z ) )
		{
			this.verticalBits |= 1 << ( y >> 4 );
			this.w.setBlockState( new BlockPos( x, y, z ), state, flags & ( ~2 ) );
		}
	}

	@Override
	public void done()
	{
		if( this.verticalBits != 0 )
		{
			Platform.sendChunk( this.target, this.verticalBits );
		}
	}

	@Override
	public boolean range( int x, int y, int z )
	{
		return this.cx == ( x >> 4 ) && this.cz == ( z >> 4 );
	}
}
