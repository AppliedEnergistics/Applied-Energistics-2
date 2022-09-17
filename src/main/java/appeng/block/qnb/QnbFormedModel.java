package appeng.block.qnb;


import appeng.core.AppEng;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;
import java.util.function.Function;


public class QnbFormedModel implements IModel {

    private static final ResourceLocation MODEL_RING = new ResourceLocation(AppEng.MOD_ID, "block/qnb/ring");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableList.of(MODEL_RING);
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return QnbFormedBakedModel.getRequiredTextures();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IBakedModel ringModel = this.getBaseModel(MODEL_RING, state, format, bakedTextureGetter);
        return new QnbFormedBakedModel(format, ringModel, bakedTextureGetter);
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    private IBakedModel getBaseModel(ResourceLocation model, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        // Load the base model
        try {
            return ModelLoaderRegistry.getModel(model).bake(state, format, bakedTextureGetter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
