package appeng.block.storage;


import appeng.api.util.AEColor;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.ColorableTileBlockColor;
import appeng.client.render.StaticItemColor;


public class ChestRendering extends BlockRenderingCustomizer
{

	@Override
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		// I checked, the ME chest doesn't keep its color in item form
		itemRendering.color( new StaticItemColor( AEColor.Transparent ) );
		rendering.blockColor( new ColorableTileBlockColor() );
	}

}
