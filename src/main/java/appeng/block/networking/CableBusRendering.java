package appeng.block.networking;


import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.client.BakingPipeline;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.model.pipeline.BakingPipelineBakedModel;
import appeng.client.render.model.pipeline.FacingQuadRotator;
import appeng.client.render.model.pipeline.Merge;
import appeng.client.render.model.pipeline.TintIndexModifier;
import appeng.client.render.model.pipeline.TypeTransformer;
import appeng.client.render.model.pipeline.cable.CableAndConnections;
import appeng.client.render.model.pipeline.cable.Facades;
import appeng.client.render.model.pipeline.cable.Parts;


/**
 * Customizes the rendering behavior for cable busses, which are the biggest multipart of AE2.
 */
public class CableBusRendering extends BlockRenderingCustomizer
{

	private final BakingPipeline<BakedQuad, BakedQuad> rotatingPipeline = new BakingPipeline<>( TypeTransformer.quads2vecs, new FacingQuadRotator(), TypeTransformer.vecs2quads );
	private final TintIndexModifier tintIndexModifier = new TintIndexModifier( tint -> tint );
	private final BakingPipeline<BakedQuad, BakedQuad> tintIndexFixPipeline = new BakingPipeline<>( TypeTransformer.quads2vecs, tintIndexModifier, TypeTransformer.vecs2quads );

	@Override
	@SideOnly( Side.CLIENT )
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.modelCustomizer( this::customizeModel );
		rendering.blockColor( new CableBusColor() );
	}

	private IBakedModel customizeModel( ResourceLocation location, IBakedModel model )
	{
		return new BakingPipelineBakedModel( model,
				new Merge<>(
						new CableAndConnections( rotatingPipeline, tintIndexModifier, tintIndexFixPipeline ),
						new Facades( rotatingPipeline, tintIndexModifier, tintIndexFixPipeline ),
						new Parts( rotatingPipeline, tintIndexModifier, tintIndexFixPipeline )
				)
		);
	}
}
