package appeng.client.render.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.Material;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.core.AppEng;

/**
 * Model wrapper for the memory card item model, which combines a base card
 * layer with a "visual hash" of the part/tile.
 */
public class MemoryCardModel implements IModelGeometry<MemoryCardModel> {

    public static final Identifier MODEL_BASE = new Identifier(AppEng.MOD_ID, "item/memory_card_base");
    private static final Material TEXTURE = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "item/memory_card_hash"));

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
                                            Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.singleton(TEXTURE);
    }

    @Nullable
    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery,
                           Function<Material, Sprite> spriteGetter, IModelTransform modelTransform,
                           ModelOverrideList overrides, Identifier modelLocation) {
        Sprite texture = spriteGetter.apply(TEXTURE);

        BakedModel baseModel = bakery.getBakedModel(MODEL_BASE, modelTransform, spriteGetter);

        return new MemoryCardBakedModel(baseModel, texture);
    }

}
