package appeng.client.render.model;


import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;


class ColorApplicatorBakedModel implements IBakedModel {

    private final IBakedModel baseModel;

    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;

    private final EnumMap<EnumFacing, List<BakedQuad>> quadsBySide;

    private final List<BakedQuad> generalQuads;

    ColorApplicatorBakedModel(IBakedModel baseModel, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> map, TextureAtlasSprite texDark, TextureAtlasSprite texMedium, TextureAtlasSprite texBright) {
        this.baseModel = baseModel;
        this.transforms = map;

        // Put the tint indices in... Since this is an item model, we are ignoring rand
        this.generalQuads = this.fixQuadTint(null, texDark, texMedium, texBright);
        this.quadsBySide = new EnumMap<>(EnumFacing.class);
        for (EnumFacing facing : EnumFacing.values()) {
            this.quadsBySide.put(facing, this.fixQuadTint(facing, texDark, texMedium, texBright));
        }
    }

    private List<BakedQuad> fixQuadTint(EnumFacing facing, TextureAtlasSprite texDark, TextureAtlasSprite texMedium, TextureAtlasSprite texBright) {
        List<BakedQuad> quads = this.baseModel.getQuads(null, facing, 0);
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            int tint;

            if (quad.getSprite() == texDark) {
                tint = 1;
            } else if (quad.getSprite() == texMedium) {
                tint = 2;
            } else if (quad.getSprite() == texBright) {
                tint = 3;
            } else {
                result.add(quad);
                continue;
            }

            BakedQuad newQuad = new BakedQuad(quad.getVertexData(), tint, quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad
                    .getFormat());
            result.add(newQuad);
        }

        return result;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side == null) {
            return this.generalQuads;
        }
        return this.quadsBySide.get(side);
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
        return this.baseModel.getOverrides();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
        return PerspectiveMapWrapper.handlePerspective(this, this.transforms, type);
    }
}
