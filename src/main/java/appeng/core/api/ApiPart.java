/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.api;

import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import appeng.api.parts.CableRenderMode;
import appeng.api.parts.IPartHelper;
import appeng.core.AppEng;
import appeng.parts.PartPlacement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ApiPart implements IPartHelper {

    @Override
    public InteractionResult placeBus(final ItemStack is, final BlockPos pos, final Direction side,
                                      final Player player, final InteractionHand hand, final Level w) {
        return PartPlacement.place(is, pos, side, player, hand, w, PartPlacement.PlaceType.PLACE_ITEM, 0);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        return AppEng.instance().getCableRenderMode();
    }
}
