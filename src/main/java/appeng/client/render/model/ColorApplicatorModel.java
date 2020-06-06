package appeng.client.render.model;


import java.util.Arrays;
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
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import appeng.core.AppEng;
import net.minecraftforge.client.model.geometry.IModelGeometry;


/**
 * A color applicator uses the base model, and extends it with additional layers that are colored according to the
 * selected color of the applicator.
 */
public class ColorApplicatorModel implements IModelGeometry<ColorApplicatorModel>
{

	private static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "item/color_applicator_colored" );

	private static final Material TEXTURE_DARK = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "items/color_applicator_tip_dark" ) );
	private static final Material TEXTURE_MEDIUM = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "items/color_applicator_tip_medium" ) );
	private static final Material TEXTURE_BRIGHT = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "items/color_applicator_tip_bright" ) );

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return Arrays.asList( TEXTURE_DARK, TEXTURE_MEDIUM, TEXTURE_DARK );
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
		IBakedModel baseModel = bakery.getBakedModel( MODEL_BASE, modelTransform, spriteGetter );

		TextureAtlasSprite texDark = spriteGetter.apply( TEXTURE_DARK );
		TextureAtlasSprite texMedium = spriteGetter.apply( TEXTURE_MEDIUM );
		TextureAtlasSprite texBright = spriteGetter.apply( TEXTURE_BRIGHT );

		return new ColorApplicatorBakedModel( baseModel, modelTransform, texDark, texMedium, texBright );
	}
}
