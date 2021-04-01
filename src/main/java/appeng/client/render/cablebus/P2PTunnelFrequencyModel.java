package appeng.client.render.cablebus;

import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

public class P2PTunnelFrequencyModel implements BasicUnbakedModel {

    private static final RenderMaterial TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "part/p2p_tunnel_frequency"));

    @Nullable
    @Override
    public IBakedModel bakeModel(ModelBakery loader, Function<RenderMaterial, TextureAtlasSprite> textureGetter,
            IModelTransform rotationContainer, ResourceLocation modelId) {
        final TextureAtlasSprite texture = textureGetter.apply(TEXTURE);
        return new P2PTunnelFrequencyBakedModel(texture);
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return Stream.of(TEXTURE);
    }

}
