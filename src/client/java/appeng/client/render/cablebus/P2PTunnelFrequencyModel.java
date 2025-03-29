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

import appeng.api.util.AEColor;
import appeng.client.render.CubeBuilder;
import appeng.core.AppEng;
import appeng.parts.p2p.P2PTunnelFrequencyModelData;
import appeng.util.Platform;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class P2PTunnelFrequencyModel implements DynamicBlockStateModel {

    private static final Material TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("part/p2p_tunnel_frequency"));

    private final TextureAtlasSprite texture;

    private final static Cache<Long, BlockModelPart> modelCache = CacheBuilder.newBuilder().maximumSize(100).build();

    private static final int[][] QUAD_OFFSETS = new int[][]{{3, 11, 2}, {11, 11, 2}, {3, 3, 2}, {11, 3, 2}};

    public P2PTunnelFrequencyModel(TextureAtlasSprite texture) {
        this.texture = texture;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        var modelData = level.getModelData(pos);

        Long frequency = modelData.get(P2PTunnelFrequencyModelData.FREQUENCY);
        if (frequency != null) {
            parts.add(getFrequencyPart(frequency));
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return texture;
    }

    private BlockModelPart getFrequencyPart(long partFlags) {
        try {
            return modelCache.get(partFlags, () -> {
                short frequency = (short) (partFlags & 0xffffL);
                boolean active = (partFlags & 0x10000L) != 0;
                return buildFrequencyPart(frequency, active);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private BlockModelPart buildFrequencyPart(short frequency, boolean active) {
        final AEColor[] colors = Platform.p2p().toColors(frequency);
        var quads = new ArrayList<BakedQuad>(4 * 4);
        final CubeBuilder cb = new CubeBuilder(quads::add);

        cb.setTexture(this.texture);
        cb.setEmissiveMaterial(active);

        for (int i = 0; i < 4; ++i) {
            final int[] offs = QUAD_OFFSETS[i];
            for (int j = 0; j < 4; ++j) {
                final AEColor col = colors[j];

                if (active) {
                    cb.setColorRGB(col.mediumVariant);
                } else {
                    final float scale = 0.3f / 255.0f;
                    cb.setColorRGB((col.blackVariant >> 16 & 0xff) * scale,
                            (col.blackVariant >> 8 & 0xff) * scale, (col.blackVariant & 0xff) * scale);
                }

                final int startx = j % 2;
                final int starty = 1 - j / 2;

                cb.addCube(offs[0] + startx, offs[1] + starty, offs[2], offs[0] + startx + 1, offs[1] + starty + 1,
                        offs[2] + 1);
            }

        }

        // Reset back to default
        cb.setEmissiveMaterial(false);

        return new BlockModelPart() {
            @Override
            public List<BakedQuad> getQuads(@Nullable Direction side) {
                return side == null ? quads : List.of();
            }

            @Override
            public boolean useAmbientOcclusion() {
                return false;
            }

            @Override
            public TextureAtlasSprite particleIcon() {
                return texture;
            }
        };
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final MapCodec<P2PTunnelFrequencyModel.Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            ModelDebugName debugName = getClass()::toString;
            var sprite = baker.sprites().get(TEXTURE, debugName);
            return new P2PTunnelFrequencyModel(sprite);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public MapCodec<P2PTunnelFrequencyModel.Unbaked> codec() {
            return MAP_CODEC;
        }
    }
}
