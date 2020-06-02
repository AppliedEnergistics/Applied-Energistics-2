package appeng.client.render.model;


import java.util.Arrays;
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
import net.minecraftforge.client.model.ModelLoaderRegistry;

import appeng.core.AppEng;


/**
 * A color applicator uses the base model, and extends it with additional layers that are colored according to the
 * selected color of the applicator.
 */
public class ColorApplicatorModel implements IUnbakedModel
{

	private static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "item/color_applicator_colored" );

	private static final Material TEXTURE_DARK = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "items/color_applicator_tip_dark" ) );
	private static final Material TEXTURE_MEDIUM = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "items/color_applicator_tip_medium" ) );
	private static final Material TEXTURE_BRIGHT = new Material( AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation( AppEng.MOD_ID, "items/color_applicator_tip_bright" ) );

	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.singletonList( MODEL_BASE );
	}

	@Override
	public Collection<Material> getTextures( Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors )
	{
		return Arrays.asList( TEXTURE_DARK, TEXTURE_MEDIUM, TEXTURE_DARK );
	}

	@Nullable
	@Override
	public IBakedModel bakeModel( ModelBakery modelBakeryIn, Function<Material, TextureAtlasSprite> spriteGetterIn, IModelTransform transformIn, ResourceLocation locationIn )
	{
		IBakedModel baseModel = modelBakeryIn.getBakedModel( MODEL_BASE, transformIn, spriteGetterIn );

		TextureAtlasSprite texDark = spriteGetterIn.apply( TEXTURE_DARK );
		TextureAtlasSprite texMedium = spriteGetterIn.apply( TEXTURE_MEDIUM );
		TextureAtlasSprite texBright = spriteGetterIn.apply( TEXTURE_BRIGHT );

		return new ColorApplicatorBakedModel( baseModel, transformIn, texDark, texMedium, texBright );
	}
}
