package appeng.block.networking;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.cablebus.CableBusModel;
import appeng.core.features.registries.PartModels;


/**
 * Customizes the rendering behavior for cable busses, which are the biggest multipart of AE2.
 */
public class CableBusRendering extends BlockRenderingCustomizer
{
	private final PartModels partModels;

	public CableBusRendering( PartModels partModels )
	{
		this.partModels = partModels;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.builtInModel( "models/block/builtin/cable_bus", new CableBusModel( partModels ) );
		rendering.blockColor( new CableBusColor() );
		rendering.modelCustomizer( ( loc, model ) -> model );
	}
}
