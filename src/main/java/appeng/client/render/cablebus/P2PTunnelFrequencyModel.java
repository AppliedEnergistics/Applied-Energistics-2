package appeng.client.render.cablebus;

import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

public class P2PTunnelFrequencyModel implements BasicUnbakedModel {

    private static final SpriteIdentifier TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "part/p2p_tunnel_frequency"));

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer, Identifier modelId) {
        final Sprite texture = textureGetter.apply(TEXTURE);
        return new P2PTunnelFrequencyBakedModel(texture);
    }

    @Override
    public Stream<SpriteIdentifier> getAdditionalTextures() {
        return Stream.of(TEXTURE);
    }

}
