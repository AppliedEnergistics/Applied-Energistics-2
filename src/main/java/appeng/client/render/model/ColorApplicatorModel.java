package appeng.client.render.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.ItemOverrideList;
import net.minecraft.client.render.model.Material;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

    private static final Material TEXTURE_DARK = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "item/color_applicator_tip_dark"));
    private static final Material TEXTURE_MEDIUM = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "item/color_applicator_tip_medium"));
    private static final Material TEXTURE_BRIGHT = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "item/color_applicator_tip_bright"));

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
                                            Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Arrays.asList(TEXTURE_DARK, TEXTURE_MEDIUM, TEXTURE_DARK);
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery,
                           Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
                           ItemOverrideList overrides, Identifier modelLocation) {
        BakedModel baseModel = bakery.getBakedModel(MODEL_BASE, modelTransform, spriteGetter);

        TextureAtlasSprite texDark = spriteGetter.apply(TEXTURE_DARK);
        TextureAtlasSprite texMedium = spriteGetter.apply(TEXTURE_MEDIUM);
        TextureAtlasSprite texBright = spriteGetter.apply(TEXTURE_BRIGHT);

        return new ColorApplicatorBakedModel(baseModel, modelTransform, texDark, texMedium, texBright);
    }
}
