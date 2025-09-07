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

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.IItemHandler;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public abstract class AEBaseInvBlockEntity extends AEBaseBlockEntity implements InternalInventoryHost {

    public AEBaseInvBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void loadTag(ValueInput data) {
        super.loadTag(data);
        var inv = this.getInternalInventory();
        if (inv != InternalInventory.empty()) {
            var opt = data.child("inv").orElse(null);
            for (int x = 0; x < inv.size(); x++) {
                ItemStack item = ItemStack.EMPTY;
                if (opt != null) {
                    item = opt.read("item" + x, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
                }
                inv.setItemDirect(x, item);
            }
        }
    }

    public abstract InternalInventory getInternalInventory();

    @Override
    public void saveAdditional(ValueOutput data) {
        super.saveAdditional(data);
        var inv = this.getInternalInventory();
        if (inv != InternalInventory.empty()) {
            var opt = data.child("inv");
            for (int x = 0; x < inv.size(); x++) {
                var is = inv.getStackInSlot(x);
                if (!is.isEmpty()) {
                    opt.store("item" + x, ItemStack.OPTIONAL_CODEC, is);
                }
            }
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        var inv = getInternalInventory();
        for (var stack : inv) {
            var genericStack = GenericStack.unwrapItemStack(stack);
            if (genericStack != null) {
                genericStack.what().addDrops(
                        genericStack.amount(),
                        drops,
                        level,
                        pos);
            } else {
                drops.add(stack);
            }
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        getInternalInventory().clear();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
    }

    protected InternalInventory getExposedInventoryForSide(Direction side) {
        return this.getInternalInventory();
    }

    @Nullable
    public IItemHandler getExposedItemHandler(@Nullable Direction side) {
        if (side == null) {
            return getInternalInventory().toItemHandler();
        } else {
            var exposed = getExposedInventoryForSide(side);
            // If the inventory has 0 slots, it's probably a dummy.
            // Return null to avoid pipe connections to it.
            // isEmpty checks for stacks, use size to only check the slot count.
            // noinspection SizeReplaceableByIsEmpty
            return exposed.size() == 0 ? null : exposed.toItemHandler();
        }
    }

}
