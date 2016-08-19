
package appeng.client.render.model.pipeline;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import appeng.api.client.BakingPipeline;
import appeng.api.client.BakingPipelineElement;


public class BakingPipelineBakedModel extends BakingPipeline implements IBakedModel
{

	private final IBakedModel parent;
	public BakingPipelineBakedModel( IBakedModel parent, BakingPipelineElement<?, ?>... pipeline )
	{
		super( pipeline );
		this.parent = parent;
	}

	@Override
	public List<BakedQuad> getQuads( IBlockState state, EnumFacing side, long rand )
	{
		return pipe( new ArrayList(), parent, state, side, rand );
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return parent.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return parent.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return parent.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return parent.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return parent.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return parent.getOverrides();
	}

}
