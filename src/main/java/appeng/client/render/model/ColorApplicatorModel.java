package appeng.client.render.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.core.AppEng;

/**
 * A color applicator uses the base model, and extends it with additional layers
 * that are colored according to the selected color of the applicator.
 */
public class ColorApplicatorModel implements IModelGeometry<ColorApplicatorModel> {

    private static final Identifier MODEL_BASE = new Identifier(AppEng.MOD_ID,
            "item/color_applicator_colored");

    private static final SpriteIdentifier TEXTURE_DARK = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "item/color_applicator_tip_dark"));
    private static final SpriteIdentifier TEXTURE_MEDIUM = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "item/color_applicator_tip_medium"));
    private static final SpriteIdentifier TEXTURE_BRIGHT = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "item/color_applicator_tip_bright"));

    @Override
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration owner,
                                                    Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Arrays.asList(TEXTURE_DARK, TEXTURE_MEDIUM, TEXTURE_DARK);
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery,
                           Function<SpriteIdentifier, Sprite> spriteGetter, IModelTransform modelTransform,
                           ModelOverrideList overrides, Identifier modelLocation) {
        BakedModel baseModel = bakery.getBakedModel(MODEL_BASE, modelTransform, spriteGetter);

        Sprite texDark = spriteGetter.apply(TEXTURE_DARK);
        Sprite texMedium = spriteGetter.apply(TEXTURE_MEDIUM);
        Sprite texBright = spriteGetter.apply(TEXTURE_BRIGHT);

        return new ColorApplicatorBakedModel(baseModel, modelTransform, texDark, texMedium, texBright);
    }
}
