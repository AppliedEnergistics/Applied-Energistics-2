package appeng.client.render.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;

class ColorApplicatorBakedModel implements BakedModel {

    private final BakedModel baseModel;

    private final IModelTransform transforms;

    private final EnumMap<Direction, List<BakedQuad>> quadsBySide;

    private final List<BakedQuad> generalQuads;

    ColorApplicatorBakedModel(BakedModel baseModel, IModelTransform transforms, Sprite texDark,
                              Sprite texMedium, Sprite texBright) {
        this.baseModel = baseModel;
        this.transforms = transforms;

        // Put the tint indices in... Since this is an item model, we are ignoring rand
        this.generalQuads = this.fixQuadTint(null, texDark, texMedium, texBright);
        this.quadsBySide = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.values()) {
            this.quadsBySide.put(facing, this.fixQuadTint(facing, texDark, texMedium, texBright));
        }
    }

    private List<BakedQuad> fixQuadTint(Direction facing, Sprite texDark, Sprite texMedium,
                                        Sprite texBright) {
        List<BakedQuad> quads = this.baseModel.getQuads(null, facing, new Random(0), EmptyModelData.INSTANCE);
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            int tint;

            if (quad.func_187508_a() == texDark) {
                tint = 1;
            } else if (quad.func_187508_a() == texMedium) {
                tint = 2;
            } else if (quad.func_187508_a() == texBright) {
                tint = 3;
            } else {
                result.add(quad);
                continue;
            }

            BakedQuad newQuad = new BakedQuad(quad.getVertexData(), tint, quad.getFace(), quad.func_187508_a(),
                    quad.shouldApplyDiffuseLighting());
            result.add(newQuad);
        }

        return result;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        if (side == null) {
            return this.generalQuads;
        }
        return this.quadsBySide.get(side);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return this.baseModel.hasDepth();
    }

    @Override
    public boolean isSideLit() {
        return false;// TODO
    }

    @Override
    public boolean isBuiltin() {
        return this.baseModel.isBuiltin();
    }

    @Override
    public Sprite getSprite() {
        return this.baseModel.getSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return this.baseModel.getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return this.baseModel.getOverrides();
    }

    @Override
    public BakedModel handlePerspective(ModelTransformation.Mode cameraTransformType, MatrixStack mat) {
        return PerspectiveMapWrapper.handlePerspective(this, transforms, cameraTransformType, mat);
    }
}
