
package appeng.client.render.cablebus;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.util.AEColor;
import appeng.util.Platform;

public class P2PTunnelFrequencyBakedModel implements IDynamicBakedModel {

    private final TextureAtlasSprite texture;

    private final static Cache<Long, List<BakedQuad>> modelCache = CacheBuilder.newBuilder().maximumSize(100).build();

    private static final int[][] QUAD_OFFSETS = new int[][] { { 4, 10, 2 }, { 10, 10, 2 }, { 4, 4, 2 }, { 10, 4, 2 } };

    public P2PTunnelFrequencyBakedModel(final TextureAtlasSprite texture) {
        this.texture = texture;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData modelData) {
        if (side != null || !(modelData instanceof P2PTunnelFrequencyModelData)) {
            return Collections.emptyList();
        }

        P2PTunnelFrequencyModelData freqModelData = (P2PTunnelFrequencyModelData) modelData;

        return this.getPartQuads(freqModelData.getFrequency());
    }

    private List<BakedQuad> getQuadsForFrequency(final short frequency, final boolean active) {
        final AEColor[] colors = Platform.p2p().toColors(frequency);
        final CubeBuilder cb = new CubeBuilder();

        cb.setTexture(this.texture);
        cb.useStandardUV();
        cb.setEmissiveMaterial(active);

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

                cb.addCube(offs[0] + startx, offs[1] + starty, offs[2], offs[0] + startx + 1, offs[1] + starty + 1,
                        offs[2] + 1);
            }

        }

        // Reset back to default
        cb.setEmissiveMaterial(false);

        return cb.getOutput();
    }

    private List<BakedQuad> getPartQuads(long partFlags) {
        try {
            return modelCache.get(partFlags, () -> {
                short frequency = (short) (partFlags & 0xffffL);
                boolean active = (partFlags & 0x10000L) != 0;
                return this.getQuadsForFrequency(frequency, active);
            });
        } catch (ExecutionException e) {
            return Collections.emptyList();
        }
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
    public boolean func_230044_c_() {
        return false;// TODO
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
        return ItemOverrideList.EMPTY;
    }
}
