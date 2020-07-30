package appeng.block.qnb;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

public class QnbFormedModel implements BasicUnbakedModel<QnbFormedModel> {

    private static final ResourceLocation MODEL_RING = new ResourceLocation(AppEng.MOD_ID, "block/qnb/ring");

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, ResourceLocation modelLocation) {
        IBakedModel ringModel = bakery.getBakedModel(MODEL_RING, modelTransform, spriteGetter);
        return new QnbFormedBakedModel(ringModel, spriteGetter);
    }

    @Override
    public Collection<ResourceLocation> getModelDependencies() {
        return ImmutableSet.of(MODEL_RING);
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return QnbFormedBakedModel.getRequiredTextures().stream();
    }

}
