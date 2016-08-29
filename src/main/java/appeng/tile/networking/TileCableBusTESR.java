package appeng.tile.networking;


import appeng.block.networking.BlockCableBus;


public class TileCableBusTESR extends TileCableBus
{

	/**
	 * Changes this tile to the non-TESR version, if none of the parts require dynamic rendering.
	 */
	@Override
	protected void updateTileSetting()
	{
		if( !this.getCableBus().isRequiresDynamicRender() )
		{
			try
			{
				final TileCableBus tcb = (TileCableBus) BlockCableBus.getNoTesrTile().newInstance();
				tcb.copyFrom( this );
				this.getWorld().setTileEntity( pos, tcb );
			}
			catch( final Throwable ignored )
			{

			}
		}
	}
}
