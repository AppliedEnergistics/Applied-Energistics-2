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
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

import appeng.items.tools.powered.MatterCannonItem;
import appeng.util.LookDirection;
import appeng.util.Platform;

public final class MatterCannonDispenseItemBehavior extends DefaultDispenseItemBehavior {

    @Override
    protected ItemStack execute(BlockSource source, ItemStack dispensedItem) {
        if (dispensedItem.getItem() instanceof MatterCannonItem tm) {
            var position = DispenserBlock.getDispensePosition(source);
            var direction = source.getBlockState().getValue(DispenserBlock.FACING);

            var src = new Vec3(
                    position.x(),
                    position.y(),
                    position.z());
            var dir = new LookDirection(
                    src,
                    src.add(
                            32 * direction.getStepX(),
                            32 * direction.getStepY(),
                            32 * direction.getStepZ()));

            var level = source.getLevel();
            var p = Platform.getPlayer(level);
            Platform.configurePlayer(p, direction, source.getEntity());
            tm.fireCannon(level, dispensedItem, p, dir);
        }
        return dispensedItem;
    }
}
