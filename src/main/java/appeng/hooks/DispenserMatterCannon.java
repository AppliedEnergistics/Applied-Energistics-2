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


import appeng.api.util.AEPartLocation;
import appeng.items.tools.powered.ToolMatterCannon;
import appeng.util.Platform;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;


public final class DispenserMatterCannon extends BehaviorDefaultDispenseItem {

    @Override
    protected ItemStack dispenseStack(final IBlockSource dispenser, ItemStack dispensedItem) {
        final Item i = dispensedItem.getItem();
        if (i instanceof ToolMatterCannon) {
            final EnumFacing enumfacing = dispenser.getBlockState().getValue(BlockDispenser.FACING);
            AEPartLocation dir = AEPartLocation.INTERNAL;
            for (final AEPartLocation d : AEPartLocation.SIDE_LOCATIONS) {
                if (enumfacing.getFrontOffsetX() == d.xOffset && enumfacing.getFrontOffsetY() == d.yOffset && enumfacing.getFrontOffsetZ() == d.zOffset) {
                    dir = d;
                }
            }

            final ToolMatterCannon tm = (ToolMatterCannon) i;

            final World w = dispenser.getWorld();
            if (w instanceof WorldServer) {
                final EntityPlayer p = Platform.getPlayer((WorldServer) w);
                Platform.configurePlayer(p, dir, dispenser.getBlockTileEntity());

                p.posX += dir.xOffset;
                p.posY += dir.yOffset;
                p.posZ += dir.zOffset;

                dispensedItem = tm.onItemRightClick(w, p, null).getResult();
            }
        }
        return dispensedItem;
    }
}
