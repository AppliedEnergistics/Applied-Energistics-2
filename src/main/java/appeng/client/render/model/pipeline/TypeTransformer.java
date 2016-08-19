
package appeng.client.render.model.pipeline;


import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import appeng.api.client.BakingPipelineElement;


public class TypeTransformer
{

	public static final BakingPipelineElement<BakedQuad, QuadVertexData> quads2vecs = new BakingPipelineElement<BakedQuad, QuadVertexData>(){

		@Override
		public List<QuadVertexData> pipe( List<BakedQuad> elements, IBakedModel parent, IBlockState state, EnumFacing side, long rand )
		{
			return Lists.transform( elements, ( quad ) -> {
				if( quad instanceof UnpackedBakedQuad )
				{
					return new QuadVertexData( (UnpackedBakedQuad) quad );
				}
				else
				{
					int[] qdata = quad.getVertexData();
					float[][][] data = new float[4][quad.getFormat().getElementCount()][4];
					for( int v = 0; v < data.length; v++ )
					{
						float[][] vd = data[v];
						for( int e = 0; e < vd.length; e++ )
						{
							LightUtil.unpack( qdata, vd[e], quad.getFormat(), v, e );
						}
					}
					return new QuadVertexData( quad.getFormat(), data, quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting() );
				}
			} );
		}

	};

	public static final BakingPipelineElement<QuadVertexData, BakedQuad> vecs2quads = new BakingPipelineElement<QuadVertexData, BakedQuad>(){

		@Override
		public List<BakedQuad> pipe( List<QuadVertexData> elements, IBakedModel parent, IBlockState state, EnumFacing side, long rand )
		{
			return Lists.transform( elements, ( data ) -> {
				return data.toQuad();
			} );
		}

	};

}
