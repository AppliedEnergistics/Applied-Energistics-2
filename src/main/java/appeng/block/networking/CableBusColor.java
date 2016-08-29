package appeng.block.networking;


import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CableBusRenderState;


/**
 * Exposes the cable bus color as tint indices 0 (dark variant), 1 (medium variant) and 2 (bright variant).
 */
@SideOnly( Side.CLIENT )
public class CableBusColor implements IBlockColor
{

	@Override
	public int colorMultiplier( IBlockState state, IBlockAccess worldIn, BlockPos pos, int color )
	{

		AEColor busColor = AEColor.Transparent;

		if( state instanceof IExtendedBlockState )
		{
			CableBusRenderState renderState = ( (IExtendedBlockState) state ).getValue( BlockCableBus.RENDER_STATE_PROPERTY );
			if( renderState != null )
			{
				busColor = renderState.getCableColor();
			}
		}

		return busColor.getVariantByTintIndex( color );

	}
}
