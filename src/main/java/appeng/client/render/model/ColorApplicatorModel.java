package appeng.client.render.model;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * A color applicator uses the base model, and extends it with additional layers that are colored according to the
 * selected color of the applicator.
 */
public class ColorApplicatorModel implements BasicUnbakedModel<ColorApplicatorModel> {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID,
            "item/color_applicator_colored");

    private static final RenderMaterial TEXTURE_DARK = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_tip_dark"));
    private static final RenderMaterial TEXTURE_MEDIUM = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_tip_medium"));
    private static final RenderMaterial TEXTURE_BRIGHT = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_tip_bright"));

    @Override
    public Collection<ResourceLocation> getModelDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return Stream.of(TEXTURE_DARK, TEXTURE_MEDIUM, TEXTURE_DARK);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, ResourceLocation modelLocation) {
        IBakedModel baseModel = bakery.getBakedModel(MODEL_BASE, modelTransform, spriteGetter);

        TextureAtlasSprite texDark = spriteGetter.apply(TEXTURE_DARK);
        TextureAtlasSprite texMedium = spriteGetter.apply(TEXTURE_MEDIUM);
        TextureAtlasSprite texBright = spriteGetter.apply(TEXTURE_BRIGHT);

        return new ColorApplicatorBakedModel(baseModel, modelTransform, texDark, texMedium, texBright);
    }
}
