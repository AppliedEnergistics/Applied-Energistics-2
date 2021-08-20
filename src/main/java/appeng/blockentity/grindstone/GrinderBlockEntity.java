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

package appeng.blockentity.grindstone;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.recipes.handlers.GrinderOptionalResult;
import appeng.recipes.handlers.GrinderRecipe;
import appeng.recipes.handlers.GrinderRecipes;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class GrinderBlockEntity extends AEBaseInvBlockEntity implements ICrankable {
    private static final int SLOT_PROCESSING = 6;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 7);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new GrinderFilter());
    private int points;

    public GrinderBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        final BlockState state = this.level.getBlockState(this.worldPosition);
        state.getBlock().neighborChanged(state, this.level, this.worldPosition, state.getBlock(), this.worldPosition,
                false);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction side) {
        return this.invExt;
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removed, final ItemStack added) {

    }

    @Override
    public boolean canTurn() {
        if (isRemote()) {
            return false;
        }

        if (this.inv.getStackInSlot(6).isEmpty()) // Add if there isn't one...
        {
            for (int x = 0; x < 3; x++) {
                ItemStack item = this.inv.getStackInSlot(x);
                if (item.isEmpty()) {
                    continue;
                }

                GrinderRecipe r = GrinderRecipes.findForInput(level, item);
                if (r != null) {
                    final ItemStack ais = item.copy();
                    ais.setCount(r.getIngredientCount());
                    item.shrink(r.getIngredientCount());

                    if (item.getCount() <= 0) {
                        item = ItemStack.EMPTY;
                    }

                    this.inv.setItemDirect(x, item);
                    this.inv.setItemDirect(6, ais);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void applyTurn() {
        if (isRemote()) {
            return;
        }

        this.points++;

        final ItemStack processing = this.inv.getStackInSlot(SLOT_PROCESSING);
        GrinderRecipe r = GrinderRecipes.findForInput(level, processing);
        if (r != null) {
            if (r.getTurns() > this.points) {
                return;
            }

            this.points = 0;
            var outputSlots = this.inv.getSubInventory(3, 6);

            this.addItem(outputSlots, r.getResultItem());

            for (GrinderOptionalResult optionalResult : r.getOptionalResults()) {
                final float chance = Platform.getRandomInt() % 2000 / 2000.0f;

                if (chance <= optionalResult.getChance()) {
                    this.addItem(outputSlots, optionalResult.getResult());
                }
            }

            this.inv.setItemDirect(6, ItemStack.EMPTY);
        }
    }

    private void addItem(InternalInventory outputSlots, ItemStack output) {
        if (output.isEmpty()) {
            return;
        }

        final ItemStack notAdded = outputSlots.addItems(output);
        if (!notAdded.isEmpty()) {
            final List<ItemStack> out = new ArrayList<>();
            out.add(notAdded);

            Platform.spawnDrops(this.level, this.worldPosition.relative(this.getForward()), out);
        }
    }

    @Override
    public boolean canCrankAttach(final Direction directionToCrank) {
        return this.getUp() == directionToCrank;
    }

    private class GrinderFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slotIndex, int amount) {
            return slotIndex >= 3 && slotIndex <= 5;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slotIndex, ItemStack stack) {
            if (!GrinderRecipes.isValidIngredient(level, stack)) {
                return false;
            }

            return slotIndex >= 0 && slotIndex <= 2;
        }
    }

}
