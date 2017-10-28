
package appeng.client.render;


import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;


public abstract class DelegateBakedModel implements IBakedModel
{
	private IBakedModel baseModel;

	protected DelegateBakedModel( IBakedModel base )
	{
		this.baseModel = base;
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective( ItemCameraTransforms.TransformType type )
	{
		Pair<? extends IBakedModel, Matrix4f> pair = this.baseModel.handlePerspective( type );
		return Pair.of( this, pair.getValue() );
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return this.baseModel.getItemCameraTransforms();
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return this.baseModel.isAmbientOcclusion();
	}

	public IBakedModel getBaseModel()
	{
		return this.baseModel;
	}
}
