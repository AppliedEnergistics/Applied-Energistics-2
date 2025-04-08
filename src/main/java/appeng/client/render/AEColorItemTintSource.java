/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.render;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import appeng.api.util.AEColor;
import appeng.api.util.AEColorVariant;
import appeng.core.AppEng;

/**
 * Returns the shades of a single AE color for tint indices 0, 1, and 2.
 */
public record AEColorItemTintSource(AEColor color, AEColorVariant variant) implements ItemTintSource {
    public static final ResourceLocation ID = AppEng.makeId("color");

    public static final MapCodec<AEColorItemTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                    AEColor.CODEC.fieldOf("color").forGetter(AEColorItemTintSource::color),
                    AEColorVariant.CODEC.fieldOf("variant").forGetter(AEColorItemTintSource::variant))
                    .apply(builder, AEColorItemTintSource::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        return ARGB.opaque(this.color.getVariant(variant));
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
