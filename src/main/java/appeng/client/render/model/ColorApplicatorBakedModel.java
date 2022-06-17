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

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This baked model will take the generated item model for the colored color applicator, and associate tint indices with
 * the added layers that correspond to the light/medium/dark variants of the {@link appeng.api.util.AEColor}.
 * <p>
 * Using the color provider registered in {@link appeng.items.tools.powered.ColorApplicatorItemRendering}, this results
 * in the right color being multiplied with the corresponding layer.
 */
class ColorApplicatorBakedModel extends ForwardingBakedModel {

    private final EnumMap<Direction, List<BakedQuad>> quadsBySide;

    private final List<BakedQuad> generalQuads;

    ColorApplicatorBakedModel(BakedModel baseModel, TextureAtlasSprite texDark, TextureAtlasSprite texMedium,
            TextureAtlasSprite texBright) {
        this.wrapped = baseModel;

        // Put the tint indices in... Since this is an item model, we are ignoring rand
        this.generalQuads = this.fixQuadTint(null, texDark, texMedium, texBright);
        this.quadsBySide = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.values()) {
            this.quadsBySide.put(facing, this.fixQuadTint(facing, texDark, texMedium, texBright));
        }
    }

    private List<BakedQuad> fixQuadTint(Direction facing, TextureAtlasSprite texDark, TextureAtlasSprite texMedium,
            TextureAtlasSprite texBright) {
        List<BakedQuad> quads = this.wrapped.getQuads(null, facing, RandomSource.create(0));
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

}
