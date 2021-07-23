/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.cablebus;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.util.AEColor;
import appeng.util.Platform;

public class P2PTunnelFrequencyBakedModel implements IDynamicBakedModel {

    private final TextureAtlasSprite texture;

    private final static Cache<Long, List<net.minecraft.client.renderer.block.model.BakedQuad>> modelCache = CacheBuilder.newBuilder().maximumSize(100).build();

    private static final int[][] QUAD_OFFSETS = new int[][] { { 4, 10, 2 }, { 10, 10, 2 }, { 4, 4, 2 }, { 10, 4, 2 } };

    public P2PTunnelFrequencyBakedModel(final TextureAtlasSprite texture) {
        this.texture = texture;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData modelData) {
        if (side != null || !modelData.hasProperty(P2PTunnelFrequencyModelData.FREQUENCY)) {
            return Collections.emptyList();
        }

        return this.getPartQuads(modelData.getData(P2PTunnelFrequencyModelData.FREQUENCY));
    }

    private List<net.minecraft.client.renderer.block.model.BakedQuad> getQuadsForFrequency(final short frequency, final boolean active) {
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
                    final float[] cv = c.dye.getTextureDiffuseColors();
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

    private List<net.minecraft.client.renderer.block.model.BakedQuad> getPartQuads(long partFlags) {
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
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;// TODO
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.texture;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
