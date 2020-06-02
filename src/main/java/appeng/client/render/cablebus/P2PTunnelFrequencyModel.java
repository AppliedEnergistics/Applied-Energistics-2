package appeng.client.render.cablebus;


import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;


public class P2PTunnelFrequencyModel implements IUnbakedModel
{
	private static final Material TEXTURE = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "parts/p2p_tunnel_frequency" ) );

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.emptyList();
	}

	@Override
	public Collection<Material> getTextures( Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors )
	{
		return Collections.singleton( TEXTURE );
	}

	@Nullable
	@Override
	public IBakedModel bakeModel( ModelBakery modelBakeryIn, Function<Material, TextureAtlasSprite> spriteGetterIn, IModelTransform transformIn, ResourceLocation locationIn )
	{
		try
		{
			final TextureAtlasSprite texture = spriteGetterIn.apply( TEXTURE );
			return new P2PTunnelFrequencyBakedModel( texture );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
}
