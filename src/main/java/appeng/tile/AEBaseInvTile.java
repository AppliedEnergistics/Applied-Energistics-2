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


import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public abstract class AEBaseInvTile extends AEBaseTile implements IAEAppEngInventory {

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        final IItemHandler inv = this.getInternalInventory();
        if (inv != EmptyHandler.INSTANCE) {
            final NBTTagCompound opt = data.getCompoundTag("inv");
            for (int x = 0; x < inv.getSlots(); x++) {
                final NBTTagCompound item = opt.getCompoundTag("item" + x);
                ItemHandlerUtil.setStackInSlot(inv, x, new ItemStack(item));
            }
        }
    }

    public abstract @Nonnull
    IItemHandler getInternalInventory();

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        final IItemHandler inv = this.getInternalInventory();
        if (inv != EmptyHandler.INSTANCE) {
            final NBTTagCompound opt = new NBTTagCompound();
            for (int x = 0; x < inv.getSlots(); x++) {
                final NBTTagCompound item = new NBTTagCompound();
                final ItemStack is = inv.getStackInSlot(x);
                if (!is.isEmpty()) {
                    is.writeToNBT(item);
                }
                opt.setTag("item" + x, item);
            }
            data.setTag("inv", opt);
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
    public abstract void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added);

    @Override
    public ITextComponent getDisplayName() {
        if (this.hasCustomInventoryName()) {
            return new TextComponentString(this.getCustomInventoryName());
        }
        return new TextComponentTranslation(this.getBlockType().getUnlocalizedName());
    }

    protected @Nonnull
    IItemHandler getItemHandlerForSide(@Nonnull EnumFacing side) {
        return this.getInternalInventory();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return this.getInternalInventory() != EmptyHandler.INSTANCE;
            } else {
                return this.getItemHandlerForSide(facing) != EmptyHandler.INSTANCE;
            }
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return (T) this.getInternalInventory();
            } else {
                return (T) this.getItemHandlerForSide(facing);
            }
        }
        return super.getCapability(capability, facing);
    }

}
