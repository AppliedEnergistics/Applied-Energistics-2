package appeng.block.misc;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEColor;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.ColorableTileBlockColor;
import appeng.client.render.StaticItemColor;


public class SecurityStationRendering extends BlockRenderingCustomizer
{

	@Override
	@SideOnly( Side.CLIENT )
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.blockColor( ColorableTileBlockColor.INSTANCE );
		itemRendering.color( new StaticItemColor( AEColor.TRANSPARENT ) );
	}
}
