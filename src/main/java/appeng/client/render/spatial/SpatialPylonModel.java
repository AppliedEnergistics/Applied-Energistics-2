package appeng.client.render.spatial;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Function;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.core.AppEng;


class SpatialPylonModel implements IModel
{

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.emptyList();
	}

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return Arrays.stream( SpatialPylonTextureType.values() )
				.map( SpatialPylonModel::getTexturePath )
				.collect( Collectors.toList() );
	}

	@Override
	public IBakedModel bake( IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter )
	{
		Map<SpatialPylonTextureType, TextureAtlasSprite> textures = new EnumMap<>( SpatialPylonTextureType.class );

		for( SpatialPylonTextureType type : SpatialPylonTextureType.values() )
		{
			ResourceLocation loc = getTexturePath( type );
			textures.put( type, bakedTextureGetter.apply( loc ) );
		}

		return new SpatialPylonBakedModel( format, textures );
	}

	@Override
	public IModelState getDefaultState()
	{
		return TRSRTransformation.identity();
	}

	private static ResourceLocation getTexturePath( SpatialPylonTextureType type )
	{
		return new ResourceLocation( AppEng.MOD_ID, "blocks/spatial_pylon/" + type.name().toLowerCase() );
	}
}
