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

import net.minecraft.client.color.block.BlockColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEColor;
import appeng.block.networking.CableBusColor;
import appeng.client.render.ColorableTileBlockColor;
import appeng.client.render.StaticBlockColor;
import appeng.core.definitions.AEBlocks;

@OnlyIn(Dist.CLIENT)
public final class InitBlockColors {

    private InitBlockColors() {
    }

    public static void init(BlockColors blockColors) {
        blockColors.register(new StaticBlockColor(AEColor.TRANSPARENT), AEBlocks.WIRELESS_ACCESS_POINT.block());
        blockColors.register(new CableBusColor(), AEBlocks.MULTI_PART.block());
        blockColors.register(ColorableTileBlockColor.INSTANCE, AEBlocks.SECURITY_STATION.block());
        blockColors.register(new ColorableTileBlockColor(), AEBlocks.CHEST.block());
    }

}
