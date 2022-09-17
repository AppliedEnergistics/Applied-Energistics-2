package appeng.client.render.cablebus;


import appeng.api.parts.IPartBakedModel;
import appeng.api.util.AEColor;
import appeng.util.Platform;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class P2PTunnelFrequencyBakedModel implements IBakedModel, IPartBakedModel {
    private final VertexFormat format;
    private final TextureAtlasSprite texture;

    private final static Cache<Long, List<BakedQuad>> modelCache = CacheBuilder.newBuilder().maximumSize(100).build();

    private static final int[][] QUAD_OFFSETS = new int[][]{
            {4, 10, 2},
            {10, 10, 2},
            {4, 4, 2},
            {10, 4, 2}
    };

    public P2PTunnelFrequencyBakedModel(final VertexFormat format, final TextureAtlasSprite texture) {
        this.format = format;
        this.texture = texture;
    }

    @Override
    public List<BakedQuad> getPartQuads(Long partFlags, long rand) {
        try {
            return modelCache.get(partFlags, () ->
            {
                short frequency = 0;
                boolean active = false;
                if (partFlags != null) {
                    frequency = (short) (partFlags.longValue() & 0xffffL);
                    active = (partFlags.longValue() & 0x10000L) != 0;
                }
                return this.getQuadsForFrequency(frequency, active);
            });
        } catch (ExecutionException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }
        return this.getPartQuads(null, rand);
    }

    private List<BakedQuad> getQuadsForFrequency(final short frequency, final boolean active) {
        final AEColor[] colors = Platform.p2p().toColors(frequency);
        final CubeBuilder cb = new CubeBuilder(this.format);

        cb.setTexture(this.texture);
        cb.useStandardUV();
        cb.setRenderFullBright(active);

        for (int i = 0; i < 4; ++i) {
            final int[] offs = QUAD_OFFSETS[i];
            for (int j = 0; j < 4; ++j) {
                final AEColor c = colors[j];
                if (active) {
                    cb.setColorRGB(c.dye.getColorValue());
                } else {
                    final float[] cv = c.dye.getColorComponentValues();
                    cb.setColorRGB(cv[0] * 0.5f, cv[1] * 0.5f, cv[2] * 0.5f);
                }

                final int startx = j % 2;
                final int starty = 1 - j / 2;

                cb.addCube(offs[0] + startx, offs[1] + starty, offs[2], offs[0] + startx + 1, offs[1] + starty + 1, offs[2] + 1);
            }

        }
        return cb.getOutput();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.texture;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
