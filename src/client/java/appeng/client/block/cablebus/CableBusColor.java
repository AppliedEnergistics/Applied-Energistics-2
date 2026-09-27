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

package appeng.client.block.cablebus;

import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.util.AEColor;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.CableBusContainer;

/**
 * Exposes the cable bus color as tint indices 0 (dark variant), 1 (medium variant) and 2 (bright variant).
 */
public class CableBusColor implements BlockTintSource {

    public static List<BlockTintSource> TINT_SOURCES = IntStream.range(0, 5)
            .<BlockTintSource>mapToObj(CableBusColor::new)
            .toList();

    private final int tintIndex;

    public CableBusColor(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    @Override
    public int color(BlockState state) {
        return AEColor.TRANSPARENT.getVariantByTintIndex(tintIndex);
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {

        AEColor busColor = AEColor.TRANSPARENT;

        if (level != null && pos != null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CableBusBlockEntity) {
                CableBusContainer container = ((CableBusBlockEntity) blockEntity).getCableBus();
                busColor = container.getColor();
            }
        }

        return ARGB.opaque(busColor.getVariantByTintIndex(tintIndex));

    }
}
