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


import appeng.entity.EntityTinyTNTPrimed;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;


public final class DispenserBehaviorTinyTNT extends BehaviorDefaultDispenseItem {

    @Override
    protected ItemStack dispenseStack(final IBlockSource dispenser, final ItemStack dispensedItem) {
        final EnumFacing enumfacing = dispenser.getBlockState().getValue(BlockDispenser.FACING);
        final World world = dispenser.getWorld();
        final int i = dispenser.getBlockPos().getX() + enumfacing.getFrontOffsetX();
        final int j = dispenser.getBlockPos().getY() + enumfacing.getFrontOffsetY();
        final int k = dispenser.getBlockPos().getZ() + enumfacing.getFrontOffsetZ();
        final EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed(world, i + 0.5F, j + 0.5F, k + 0.5F, null);
        world.spawnEntity(primedTinyTNTEntity);
        dispensedItem.setCount(dispensedItem.getCount() - 1);
        return dispensedItem;
    }
}
