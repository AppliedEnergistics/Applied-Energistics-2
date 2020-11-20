package appeng.client.render.model;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * A color applicator uses the base model, and extends it with additional layers that are colored according to the
 * selected color of the applicator.
 */
public class ColorApplicatorModel implements BasicUnbakedModel {

    private static final Identifier MODEL_BASE = new Identifier(AppEng.MOD_ID, "item/color_applicator_colored");

    private static final SpriteIdentifier TEXTURE_DARK = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "item/color_applicator_tip_dark"));
    private static final SpriteIdentifier TEXTURE_MEDIUM = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "item/color_applicator_tip_medium"));
    private static final SpriteIdentifier TEXTURE_BRIGHT = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "item/color_applicator_tip_bright"));

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Override
    public Stream<SpriteIdentifier> getAdditionalTextures() {
        return Stream.of(TEXTURE_DARK, TEXTURE_MEDIUM, TEXTURE_DARK);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer, Identifier modelId) {
        BakedModel baseModel = loader.bake(MODEL_BASE, rotationContainer);

        Sprite texDark = textureGetter.apply(TEXTURE_DARK);
        Sprite texMedium = textureGetter.apply(TEXTURE_MEDIUM);
        Sprite texBright = textureGetter.apply(TEXTURE_BRIGHT);

        return new ColorApplicatorBakedModel(baseModel, texDark, texMedium, texBright);
    }
}
