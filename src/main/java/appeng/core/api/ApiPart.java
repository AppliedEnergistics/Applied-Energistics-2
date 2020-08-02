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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import appeng.api.parts.CableRenderMode;
import appeng.api.parts.IPartHelper;
import appeng.core.AppEng;
import appeng.parts.PartPlacement;

public class ApiPart implements IPartHelper {

    @Override
    public ActionResult placeBus(final ItemStack is, final BlockPos pos, final Direction side,
            final PlayerEntity player, final Hand hand, final World w) {
        return PartPlacement.place(is, pos, side, player, hand, w, PartPlacement.PlaceType.PLACE_ITEM, 0);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        return AppEng.instance().getCableRenderMode();
    }
}
