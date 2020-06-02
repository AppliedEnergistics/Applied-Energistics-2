package appeng.client.render.model;


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


/**
 * Model wrapper for the biometric card item model, which combines a base card layer with a "visual hash" of the player
 * name
 */
public class BiometricCardModel implements IUnbakedModel
{

	private static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "item/biometric_card" );
	private static final Material TEXTURE = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "items/biometric_card_hash" ) );

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.singletonList( MODEL_BASE );
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
		TextureAtlasSprite texture = spriteGetterIn.apply( TEXTURE );

		IBakedModel baseModel = modelBakeryIn.getBakedModel( MODEL_BASE, transformIn, spriteGetterIn );

		return new BiometricCardBakedModel( baseModel, texture );
	}
}
