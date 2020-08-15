package appeng.block.qnb;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

public class QnbFormedModel implements BasicUnbakedModel {

    private static final Identifier MODEL_RING = new Identifier(AppEng.MOD_ID, "block/qnb/ring");

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer, Identifier modelId) {
        BakedModel ringModel = loader.bake(MODEL_RING, rotationContainer);
        return new QnbFormedBakedModel(ringModel, textureGetter);
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return ImmutableSet.of(MODEL_RING);
    }

    @Override
    public Stream<SpriteIdentifier> getAdditionalTextures() {
        return QnbFormedBakedModel.getRequiredTextures().stream();
    }

}
