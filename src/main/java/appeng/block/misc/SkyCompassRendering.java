package appeng.block.misc;


import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.model.SkyCompassModel;
import appeng.client.render.tesr.SkyCompassTESR;


public class SkyCompassRendering extends BlockRenderingCustomizer
{

	private static final ModelResourceLocation ITEM_MODEL = new ModelResourceLocation( "appliedenergistics2:sky_compass", "normal" );

	@Override
	@SideOnly( Side.CLIENT )
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.tesr( new SkyCompassTESR() );
		// This disables the default smart-rotating model
		rendering.modelCustomizer( ( loc, model ) -> model );
		itemRendering.model( ITEM_MODEL );
		itemRendering.builtInModel( "models/block/builtin/sky_compass", new SkyCompassModel() );
	}

}
