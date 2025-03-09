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

import java.util.Collection;
import java.util.Collections;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * A color applicator uses the base model, and extends it with additional layers that are colored according to the
 * selected color of the applicator.
 */
public class ColorApplicatorModel implements BasicUnbakedModel {

    private static final ResourceLocation MODEL_BASE = AppEng.makeId(
            "item/color_applicator_colored");

    private static final Material TEXTURE_DARK = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("item/color_applicator_tip_dark"));
    private static final Material TEXTURE_MEDIUM = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("item/color_applicator_tip_medium"));
    private static final Material TEXTURE_BRIGHT = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("item/color_applicator_tip_bright"));

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Override
    public BakedModel bake(TextureSlots textures, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion,
            boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties) {
        BakedModel baseModel = baker.bake(MODEL_BASE, modelState);

        TextureAtlasSprite texDark = baker.sprites().get(TEXTURE_DARK);
        TextureAtlasSprite texMedium = baker.sprites().get(TEXTURE_MEDIUM);
        TextureAtlasSprite texBright = baker.sprites().get(TEXTURE_BRIGHT);

        return new ColorApplicatorBakedModel(baseModel, texDark, texMedium, texBright);
    }
}
