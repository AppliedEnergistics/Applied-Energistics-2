package appeng.worldgen.meteorite;


import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
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
		return !this.w.provider.hasNoSky;
	}

	@Override
	public int getBlockMetadata( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.getBlockMetadata( x, y, z );
		}
		return 0;
	}

	@Override
	public Block getBlock( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.getBlock( x, y, z );
		}
		return Platform.AIR_BLOCK;
	}

	@Override
	public boolean canBlockSeeTheSky( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.canBlockSeeTheSky( x, y, z );
		}
		return false;
	}

	@Override
	public TileEntity getTileEntity( int x, int y, int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.w.getTileEntity( x, y, z );
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
			this.w.setBlock( x, y, z, blk );
		}
	}

	@Override
	public void setBlock( int x, int y, int z, Block block, int meta, int flags )
	{
		if( this.range( x, y, z ) )
		{
			this.w.setBlock( x, y, z, block, meta, flags );
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
}
