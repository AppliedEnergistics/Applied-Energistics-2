package appeng.tile.networking;


import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

import appeng.api.parts.IPart;
import appeng.tile.AEBaseTile;


public class CableBusTESR extends TileEntitySpecialRenderer<AEBaseTile>
{

	@Override
	public void renderTileEntityAt( AEBaseTile te, double x, double y, double z, float partialTicks, int destroyStage )
	{

		if( !( te instanceof TileCableBusTESR ) )
		{
			return;
		}

		TileCableBusTESR realTe = (TileCableBusTESR) te;

		for( EnumFacing facing : EnumFacing.values() )
		{
			IPart part = realTe.getPart( facing );
			if( part != null && part.requireDynamicRender() )
			{
				part.renderDynamic( x, y, z, partialTicks, destroyStage );
			}
		}
	}
}
