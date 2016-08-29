package appeng.client.render.model;


import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;


/**
 * Used as the cache key for caching automatically rotated baked models.
 */
final class AutoRotatingCacheKey
{
	private final IBlockState blockState;
	private final EnumFacing forward;
	private final EnumFacing up;
	private final EnumFacing side;

	AutoRotatingCacheKey( IBlockState blockState, EnumFacing forward, EnumFacing up, EnumFacing side )
	{
		this.blockState = blockState;
		this.forward = forward;
		this.up = up;
		this.side = side;
	}

	public IBlockState getBlockState()
	{
		return blockState;
	}

	public EnumFacing getForward()
	{
		return forward;
	}

	public EnumFacing getUp()
	{
		return up;
	}

	public EnumFacing getSide()
	{
		return side;
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o )
		{
			return true;
		}
		if( o == null || getClass() != o.getClass() )
		{
			return false;
		}

		AutoRotatingCacheKey cacheKey = (AutoRotatingCacheKey) o;
		return blockState.equals( cacheKey.blockState ) && forward == cacheKey.forward && up == cacheKey.up && side == cacheKey.side;
	}

	@Override
	public int hashCode()
	{
		int result = blockState.hashCode();
		result = 31 * result + forward.hashCode();
		result = 31 * result + up.hashCode();
		result = 31 * result + ( side != null ? side.hashCode() : 0 );
		return result;
	}
}
