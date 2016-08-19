
package appeng.client.render.model.pipeline;


import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.TRSRTransformation;


public class FacingQuadRotator extends MatVecApplicator
{

	private EnumFacing override;

	public FacingQuadRotator( EnumFacing override )
	{
		super( TRSRTransformation.getMatrix( override ) );
		this.override = override;
	}

	public FacingQuadRotator()
	{
		this.override = null;
	}

	@Override
	public List<QuadVertexData> pipe( List<QuadVertexData> elements, IBakedModel parent, IBlockState state, EnumFacing side, long rand )
	{
		if( override == null )
		{
			setMatrix( TRSRTransformation.getMatrix( side ) );
		}
		return super.pipe( elements, parent, state, side, rand );
	}

}
