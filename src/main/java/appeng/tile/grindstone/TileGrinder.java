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

package appeng.tile.grindstone;


import appeng.api.AEApi;
import appeng.api.features.IGrinderRecipe;
import appeng.api.implementations.tiles.ICrankable;
import appeng.tile.AEBaseInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import java.util.ArrayList;
import java.util.List;


public class TileGrinder extends AEBaseInvTile implements ICrankable {
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 7);
    private final IItemHandler invExt = new WrapperFilteredItemHandler(this.inv, new GrinderFilter());
    private int points;

    @Override
    public void setOrientation(final EnumFacing inForward, final EnumFacing inUp) {
        super.setOrientation(inForward, inUp);
        final IBlockState state = this.world.getBlockState(this.pos);
        this.getBlockType().neighborChanged(state, this.world, this.pos, state.getBlock(), this.pos);
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    protected IItemHandler getItemHandlerForSide(EnumFacing side) {
        return this.invExt;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {

    }

    @Override
    public boolean canTurn() {
        if (Platform.isClient()) {
            return false;
        }

        if (this.inv.getStackInSlot(6).isEmpty()) // Add if there isn't one...
        {
            for (int x = 0; x < 3; x++) {
                ItemStack item = this.inv.getStackInSlot(x);
                if (item.isEmpty()) {
                    continue;
                }

                final IGrinderRecipe r = AEApi.instance().registries().grinder().getRecipeForInput(item);
                if (r != null) {
                    if (item.getCount() >= r.getInput().getCount()) {
                        final ItemStack ais = item.copy();
                        ais.setCount(r.getInput().getCount());
                        item.shrink(r.getInput().getCount());

                        if (item.getCount() <= 0) {
                            item = ItemStack.EMPTY;
                        }

                        this.inv.setStackInSlot(x, item);
                        this.inv.setStackInSlot(6, ais);
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void applyTurn() {
        if (Platform.isClient()) {
            return;
        }

        this.points++;

        final ItemStack processing = this.inv.getStackInSlot(6);
        final IGrinderRecipe r = AEApi.instance().registries().grinder().getRecipeForInput(processing);
        if (r != null) {
            if (r.getRequiredTurns() > this.points) {
                return;
            }

            this.points = 0;
            final InventoryAdaptor sia = new AdaptorItemHandler(new RangedWrapper(this.inv, 3, 6));

            this.addItem(sia, r.getOutput());

            r.getOptionalOutput().ifPresent(itemStack ->
            {
                final float chance = (Platform.getRandomInt() % 2000) / 2000.0f;

                if (chance <= r.getOptionalChance()) {
                    this.addItem(sia, itemStack);
                }
            });

            r.getSecondOptionalOutput().ifPresent(itemStack ->
            {
                final float chance = (Platform.getRandomInt() % 2000) / 2000.0f;

                if (chance <= r.getSecondOptionalChance()) {
                    this.addItem(sia, itemStack);
                }
            });

            this.inv.setStackInSlot(6, ItemStack.EMPTY);
        }
    }

    private void addItem(final InventoryAdaptor sia, final ItemStack output) {
        if (output.isEmpty()) {
            return;
        }

        final ItemStack notAdded = sia.addItems(output);
        if (!notAdded.isEmpty()) {
            final List<ItemStack> out = new ArrayList<>();
            out.add(notAdded);

            Platform.spawnDrops(this.world, this.pos.offset(this.getForward()), out);
        }
    }

    @Override
    public boolean canCrankAttach(final EnumFacing directionToCrank) {
        return this.getUp() == directionToCrank;
    }

    private class GrinderFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(IItemHandler inv, int slotIndex, int amount) {
            return slotIndex >= 3 && slotIndex <= 5;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slotIndex, ItemStack stack) {
            if (AEApi.instance().registries().grinder().getRecipeForInput(stack) == null) {
                return false;
            }

            return slotIndex >= 0 && slotIndex <= 2;
        }
    }

}
