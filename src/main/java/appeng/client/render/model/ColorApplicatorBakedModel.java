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

package appeng.client.render.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

class ColorApplicatorBakedModel implements BakedModel {

    private final BakedModel baseModel;

    private final ModelState transforms;

    private final EnumMap<Direction, List<BakedQuad>> quadsBySide;

    private final List<BakedQuad> generalQuads;

    ColorApplicatorBakedModel(BakedModel baseModel, ModelState transforms, TextureAtlasSprite texDark,
            TextureAtlasSprite texMedium, TextureAtlasSprite texBright) {
        this.baseModel = baseModel;
        this.transforms = transforms;

        // Put the tint indices in... Since this is an item model, we are ignoring rand
        this.generalQuads = this.fixQuadTint(null, texDark, texMedium, texBright);
        this.quadsBySide = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.values()) {
            this.quadsBySide.put(facing, this.fixQuadTint(facing, texDark, texMedium, texBright));
        }
    }

    private List<BakedQuad> fixQuadTint(Direction facing, TextureAtlasSprite texDark, TextureAtlasSprite texMedium,
            TextureAtlasSprite texBright) {
        List<BakedQuad> quads = this.baseModel.getQuads(null, facing, RandomSource.create(0), ModelData.EMPTY, null);
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

            BakedQuad newQuad = new BakedQuad(quad.getVertices(), tint, quad.getDirection(), quad.getSprite(),
                    quad.isShade());
            result.add(newQuad);
        }

        return result;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
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
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return false;// TODO
    }

    @Override
    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.baseModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.baseModel.getOverrides();
    }

    @Override
    public BakedModel applyTransform(TransformType transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        baseModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
        return this;
    }
}
