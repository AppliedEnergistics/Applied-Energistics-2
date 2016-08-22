package appeng.block.networking;


import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEPartLocation;
import appeng.parts.CableBusContainer;


@SideOnly( Side.CLIENT )
public class CableBusColor implements IBlockColor
{

	@Override
	public int colorMultiplier( IBlockState state, IBlockAccess worldIn, BlockPos pos, int color )
	{
		AEPartLocation side = AEPartLocation.fromOrdinal( ( color >> 2 ) & 7 );
		CableBusContainer bus = ( (IExtendedBlockState) state ).getValue( BlockCableBus.cableBus );
		switch( color & 3 )
		{
			case 0:
				return bus.getGridNode( side ) != null && bus.getGridNode( side ).isActive() ? 0xffffff : 0;
			case 1:
				return bus.getColor().blackVariant;
			case 2:
				return bus.getColor().mediumVariant;
			case 3:
				return bus.getColor().whiteVariant;
			default:
				return color;
		}
	}
}
