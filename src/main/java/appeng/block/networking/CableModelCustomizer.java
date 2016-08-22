package appeng.block.networking;


import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import appeng.api.client.BakingPipeline;
import appeng.client.render.model.pipeline.BakingPipelineBakedModel;
import appeng.client.render.model.pipeline.FacingQuadRotator;
import appeng.client.render.model.pipeline.Merge;
import appeng.client.render.model.pipeline.TintIndexModifier;
import appeng.client.render.model.pipeline.TypeTransformer;
import appeng.client.render.model.pipeline.cable.CableAndConnections;
import appeng.client.render.model.pipeline.cable.Facades;
import appeng.client.render.model.pipeline.cable.Parts;


public class CableModelCustomizer
{

	private final BakingPipeline rotatingPipeline = new BakingPipeline( TypeTransformer.quads2vecs, new FacingQuadRotator(), TypeTransformer.vecs2quads );
	private final TintIndexModifier tintIndexModifier = new TintIndexModifier( tint -> tint );
	private final BakingPipeline tintIndexFixPipeline = new BakingPipeline( TypeTransformer.quads2vecs, tintIndexModifier, TypeTransformer.vecs2quads );

	public IBakedModel customizeModel( ModelResourceLocation location, IBakedModel model )
	{
		return new BakingPipelineBakedModel( model, new Merge( new CableAndConnections( rotatingPipeline, tintIndexModifier, tintIndexFixPipeline ), new Facades( rotatingPipeline, tintIndexModifier, tintIndexFixPipeline ), new Parts( rotatingPipeline, tintIndexModifier, tintIndexFixPipeline ) ) );
	}
}
