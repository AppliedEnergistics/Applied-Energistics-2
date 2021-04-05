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

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

public class QnbFormedModel implements BasicUnbakedModel {

    private static final ResourceLocation MODEL_RING = new ResourceLocation(AppEng.MOD_ID, "block/qnb/ring");

    @Nullable
    @Override
    public IBakedModel bakeModel(ModelBakery loader, Function<RenderMaterial, TextureAtlasSprite> textureGetter,
            IModelTransform rotationContainer, ResourceLocation modelId) {
        IBakedModel ringModel = loader.bake(MODEL_RING, rotationContainer);
        return new QnbFormedBakedModel(ringModel, textureGetter);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.of(MODEL_RING);
    }

    @Override
    public Stream<RenderMaterial> getAdditionalTextures() {
        return QnbFormedBakedModel.getRequiredTextures().stream();
    }

}
