
package appeng.client.render.model.pipeline;


import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import appeng.api.client.BakingPipelineElement;


public class Merge<F, T> implements BakingPipelineElement<F, T>
{

	private final ImmutableList<BakingPipelineElement<?, ?>> pipeline;

	public Merge( BakingPipelineElement<?, ?>... pipeline )
	{
		this.pipeline = ImmutableList.copyOf( pipeline );
	}

	@Override
	public List pipe( List elements, IBakedModel parent, IBlockState state, EnumFacing side, long rand )
	{
		for( BakingPipelineElement<?, ?> element : pipeline )
		{
			elements.addAll( element.pipe( new ArrayList<>(), parent, state, side, rand ) );
		}
		return elements;
	}

}
