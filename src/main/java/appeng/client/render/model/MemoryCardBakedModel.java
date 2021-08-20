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

import java.util.Random;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;

class MemoryCardBakedModel extends ForwardingBakedModel implements FabricBakedModel {
    private static final AEColor[] DEFAULT_COLOR_CODE = new AEColor[] { AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, };

    private final TextureAtlasSprite texture;

    public MemoryCardBakedModel(BakedModel baseModel, TextureAtlasSprite texture) {
        this.wrapped = baseModel;
        this.texture = texture;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

        context.fallbackConsumer().accept(wrapped);

        AEColor[] colorCode = getColorCode(stack);

        CubeBuilder builder = new CubeBuilder(context.getEmitter());

        builder.setTexture(this.texture);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                final AEColor color = colorCode[x + y * 4];

                builder.setColorRGB(color.mediumVariant);
                builder.addCube(7 + x, 8 + 1 - y, 7.5f, 7 + x + 1, 8 + 1 - y + 1, 8.5f);
            }
        }
    }

    private static AEColor[] getColorCode(ItemStack stack) {
        if (stack.getItem() instanceof IMemoryCard) {
            final IMemoryCard memoryCard = (IMemoryCard) stack.getItem();
            return memoryCard.getColorCode(stack);
        }

        return DEFAULT_COLOR_CODE;
    }

}
