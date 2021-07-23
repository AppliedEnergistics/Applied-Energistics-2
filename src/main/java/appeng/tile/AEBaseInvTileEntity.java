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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;

public abstract class AEBaseInvTileEntity extends AEBaseTileEntity implements IAEAppEngInventory {

    public AEBaseInvTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void load(BlockState blockState, final CompoundNBT data) {
        super.load(blockState, data);
        final IItemHandler inv = this.getInternalInventory();
        if (inv != EmptyHandler.INSTANCE) {
            final CompoundNBT opt = data.getCompound("inv");
            for (int x = 0; x < inv.getSlots(); x++) {
                final CompoundNBT item = opt.getCompound("item" + x);
                ItemHandlerUtil.setStackInSlot(inv, x, ItemStack.of(item));
            }
        }
    }

    public abstract @Nonnull IItemHandler getInternalInventory();

    @Override
    public CompoundNBT save(final CompoundNBT data) {
        super.save(data);
        final IItemHandler inv = this.getInternalInventory();
        if (inv != EmptyHandler.INSTANCE) {
            final CompoundNBT opt = new CompoundNBT();
            for (int x = 0; x < inv.getSlots(); x++) {
                final CompoundNBT item = new CompoundNBT();
                final ItemStack is = inv.getStackInSlot(x);
                if (!is.isEmpty()) {
                    is.save(item);
                }
                opt.put("item" + x, item);
            }
            data.put("inv", opt);
        }
        return data;
    }

    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        final IItemHandler inv = this.getInternalInventory();

        for (int l = 0; l < inv.getSlots(); l++) {
            final ItemStack is = inv.getStackInSlot(l);
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public abstract void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed,
            ItemStack added);

    protected @Nonnull IItemHandler getItemHandlerForSide(@Nonnull Direction side) {
        return this.getInternalInventory();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return (LazyOptional<T>) LazyOptional.of(this::getInternalInventory);
            } else {
                return (LazyOptional<T>) LazyOptional.of(() -> getItemHandlerForSide(facing));
            }
        }
        return super.getCapability(capability, facing);
    }

}
