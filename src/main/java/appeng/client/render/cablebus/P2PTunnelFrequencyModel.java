package appeng.client.render.cablebus;


import appeng.core.AppEng;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;


public class P2PTunnelFrequencyModel implements IModel {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AppEng.MOD_ID, "parts/p2p_tunnel_frequency");

    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.singletonList(TEXTURE);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        try {
            final TextureAtlasSprite texture = bakedTextureGetter.apply(TEXTURE);
            return new P2PTunnelFrequencyBakedModel(format, texture);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
