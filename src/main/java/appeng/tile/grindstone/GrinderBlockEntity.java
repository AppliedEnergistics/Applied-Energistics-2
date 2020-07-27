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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.implementations.tiles.ICrankable;
import appeng.recipes.handlers.GrinderOptionalResult;
import appeng.recipes.handlers.GrinderRecipe;
import appeng.recipes.handlers.GrinderRecipes;
import appeng.tile.AEBaseInvBlockEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorFixedInv;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.IAEItemFilter;

public class GrinderBlockEntity extends AEBaseInvBlockEntity implements ICrankable {
    private static final int SLOT_PROCESSING = 6;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 7);
    private final FixedItemInv invExt = new WrapperFilteredItemHandler(this.inv, new GrinderFilter());
    private int points;

    public GrinderBlockEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        final BlockState state = this.world.getBlockState(this.pos);
        state.getBlock().neighborUpdate(state, this.world, this.pos, state.getBlock(), this.pos, false);
    }

    @Override
    public FixedItemInv getInternalInventory() {
        return this.inv;
    }

    @Override
    protected FixedItemInv getItemHandlerForSide(Direction side) {
        return this.invExt;
    }

    @Override
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {

    }

    @Override
    public boolean canTurn() {
        if (isClient()) {
            return false;
        }

        if (this.inv.getInvStack(6).isEmpty()) // Add if there isn't one...
        {
            for (int x = 0; x < 3; x++) {
                ItemStack item = this.inv.getInvStack(x);
                if (item.isEmpty()) {
                    continue;
                }

                GrinderRecipe r = GrinderRecipes.findForInput(world, item);
                if (r != null) {
                    final ItemStack ais = item.copy();
                    ais.setCount(r.getIngredientCount());
                    item.decrement(r.getIngredientCount());

                    if (item.getCount() <= 0) {
                        item = ItemStack.EMPTY;
                    }

                    this.inv.setInvStack(x, item, Simulation.ACTION);
                    this.inv.setInvStack(6, ais, Simulation.ACTION);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void applyTurn() {
        if (isClient()) {
            return;
        }

        this.points++;

        final ItemStack processing = this.inv.getInvStack(SLOT_PROCESSING);
        GrinderRecipe r = GrinderRecipes.findForInput(world, processing);
        if (r != null) {
            if (r.getTurns() > this.points) {
                return;
            }

            this.points = 0;
            final InventoryAdaptor sia = new AdaptorFixedInv(this.inv.getSubInv(3, 6));

            this.addItem(sia, r.getOutput());

            for (GrinderOptionalResult optionalResult : r.getOptionalResults()) {
                final float chance = (Platform.getRandomInt() % 2000) / 2000.0f;

                if (chance <= optionalResult.getChance()) {
                    this.addItem(sia, optionalResult.getResult());
                }
            }

            this.inv.setInvStack(6, ItemStack.EMPTY, Simulation.ACTION);
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
    public boolean canCrankAttach(final Direction directionToCrank) {
        return this.getUp() == directionToCrank;
    }

    private class GrinderFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(FixedItemInv inv, int slotIndex, int amount) {
            return slotIndex >= 3 && slotIndex <= 5;
        }

        @Override
        public boolean allowInsert(FixedItemInv inv, int slotIndex, ItemStack stack) {
            if (!GrinderRecipes.isValidIngredient(world, stack)) {
                return false;
            }

            return slotIndex >= 0 && slotIndex <= 2;
        }
    }

}
