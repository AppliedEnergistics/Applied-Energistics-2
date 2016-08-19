
package appeng.client.render.model.pipeline;


import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;

import appeng.api.client.BakingPipelineElement;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.FacingToRotation;


public class DoubleFacingQuadRotator implements BakingPipelineElement<QuadVertexData, QuadVertexData>
{

	@Override
	public List<QuadVertexData> pipe( List<QuadVertexData> elements, IBakedModel parent, IBlockState state, EnumFacing side, long rand )
	{
		if( state != null )
		{
			final EnumFacing forward = state.getValue( AEBaseTileBlock.AE_BLOCK_FORWARD );
			final EnumFacing up = state.getValue( AEBaseTileBlock.AE_BLOCK_UP );
			final FacingToRotation f2r = FacingToRotation.get( forward, up );
			List<QuadVertexData> rotated = new ArrayList();
			for( QuadVertexData data : elements )
			{
				data.setFace( f2r.rotate( data.getFace() ) );
				float[][][] qd = data.getData();
				for( int v = 0; v < 4; v++ )
				{
					for( int e = 0; e < data.getFormat().getElementCount(); e++ )
					{
						VertexFormatElement element = data.getFormat().getElement( e );
						if( element.getUsage() == VertexFormatElement.EnumUsage.POSITION )
						{
							qd[v][e] = transform( f2r, qd[v][e] );
						}
						else if( element.getUsage() == VertexFormatElement.EnumUsage.NORMAL )
						{
							qd[v][e] = transformNormal( f2r, qd[v][e] );
						}
					}
				}
				rotated.add( new QuadVertexData( data.getFormat(), qd, data.getTintIndex(), data.getFace(), data.getSprite(), data.shouldApplyDiffuseLighting() ) );
			}
			return rotated;
		}
		return elements;
	}

	private float[] transform( FacingToRotation f2r, float[] fs )
	{
		switch( fs.length )
		{
			case 3:
				Vector3f vec = new Vector3f( fs[0], fs[1], fs[2] );
				vec.x -= 0.5f;
				vec.y -= 0.5f;
				vec.z -= 0.5f;
				f2r.getMat().transform( vec );
				vec.x += 0.5f;
				vec.y += 0.5f;
				vec.z += 0.5f;
				return new float[] { vec.x, vec.y, vec.z };
			case 4:
				Vector4f vecc = new Vector4f( fs[0], fs[1], fs[2], fs[3] );
				vecc.x -= 0.5f;
				vecc.y -= 0.5f;
				vecc.z -= 0.5f;
				f2r.getMat().transform( vecc );
				vecc.x += 0.5f;
				vecc.y += 0.5f;
				vecc.z += 0.5f;
				return new float[] { vecc.x, vecc.y, vecc.z, vecc.w };

			default:
				return fs;
		}
	}

	private float[] transformNormal( FacingToRotation f2r, float[] fs )
	{
		switch( fs.length )
		{
			case 3:
				Vector3f vec = new Vector3f( fs[0], fs[1], fs[2] );
				f2r.getMat().transform( vec );
				return new float[] { vec.x, vec.y, vec.z };
			case 4:
				Vector4f vecc = new Vector4f( fs[0], fs[1], fs[2], fs[3] );
				f2r.getMat().transform( vecc );
				return new float[] { vecc.x, vecc.y, vecc.z, vecc.w };

			default:
				return fs;
		}
	}

}
