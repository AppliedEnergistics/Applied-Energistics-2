package appeng.block.networking;


import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;


public class ControllerRendering extends BlockRenderingCustomizer
{
	@Override
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		// Disables the default model rotator
		rendering.modelCustomizer( ( loc, model ) -> model );
	}
}
