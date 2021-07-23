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

package appeng.client.render.cablebus;

import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import appeng.client.render.BasicUnbakedModel;
import appeng.core.AppEng;

public class P2PTunnelFrequencyModel implements BasicUnbakedModel<P2PTunnelFrequencyModel> {
    private static final Material TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS,
            new net.minecraft.resources.ResourceLocation(AppEng.MOD_ID, "part/p2p_tunnel_frequency"));

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
                           ItemOverrides overrides, ResourceLocation modelLocation) {
        try {
            final TextureAtlasSprite texture = spriteGetter.apply(TEXTURE);
            return new P2PTunnelFrequencyBakedModel(texture);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<Material> getAdditionalTextures() {
        return Stream.of(TEXTURE);
    }

}
