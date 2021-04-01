package appeng.client.render.model;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * A color applicator uses the base model, and extends it with additional layers that are colored according to the
 * selected color of the applicator.
 */
public class ColorApplicatorModel implements BasicUnbakedModel {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID,
            "item/color_applicator_colored");

    private static final RenderMaterial TEXTURE_DARK = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_tip_dark"));
    private static final RenderMaterial TEXTURE_MEDIUM = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_tip_medium"));
    private static final RenderMaterial TEXTURE_BRIGHT = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_tip_bright"));

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return Stream.of(TEXTURE_DARK, TEXTURE_MEDIUM, TEXTURE_DARK);
    }

    @Nullable
    @Override
    public IBakedModel bakeModel(ModelBakery loader, Function<RenderMaterial, TextureAtlasSprite> textureGetter,
            IModelTransform rotationContainer, ResourceLocation modelId) {
        IBakedModel baseModel = loader.bake(MODEL_BASE, rotationContainer);

        TextureAtlasSprite texDark = textureGetter.apply(TEXTURE_DARK);
        TextureAtlasSprite texMedium = textureGetter.apply(TEXTURE_MEDIUM);
        TextureAtlasSprite texBright = textureGetter.apply(TEXTURE_BRIGHT);

        return new ColorApplicatorBakedModel(baseModel, texDark, texMedium, texBright);
    }
}
