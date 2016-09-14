package appeng.client.render.spatial;


import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.core.AppEng;


public class SpatialPylonRendering extends BlockRenderingCustomizer
{

	private static final ResourceLocation MODEL_ID = new ResourceLocation( AppEng.MOD_ID, "models/blocks/spatial_pylon/builtin" );

	@Override
	@SideOnly( Side.CLIENT )
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.builtInModel( MODEL_ID.getResourcePath(), new SpatialPylonModel() );
		rendering.stateMapper( this::mapState );
	}

	private Map<IBlockState, ModelResourceLocation> mapState( Block block )
	{
		return ImmutableMap.of(
				block.getDefaultState(), new ModelResourceLocation( MODEL_ID, "normal" )
		);
	}

}
