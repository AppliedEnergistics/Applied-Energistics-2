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

package appeng.init.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;

/**
 * Initializes which layers specific blocks render in.
 */
@OnlyIn(Dist.CLIENT)
public final class InitRenderTypes {

    /**
     * List of blocks that should render in the cutout layer.
     */
    private static final BlockDefinition[] CUTOUT_BLOCKS = {
            AEBlocks.CRAFTING_MONITOR,
            AEBlocks.SECURITY_STATION,
            AEBlocks.CONTROLLER,
            AEBlocks.MOLECULAR_ASSEMBLER,
            AEBlocks.QUARTZ_ORE_CHARGED,
            AEBlocks.QUARTZ_GLASS,
            AEBlocks.QUARTZ_VIBRANT_GLASS,
            AEBlocks.QUARTZ_FIXTURE,
            AEBlocks.LIGHT_DETECTOR,
            AEBlocks.WIRELESS_ACCESS_POINT,
            AEBlocks.PAINT,
            AEBlocks.QUANTUM_RING,
            AEBlocks.QUANTUM_LINK,
            AEBlocks.CHEST,
            AEBlocks.DRIVE,
            AEBlocks.CRAFTING_UNIT,
            AEBlocks.CRAFTING_ACCELERATOR,
            AEBlocks.CRAFTING_STORAGE_1K,
            AEBlocks.CRAFTING_STORAGE_4K,
            AEBlocks.CRAFTING_STORAGE_16K,
            AEBlocks.CRAFTING_STORAGE_64K,
            AEBlocks.SPATIAL_PYLON,
    };

    private InitRenderTypes() {
    }

    public static void init() {
        for (BlockDefinition definition : CUTOUT_BLOCKS) {
            RenderTypeLookup.setRenderLayer(definition.block(), RenderType.cutout());
        }

        // Cable bus multiblock renders in all layers
        RenderTypeLookup.setRenderLayer(AEBlocks.MULTI_PART.block(), rt -> true);
    }

}
