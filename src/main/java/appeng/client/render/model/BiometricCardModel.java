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
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.core.AppEng;

/**
 * Model wrapper for the biometric card item model, which combines a base card
 * layer with a "visual hash" of the player name
 */
public class BiometricCardModel implements IModelGeometry<BiometricCardModel> {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "item/biometric_card_base");
    private static final RenderMaterial TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "item/biometric_card_hash"));

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner,
            Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.singleton(TEXTURE);
    }

    @Nullable
    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform transformIn,
            ItemOverrideList overrides, ResourceLocation locationIn) {
        TextureAtlasSprite texture = spriteGetter.apply(TEXTURE);

        IBakedModel baseModel = bakery.getBakedModel(MODEL_BASE, transformIn, spriteGetter);

        return new BiometricCardBakedModel(baseModel, texture);
    }

}
