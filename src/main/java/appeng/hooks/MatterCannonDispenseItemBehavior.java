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

package appeng.hooks;

import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import appeng.api.util.AEPartLocation;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.util.Platform;

public final class MatterCannonDispenseItemBehavior extends DefaultDispenseItemBehavior {

    @Override
    protected ItemStack execute(final BlockSource dispenser, ItemStack dispensedItem) {
        final Item i = dispensedItem.getItem();
        if (i instanceof MatterCannonItem) {
            final Direction Direction = dispenser.getBlockState().getValue(DispenserBlock.FACING);
            AEPartLocation dir = AEPartLocation.INTERNAL;
            for (final AEPartLocation d : AEPartLocation.SIDE_LOCATIONS) {
                if (Direction.getStepX() == d.xOffset && Direction.getStepY() == d.yOffset
                        && Direction.getStepZ() == d.zOffset) {
                    dir = d;
                }
            }

            final MatterCannonItem tm = (MatterCannonItem) i;

            final Level w = dispenser.getLevel();
            if (w instanceof ServerLevel) {
                final Player p = Platform.getPlayer((ServerLevel) w);
                Platform.configurePlayer(p, dir, dispenser.getEntity());

                p.setPos(p.getX() + dir.xOffset, p.getY() + dir.yOffset, p.getZ() + dir.zOffset);

                dispensedItem = tm.use(w, p, null).getObject();
            }
        }
        return dispensedItem;
    }
}
