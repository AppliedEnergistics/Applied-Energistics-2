package appeng.client.render;


import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;


/**
 * This is the actual baked model that will combine the north face of a given block state
 * with the base facade item model to achieve what is then actually rendered on screen.
 */
public class FacadeWithBlockBakedModel implements IBakedModel
{

	private final IBakedModel baseModel;

	private final IBlockState blockState;

	private final IBakedModel textureModel;

	public FacadeWithBlockBakedModel( IBakedModel baseModel, IBlockState blockState )
	{
		this.baseModel = baseModel;
		this.blockState = blockState;
		this.textureModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState( blockState );
	}

	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{
		// Only the north side is actually read from the base model for item models
		if( side == EnumFacing.NORTH )
		{
			return textureModel.getQuads( blockState, side, rand );
		}
		else
		{
			return baseModel.getQuads( state, side, rand );
		}
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return baseModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}
}
