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

package appeng.block.qnb;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

public class QnbFormedModel implements BasicUnbakedModel<QnbFormedModel> {

    private static final ResourceLocation MODEL_RING = new ResourceLocation(AppEng.MOD_ID, "block/qnb/ring");

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
            ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel ringModel = bakery.bake(MODEL_RING, modelTransform, spriteGetter);
        return new QnbFormedBakedModel(ringModel, spriteGetter);
    }

    @Override
    public Collection<ResourceLocation> getModelDependencies() {
        return ImmutableSet.of(MODEL_RING);
    }

    @Override
    public Stream<Material> getAdditionalTextures() {
        return QnbFormedBakedModel.getRequiredTextures().stream();
    }

}
