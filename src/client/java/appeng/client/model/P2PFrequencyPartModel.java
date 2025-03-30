package appeng.client.model;

import appeng.api.util.AEColor;
import appeng.client.api.model.parts.PartModel;
import appeng.client.render.CubeBuilder;
import appeng.core.AppEng;
import appeng.parts.automation.PartModelData;
import appeng.util.Platform;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class P2PFrequencyPartModel implements PartModel {
    private static final Material TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, AppEng.makeId("part/p2p_tunnel_frequency"));
    private static final int[][] QUAD_OFFSETS = new int[][]{{3, 11, 2}, {11, 11, 2}, {3, 3, 2}, {11, 3, 2}};
    private final TextureAtlasSprite texture;
    private final Transformation transformation;
    private final Cache<Long, BlockModelPart> modelCache = CacheBuilder.newBuilder().maximumSize(100).build();

    public P2PFrequencyPartModel(TextureAtlasSprite texture, Transformation transformation) {
        this.texture = texture;
        this.transformation = BlockMath.blockCenterToCorner(transformation);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, ModelData partModelData, RandomSource random, List<BlockModelPart> parts) {
        var frequency = partModelData.get(PartModelData.P2P_FREQUENCY);
        frequency = Objects.requireNonNullElse(frequency, 0L);
        parts.add(getFrequencyPart(frequency));
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
        var colors = Platform.p2p().toColors(frequency);
        var quads = new ArrayList<BakedQuad>(4 * 4);
        var quadTransformer = QuadTransformers.applying(transformation);
        quadTransformer.processInPlace(quads);
        var cb = new CubeBuilder(q -> {
            quadTransformer.processInPlace(q);
            quads.add(q);
        });

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

    public record Unbaked() implements PartModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("p2p_tunnel_frequency");
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<? extends PartModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PartModel bake(ModelBaker baker, ModelState modelState) {
            var texture = baker.sprites().get(TEXTURE, getClass()::toString);
            return new P2PFrequencyPartModel(texture, modelState.transformation());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }
    }
}
