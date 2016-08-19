
package appeng.client.render.model.pipeline;


import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.client.BakingPipelineElement;


public class StatePosRecolorator implements BakingPipelineElement<QuadVertexData, QuadVertexData>
{

	private IBlockAccess blockAccess;
	private BlockPos pos;
	private IBlockState state;

	public StatePosRecolorator( IBlockAccess blockAccess, BlockPos pos, IBlockState state )
	{
		this.blockAccess = blockAccess;
		this.pos = pos;
		this.state = state;
	}

	public IBlockAccess getBlockAccess()
	{
		return blockAccess;
	}

	public void setBlockAccess( IBlockAccess blockAccess )
	{
		this.blockAccess = blockAccess;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public void setPos( BlockPos pos )
	{
		this.pos = pos;
	}

	public IBlockState getState()
	{
		return state;
	}

	public void setState( IBlockState state )
	{
		this.state = state;
	}

	@Override
	public List<QuadVertexData> pipe( List<QuadVertexData> elements, IBakedModel parent, IBlockState state, EnumFacing side, long rand )
	{
		for( QuadVertexData data : elements )
		{
			data.setTintIndex( Minecraft.getMinecraft().getBlockColors().colorMultiplier( this.state, blockAccess, this.pos, data.getTintIndex() ) );
		}
		return elements;
	}

}
