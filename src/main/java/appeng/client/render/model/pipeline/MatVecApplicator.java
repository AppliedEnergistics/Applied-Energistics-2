
package appeng.client.render.model.pipeline;


import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.api.client.BakingPipelineElement;


public class MatVecApplicator implements BakingPipelineElement<QuadVertexData, QuadVertexData>
{

	private Matrix4f matrix;
	private boolean forceTranslate;

	public MatVecApplicator( Matrix4f matrix, boolean forceTranslate )
	{
		this.matrix = matrix;
		this.forceTranslate = forceTranslate;
	}

	public MatVecApplicator( Matrix4f matrix )
	{
		this( matrix, false );
	}

	public MatVecApplicator()
	{
		this( new Matrix4f() );
	}

	public Matrix4f getMatrix()
	{
		return matrix;
	}

	public void setMatrix( Matrix4f matrix )
	{
		this.matrix = matrix;
	}

	public boolean forceTranslate()
	{
		return forceTranslate;
	}

	public void setForceTranslate( boolean forceTranslate )
	{
		this.forceTranslate = forceTranslate;
	}

	@Override
	public List<QuadVertexData> pipe( List<QuadVertexData> elements, IBakedModel parent, IBlockState state, EnumFacing side, long rand )
	{
		List<QuadVertexData> rotated = new ArrayList();
		for( QuadVertexData data : elements )
		{
			float[][][] qd = data.getData();
			data.setFace( side != null ? TRSRTransformation.rotate( matrix, side ) : side );
			for( int v = 0; v < 4; v++ )
			{
				for( int e = 0; e < data.getFormat().getElementCount(); e++ )
				{
					VertexFormatElement element = data.getFormat().getElement( e );
					if( element.getUsage() == VertexFormatElement.EnumUsage.POSITION )
					{
						qd[v][e] = transform( qd[v][e] );
					}
					else if( element.getUsage() == VertexFormatElement.EnumUsage.NORMAL )
					{
						qd[v][e] = transformNormal( qd[v][e] );
					}
				}
			}
			rotated.add( new QuadVertexData( data.getFormat(), qd, data.getTintIndex(), data.getFace(), data.getSprite(), data.shouldApplyDiffuseLighting() ) );
		}
		return rotated;
	}

	private float[] transform( float[] fs )
	{
		switch( fs.length )
		{
			case 3:
				Vector4f vec = new Vector4f( fs[0], fs[1], fs[2], forceTranslate ? 1 : 0 );
				vec.x -= 0.5f;
				vec.y -= 0.5f;
				vec.z -= 0.5f;
				this.matrix.transform( vec );
				vec.x += 0.5f;
				vec.y += 0.5f;
				vec.z += 0.5f;
				return new float[] { vec.x, vec.y, vec.z };
			case 4:
				Vector4f vecc = new Vector4f( fs[0], fs[1], fs[2], forceTranslate ? 1 : fs[3] );
				vecc.x -= 0.5f;
				vecc.y -= 0.5f;
				vecc.z -= 0.5f;
				this.matrix.transform( vecc );
				vecc.x += 0.5f;
				vecc.y += 0.5f;
				vecc.z += 0.5f;
				return new float[] { vecc.x, vecc.y, vecc.z, vecc.w };

			default:
				return fs;
		}
	}

	private float[] transformNormal( float[] fs )
	{
		switch( fs.length )
		{
			case 3:
				Vector3f vec = new Vector3f( fs[0], fs[1], fs[2] );
				this.matrix.transform( vec );
				vec.normalize();
				return new float[] { vec.x, vec.y, vec.z };
			case 4:
				Vector4f vecc = new Vector4f( fs[0], fs[1], fs[2], fs[3] );
				this.matrix.transform( vecc );
				vecc.normalize();
				return new float[] { vecc.x, vecc.y, vecc.z, vecc.w };

			default:
				return fs;
		}
	}

}
