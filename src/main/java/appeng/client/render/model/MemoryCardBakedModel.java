package appeng.client.render.model;


import appeng.api.implementations.items.IMemoryCard;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AELog;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


class MemoryCardBakedModel implements IBakedModel {
    private static final AEColor[] DEFAULT_COLOR_CODE = new AEColor[]{
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
    };

    private final VertexFormat format;

    private final IBakedModel baseModel;

    private final TextureAtlasSprite texture;

    private final AEColor[] colorCode;

    private final Cache<CacheKey, MemoryCardBakedModel> modelCache;

    private final ImmutableList<BakedQuad> generalQuads;

    MemoryCardBakedModel(VertexFormat format, IBakedModel baseModel, TextureAtlasSprite texture) {
        this(format, baseModel, texture, DEFAULT_COLOR_CODE, createCache());
    }

    private MemoryCardBakedModel(VertexFormat format, IBakedModel baseModel, TextureAtlasSprite texture, AEColor[] hash, Cache<CacheKey, MemoryCardBakedModel> modelCache) {
        this.format = format;
        this.baseModel = baseModel;
        this.texture = texture;
        this.colorCode = hash;
        this.generalQuads = ImmutableList.copyOf(this.buildGeneralQuads());
        this.modelCache = modelCache;
    }

    private static Cache<CacheKey, MemoryCardBakedModel> createCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(100)
                .build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

        List<BakedQuad> quads = this.baseModel.getQuads(state, side, rand);

        if (side != null) {
            return quads;
        }

        List<BakedQuad> result = new ArrayList<>(quads.size() + this.generalQuads.size());
        result.addAll(quads);
        result.addAll(this.generalQuads);
        return result;
    }

    private List<BakedQuad> buildGeneralQuads() {
        CubeBuilder builder = new CubeBuilder(this.format);

        builder.setTexture(this.texture);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                final AEColor color = this.colorCode[x + y * 4];

                builder.setColorRGB(color.mediumVariant);
                builder.addCube(7 + x, 8 + (1 - y), 7.5f, 7 + x + 1, 8 + (1 - y) + 1, 8.5f);
            }
        }

        return builder.getOutput();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return this.baseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return this.baseModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.baseModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.baseModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
                try {
                    if (stack.getItem() instanceof IMemoryCard) {
                        final IMemoryCard memoryCard = (IMemoryCard) stack.getItem();
                        final AEColor[] colors = memoryCard.getColorCode(stack);

                        return MemoryCardBakedModel.this.modelCache.get(new CacheKey(colors),
                                () -> new MemoryCardBakedModel(MemoryCardBakedModel.this.format, MemoryCardBakedModel.this.baseModel, MemoryCardBakedModel.this.texture, colors, MemoryCardBakedModel.this.modelCache));
                    }
                } catch (ExecutionException e) {
                    AELog.error(e);
                }

                return MemoryCardBakedModel.this;
            }
        };

    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
        // Delegate to the base model if possible
        if (this.baseModel instanceof IBakedModel) {
            IBakedModel pam = this.baseModel;
            Pair<? extends IBakedModel, Matrix4f> pair = pam.handlePerspective(type);
            return Pair.of(this, pair.getValue());
        }
        return Pair.of(this, TRSRTransformation.identity().getMatrix());
    }

    private static class CacheKey {
        private final AEColor[] key;

        CacheKey(AEColor[] key) {
            this.key = key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.key);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            return Arrays.equals(this.key, other.key);
        }

    }
}
