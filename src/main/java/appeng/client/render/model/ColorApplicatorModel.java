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
import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

/**
 * A color applicator uses the base model, and extends it with additional layers that are colored according to the
 * selected color of the applicator.
 */
public class ColorApplicatorModel implements BasicUnbakedModel<ColorApplicatorModel> {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID,
            "item/color_applicator_colored");

    private static final Material TEXTURE_DARK = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_tip_dark"));
    private static final Material TEXTURE_MEDIUM = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_tip_medium"));
    private static final Material TEXTURE_BRIGHT = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation(AppEng.MOD_ID, "item/color_applicator_tip_bright"));

    @Override
    public Collection<ResourceLocation> getModelDependencies() {
        return Collections.singleton(MODEL_BASE);
    }

    @Override
    public Stream<Material> getAdditionalTextures() {
        return Stream.of(TEXTURE_DARK, TEXTURE_MEDIUM, TEXTURE_DARK);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
            ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel baseModel = bakery.bake(MODEL_BASE, modelTransform, spriteGetter);

        TextureAtlasSprite texDark = spriteGetter.apply(TEXTURE_DARK);
        TextureAtlasSprite texMedium = spriteGetter.apply(TEXTURE_MEDIUM);
        TextureAtlasSprite texBright = spriteGetter.apply(TEXTURE_BRIGHT);

        return new ColorApplicatorBakedModel(baseModel, modelTransform, texDark, texMedium, texBright);
    }
}
