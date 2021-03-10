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

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import appeng.api.util.AEPartLocation;
import appeng.items.tools.powered.MatterCannonItem;
import appeng.util.Platform;

public final class MatterCannonDispenseItemBehavior extends DefaultDispenseItemBehavior {

    @Override
    protected ItemStack execute(final IBlockSource dispenser, ItemStack dispensedItem) {
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

            final World w = dispenser.getLevel();
            if (w instanceof ServerWorld) {
                final PlayerEntity p = Platform.getPlayer((ServerWorld) w);
                Platform.configurePlayer(p, dir, dispenser.getEntity());

                p.setPos(p.getX() + dir.xOffset, p.getY() + dir.yOffset, p.getZ() + dir.zOffset);

                dispensedItem = tm.use(w, p, null).getObject();
            }
        }
        return dispensedItem;
    }
}
