package appeng.block.storage;


import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.model.DriveModel;


public class DriveRendering extends BlockRenderingCustomizer
{
	@Override
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.builtInModel( "models/block/builtin/drive", new DriveModel() );
	}
}
