package appeng.block.paint;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import com.mojang.datafixers.util.Pair;

public class PaintSplotchesModel implements IUnbakedModel {

    @Nullable
    @Override
    public IBakedModel bakeModel(ModelBakery loader, Function<RenderMaterial, TextureAtlasSprite> textureGetter,
            IModelTransform rotationContainer, ResourceLocation modelId) {
        return new PaintSplotchesBakedModel(textureGetter);
    }

    @Override
    public Collection<RenderMaterial> getTextures(Function<ResourceLocation, IUnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences) {
        return PaintSplotchesBakedModel.getRequiredTextures();
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }
}
