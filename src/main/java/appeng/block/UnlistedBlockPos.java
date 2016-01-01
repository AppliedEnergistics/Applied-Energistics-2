
package appeng.block;


import net.minecraft.util.BlockPos;
import net.minecraftforge.common.property.IUnlistedProperty;


public final class UnlistedBlockPos implements IUnlistedProperty<BlockPos>
{
	@Override
	public String getName()
	{
		return "pos";
	}

	@Override
	public boolean isValid( final BlockPos value )
	{
		return true;
	}

	@Override
	public Class<BlockPos> getType()
	{
		return BlockPos.class;
	}

	@Override
	public String valueToString( final BlockPos value )
	{
		return null;
	}
}