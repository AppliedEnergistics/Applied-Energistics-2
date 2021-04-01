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

package appeng.tile;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.impl.EmptyFixedItemInv;

import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;

public abstract class AEBaseInvTileEntity extends AEBaseTileEntity implements IAEAppEngInventory {

    public AEBaseInvTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void read(BlockState state, final CompoundNBT data) {
        super.read(state, data);
        final FixedItemInv inv = this.getInternalInventory();
        if (inv != EmptyFixedItemInv.INSTANCE) {
            final CompoundNBT opt = data.getCompound("inv");
            for (int x = 0; x < inv.getSlotCount(); x++) {
                final CompoundNBT item = opt.getCompound("item" + x);
                ItemHandlerUtil.setStackInSlot(inv, x, ItemStack.read(item));
            }
        }
    }

    public abstract @Nonnull FixedItemInv getInternalInventory();

    /**
     * Inventory offered to other mods accessing this block externally.
     */
    public @Nonnull FixedItemInv getExternalInventory() {
        return getInternalInventory();
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        final FixedItemInv inv = this.getInternalInventory();
        if (inv != EmptyFixedItemInv.INSTANCE) {
            final CompoundNBT opt = new CompoundNBT();
            for (int x = 0; x < inv.getSlotCount(); x++) {
                final CompoundNBT item = new CompoundNBT();
                final ItemStack is = inv.getInvStack(x);
                if (!is.isEmpty()) {
                    is.write(item);
                }
                opt.put("item" + x, item);
            }
            data.put("inv", opt);
        }
        return data;
    }

    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        final FixedItemInv inv = this.getInternalInventory();

        for (int l = 0; l < inv.getSlotCount(); l++) {
            final ItemStack is = inv.getInvStack(l);
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public abstract void onChangeInventory(FixedItemInv inv, int slot, InvOperation mc, ItemStack removed,
            ItemStack added);

    protected @Nonnull FixedItemInv getItemHandlerForSide(@Nonnull Direction side) {
        return this.getExternalInventory();
    }

    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
        super.addAllAttributes(world, pos, state, to);
        offerItemInventory(to);
    }

    private void offerItemInventory(AttributeList<?> to) {
        Direction searchDirection = to.getSearchDirection();
        if (searchDirection == null) {
            to.offer(getExternalInventory());
        } else {
            Direction side = searchDirection.getOpposite();
            to.offer(getItemHandlerForSide(side));
        }
    }

}
