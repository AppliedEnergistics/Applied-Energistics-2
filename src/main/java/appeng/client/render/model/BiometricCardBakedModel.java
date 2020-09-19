package appeng.client.render.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;

import appeng.api.implementations.items.IBiometricCard;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AELog;

class BiometricCardBakedModel implements IBakedModel {

    private final IBakedModel baseModel;

    private final TextureAtlasSprite texture;

    private final int hash;

    private final Cache<Integer, BiometricCardBakedModel> modelCache;

    private final ImmutableList<BakedQuad> generalQuads;

    BiometricCardBakedModel(IBakedModel baseModel, TextureAtlasSprite texture) {
        this(baseModel, texture, 0, createCache());
    }

    private BiometricCardBakedModel(IBakedModel baseModel, TextureAtlasSprite texture, int hash,
            Cache<Integer, BiometricCardBakedModel> modelCache) {
        this.baseModel = baseModel;
        this.texture = texture;
        this.hash = hash;
        this.generalQuads = ImmutableList.copyOf(this.buildGeneralQuads());
        this.modelCache = modelCache;
    }

    private static Cache<Integer, BiometricCardBakedModel> createCache() {
        return CacheBuilder.newBuilder().maximumSize(100).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {

        List<BakedQuad> quads = this.baseModel.getQuads(state, side, rand, EmptyModelData.INSTANCE);

        if (side != null) {
            return quads;
        }

        List<BakedQuad> result = new ArrayList<>(quads.size() + this.generalQuads.size());
        result.addAll(quads);
        result.addAll(this.generalQuads);
        return result;
    }

    private List<BakedQuad> buildGeneralQuads() {
        CubeBuilder builder = new CubeBuilder();

        builder.setTexture(this.texture);

        AEColor col = AEColor.values()[Math.abs(3 + this.hash) % AEColor.values().length];
        if (this.hash == 0) {
            col = AEColor.BLACK;
        }

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 6; y++) {
                final boolean isLit;

                // This makes the border always use the darker color
                if (x == 0 || y == 0 || x == 7 || y == 5) {
                    isLit = false;
                } else {
                    isLit = (this.hash & (1 << x)) != 0 || (this.hash & (1 << y)) != 0;
                }

                if (isLit) {
                    builder.setColorRGB(col.mediumVariant);
                } else {
                    final float scale = 0.3f / 255.0f;
                    builder.setColorRGB(((col.blackVariant >> 16) & 0xff) * scale,
                            ((col.blackVariant >> 8) & 0xff) * scale, (col.blackVariant & 0xff) * scale);
                }

                builder.addCube(4 + x, 6 + y, 7.5f, 4 + x + 1, 6 + y + 1, 8.5f);
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
    public boolean isSideLit() {
        return false;// TODO
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
        return new ItemOverrideList() {
            @Override
            public IBakedModel getOverrideModel(IBakedModel originalModel, ItemStack stack, ClientWorld world,
                    LivingEntity entity) {
                String username = "";
                if (stack.getItem() instanceof IBiometricCard) {
                    final GameProfile gp = ((IBiometricCard) stack.getItem()).getProfile(stack);
                    if (gp != null) {
                        if (gp.getId() != null) {
                            username = gp.getId().toString();
                        } else {
                            username = gp.getName();
                        }
                    }
                }
                final int hash = !username.isEmpty() ? username.hashCode() : 0;

                // Get hash
                if (hash == 0) {
                    return BiometricCardBakedModel.this;
                }

                try {
                    return BiometricCardBakedModel.this.modelCache.get(hash,
                            () -> new BiometricCardBakedModel(BiometricCardBakedModel.this.baseModel,
                                    BiometricCardBakedModel.this.texture, hash,
                                    BiometricCardBakedModel.this.modelCache));
                } catch (ExecutionException e) {
                    AELog.error(e);
                    return BiometricCardBakedModel.this;
                }
            }
        };
    }

    @Override
    public boolean doesHandlePerspectives() {
        return true;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
        baseModel.handlePerspective(cameraTransformType, mat);
        return this;
    }
}
