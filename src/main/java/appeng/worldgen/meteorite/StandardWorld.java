package appeng.worldgen.meteorite;


import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public class StandardWorld implements IMeteoriteWorld
{

	private final World w;

	public StandardWorld( final World w )
	{
		this.w = w;
	}

	@Override
	public int minX( final int in )
	{
		return in;
	}

	@Override
	public int minZ( final int in )
	{
		return in;
	}

	@Override
	public int maxX( final int in )
	{
		return in;
	}

	@Override
	public int maxZ( final int in )
	{
		return in;
	}

	@Override
	public boolean hasNoSky()
	{
		return !this.getWorld().provider.hasNoSky;
	}

	@Override
	public int getBlockMetadata( final int x, final int y, final int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.getWorld().getBlockMetadata( x, y, z );
		}
		return 0;
	}

	@Override
	public Block getBlock( final int x, final int y, final int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.getWorld().getBlock( x, y, z );
		}
		return Platform.AIR_BLOCK;
	}

	@Override
	public boolean canBlockSeeTheSky( final int x, final int y, final int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.getWorld().canBlockSeeTheSky( x, y, z );
		}
		return false;
	}

	@Override
	public TileEntity getTileEntity( final int x, final int y, final int z )
	{
		if( this.range( x, y, z ) )
		{
			return this.getWorld().getTileEntity( x, y, z );
		}
		return null;
	}

	@Override
	public World getWorld()
	{
		return this.w;
	}

	@Override
	public void setBlock( final int x, final int y, final int z, final Block blk )
	{
		if( this.range( x, y, z ) )
		{
			this.getWorld().setBlock( x, y, z, blk );
		}
	}

	@Override
	public void setBlock( final int x, final int y, final int z, final Block block, final int meta, final int flags )
	{
		if( this.range( x, y, z ) )
		{
			this.getWorld().setBlock( x, y, z, block, meta, flags );
		}
	}

	@Override
	public void done()
	{

	}

	public boolean range( final int x, final int y, final int z )
	{
		return true;
	}
}
