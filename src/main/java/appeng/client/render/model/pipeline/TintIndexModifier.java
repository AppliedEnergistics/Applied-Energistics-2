
package appeng.client.render.model.pipeline;


import java.util.List;
import java.util.function.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import appeng.api.client.BakingPipelineElement;


public class TintIndexModifier implements BakingPipelineElement<QuadVertexData, QuadVertexData>
{

	private Function<Integer, Integer> tintTransformer;

	public TintIndexModifier( Function<Integer, Integer> tintTransformer )
	{
		this.tintTransformer = tintTransformer;
	}

	public Function<Integer, Integer> getTintTransformer()
	{
		return tintTransformer;
	}

	public void setTintTransformer( Function<Integer, Integer> tintTransformer )
	{
		this.tintTransformer = tintTransformer;
	}

	@Override
	public List<QuadVertexData> pipe( List<QuadVertexData> elements, IBakedModel parent, IBlockState state, EnumFacing side, long rand )
	{
		for( QuadVertexData quad : elements )
		{
			quad.setTintIndex( tintTransformer.apply( quad.getTintIndex() ) );
		}
		return elements;
	}

}
