package appeng.tile.networking;

import appeng.block.networking.BlockCableBus;

public class TileCableBusTESR extends TileCableBus
{

	@Override
	protected void updateTileSetting()
	{
		if ( !cb.requiresDynamicRender )
		{
			TileCableBus tcb;
			try
			{
				tcb = (TileCableBus) BlockCableBus.noTesrTile.newInstance();
				tcb.copyFrom( this );
				getWorldObj().setTileEntity( xCoord, yCoord, zCoord, tcb );
			}
			catch (Throwable t)
			{

			}
		}
	}

}
