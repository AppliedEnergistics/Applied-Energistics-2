package appeng.client.render;


import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Function;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.core.AppEng;


/**
 * The model class for facades. Since facades wrap existing models, they don't declare any dependencies here other
 * than the cable anchor.
 */
public class FacadeItemModel implements IModel
{

	// We use this to get the default item transforms and make our lives easier
	private static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "item/facade_base" );

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.emptyList();
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return Collections.emptyList();
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		IModel baseModel;
		try
		{
			baseModel = ModelLoaderRegistry.getModel( MODEL_BASE );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}

		IBakedModel bakedBaseModel = baseModel.bake( state, format, bakedTextureGetter );

		return new FacadeDispatcherBakedModel( bakedBaseModel );
	}

	@Override
	public IModelState getDefaultState()
	{
		return TRSRTransformation.identity();
	}
}
