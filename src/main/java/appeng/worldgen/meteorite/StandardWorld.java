package appeng.worldgen.meteorite;


import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import appeng.util.Platform;


public class StandardWorld implements IMeteoriteWorld
{

	protected final World w;

	public StandardWorld( World w )
	{
		this.w = w;
	}

	@Override
	public int minX( int in )
	{
		return in;
	}

	@Override
	public int minZ( int in )
	{
		return in;
	}

	@Override
	public int maxX( int in )
	{
		return in;
	}

	@Override
	public int maxZ( int in )
	{
		return in;
	}

	@Override
	public boolean hasNoSky()
	{
		return !this.w.provider.getHasNoSky();
	}

	@Override
	public Block getBlock( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.getBlockState( new BlockPos( x, y, z ) ).getBlock();
		}
		return Platform.AIR_BLOCK;
	}

	@Override
	public boolean canBlockSeeTheSky( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.canBlockSeeSky( new BlockPos(x,y,z) );
		}
		return false;
	}

	@Override
	public TileEntity getTileEntity( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.getTileEntity( new BlockPos( x, y, z)  );
		}
		return null;
	}

	@Override
	public World getWorld()
	{
		return this.w;
	}

	@Override
	public void setBlock( int x, int y, int z, Block blk )
	{
		if( this.range( x, y, z ) )
		{
			this.w.setBlockState( new BlockPos( x, y, z ), blk.getDefaultState() );
		}
	}

	@Override
	public void done()
	{

	}

	public boolean range( int x, int y, int z )
	{
		return true;
	}

	@Override
	public void setBlock(
			int x,
			int y,
			int z,
			IBlockState state,
			int l )
	{
		if( this.range( x, y, z ) )
		{
			this.w.setBlockState( new BlockPos( x, y, z ), state, l );
		}
	}

	@Override
	public IBlockState getBlockState(
			int x,
			int y,
			int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.getBlockState( new BlockPos(x,y,z) );
		}
		return Blocks.air.getDefaultState();
	}
}
