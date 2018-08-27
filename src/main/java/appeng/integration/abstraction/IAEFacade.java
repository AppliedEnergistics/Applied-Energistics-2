package appeng.integration.abstraction;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Optional;

import team.chisel.ctm.api.IFacade;


/**
 * Neat abstraction class for All the IFacade interfaces.
 *
 * @author covers1624
 */
@Optional.Interface( iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api" )
public interface IAEFacade extends IFacade
{

	IBlockState getFacadeState( IBlockAccess world, BlockPos pos, EnumFacing side );

	@Nonnull
	@Override
	@Optional.Method( modid = "ctm-api" )
	default IBlockState getFacade( @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side, @Nonnull BlockPos connection )
	{
		return getFacadeState( world, pos, side );
	}

	@Nonnull
	@Override
	@Optional.Method( modid = "ctm-api" )
	default IBlockState getFacade( @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side )
	{
		return getFacadeState( world, pos, side );
	}
}
