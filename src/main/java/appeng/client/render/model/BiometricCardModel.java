package appeng.client.render.model;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

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
 * Model wrapper for the biometric card item model, which combines a base card layer with a "visual hash" of the player
 * name
 */
public class BiometricCardModel implements BasicUnbakedModel<BiometricCardModel> {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "item/biometric_card_base");
    private static final RenderMaterial TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "item/biometric_card_hash"));

    @Override
    public Collection<ResourceLocation> getModelDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return Stream.of(TEXTURE);
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
