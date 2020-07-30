package appeng.client.render.cablebus;

import java.util.function.Function;
import java.util.stream.Stream;

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

public class P2PTunnelFrequencyModel implements BasicUnbakedModel<P2PTunnelFrequencyModel> {
    private static final RenderMaterial TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "part/p2p_tunnel_frequency"));

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, ResourceLocation modelLocation) {
        try {
            final TextureAtlasSprite texture = spriteGetter.apply(TEXTURE);
            return new P2PTunnelFrequencyBakedModel(texture);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return Stream.of(TEXTURE);
    }

}
