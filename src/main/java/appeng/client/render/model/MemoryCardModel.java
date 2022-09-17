package appeng.client.render.model;


import appeng.core.AppEng;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;


/**
 * Model wrapper for the memory card item model, which combines a base card layer with a "visual hash" of the part/tile.
 */
public class MemoryCardModel implements IModel {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "item/memory_card");
    private static final ResourceLocation TEXTURE = new ResourceLocation(AppEng.MOD_ID, "items/memory_card_hash");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singletonList(MODEL_BASE);
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.singletonList(TEXTURE);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        TextureAtlasSprite texture = bakedTextureGetter.apply(TEXTURE);

        IBakedModel baseModel = this.getBaseModel(state, format, bakedTextureGetter);

        return new MemoryCardBakedModel(format, baseModel, texture);
    }

    private IBakedModel getBaseModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        // Load the base model
        try {
            return ModelLoaderRegistry.getModel(MODEL_BASE).bake(state, format, bakedTextureGetter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity().toItemTransform();
    }
}
