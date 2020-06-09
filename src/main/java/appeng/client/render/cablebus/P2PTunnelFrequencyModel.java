package appeng.client.render.cablebus;


import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;


public class P2PTunnelFrequencyModel implements IModelGeometry<P2PTunnelFrequencyModel>
{
	private static final Material TEXTURE = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "parts/p2p_tunnel_frequency" ) );

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
		try
		{
			final TextureAtlasSprite texture = spriteGetter.apply( TEXTURE );
			return new P2PTunnelFrequencyBakedModel( texture );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return Collections.singleton( TEXTURE );
	}

}
