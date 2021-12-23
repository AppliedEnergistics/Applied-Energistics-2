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

package appeng.blockentity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import appeng.util.inv.InternalInventoryHost;

public abstract class AEBaseInvBlockEntity extends AEBaseBlockEntity implements InternalInventoryHost {

    public AEBaseInvBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        var inv = this.getInternalInventory();
        if (inv != InternalInventory.empty()) {
            var opt = data.getCompound("inv");
            for (int x = 0; x < inv.size(); x++) {
                var item = opt.getCompound("item" + x);
                inv.setItemDirect(x, ItemStack.of(item));
            }
        }
    }

    public abstract InternalInventory getInternalInventory();

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        var inv = this.getInternalInventory();
        if (inv != InternalInventory.empty()) {
            final CompoundTag opt = new CompoundTag();
            for (int x = 0; x < inv.size(); x++) {
                final CompoundTag item = new CompoundTag();
                final ItemStack is = inv.getStackInSlot(x);
                if (!is.isEmpty()) {
                    is.save(item);
                }
                opt.put("item" + x, item);
            }
            data.put("inv", opt);
        }
    }

    @Override
    public void getDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        for (var stack : getInternalInventory()) {
            // Wrapped fluid stacks are internal implementation details and should NEVER drop
            if (GenericStack.isWrapped(stack)) {
                continue;
            }

            drops.add(stack);
        }
    }

    @Override
    public abstract void onChangeInventory(InternalInventory inv, int slot);

    public InternalInventory getExposedInventoryForSide(Direction side) {
        return this.getInternalInventory();
    }

}
