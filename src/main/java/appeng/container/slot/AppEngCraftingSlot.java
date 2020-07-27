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

package appeng.container.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.util.helpers.ItemHandlerUtil;

public class AppEngCraftingSlot extends AppEngSlot {

    /**
     * The craft matrix inventory linked to this result slot.
     */
    private final FixedItemInv craftMatrix;

    /**
     * The player that is using the GUI where this slot resides.
     */
    private final PlayerEntity thePlayer;

    /**
     * The number of items that have been crafted so far. Gets passed to
     * ItemStack.onCrafted before being reset.
     */
    private int amountCrafted;

    public AppEngCraftingSlot(final PlayerEntity par1PlayerEntity, final FixedItemInv par2IInventory,
            final FixedItemInv par3IInventory, final int par4, final int par5, final int par6) {
        super(par3IInventory, par4, par5, par6);
        this.thePlayer = par1PlayerEntity;
        this.craftMatrix = par2IInventory;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the
     * armor slots.
     */
    @Override
    public boolean canInsert(final ItemStack par1ItemStack) {
        return false;
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
     * ore and wood. Typically increases an internal count then calls
     * onCrafted(item).
     */
    @Override
    protected void onCrafted(final ItemStack par1ItemStack, final int par2) {
        this.amountCrafted += par2;
        this.onCrafted(par1ItemStack);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
     * ore and wood.
     */
    @Override
    protected void onCrafted(final ItemStack par1ItemStack) {
        par1ItemStack.onCraft(this.thePlayer.world, this.thePlayer, this.amountCrafted);
        this.amountCrafted = 0;
    }

    @Override
    public ItemStack onTakeItem(final PlayerEntity playerIn, final ItemStack stack) {
        // FIXME FABRIC BasicEventHooks.firePlayerCraftingEvent(playerIn, stack, new
        // WrapperInvItemHandler(this.craftMatrix));
        this.onCrafted(stack);
        // FIXME FABRIC:
        // net.minecraftforge.common.ForgeHooks.setCraftingPlayer(playerIn);
        final CraftingInventory ic = new CraftingInventory(this.getContainer(), 3, 3);

        for (int x = 0; x < this.craftMatrix.getSlotCount(); x++) {
            ic.setStack(x, this.craftMatrix.getInvStack(x));
        }

        final DefaultedList<ItemStack> remainingItems = getRemainingItems(ic, playerIn.world);

        ItemHandlerUtil.copy(ic, this.craftMatrix, false);

        // FIXME FABRIC: net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < remainingItems.size(); ++i) {
            final ItemStack itemstack1 = this.craftMatrix.getInvStack(i);
            final ItemStack itemstack2 = remainingItems.get(i);

            if (!itemstack1.isEmpty()) {
                this.craftMatrix.getSlot(i).extract(1);
            }

            if (!itemstack2.isEmpty()) {
                if (this.craftMatrix.getInvStack(i).isEmpty()) {
                    ItemHandlerUtil.setStackInSlot(this.craftMatrix, i, itemstack2);
                } else if (!this.thePlayer.inventory.insertStack(itemstack2)) {
                    this.thePlayer.dropItem(itemstack2, false);
                }
            }
        }

        return stack;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the
     * second int arg. Returns the new stack.
     */
    @Override
    public ItemStack takeStack(final int par1) {
        if (this.hasStack()) {
            this.amountCrafted += Math.min(par1, this.getStack().getCount());
        }

        return super.takeStack(par1);
    }

    // TODO: This is really hacky and NEEDS to be solved with a full container/gui
    // refactoring.
    protected DefaultedList<ItemStack> getRemainingItems(CraftingInventory ic, World world) {
        return world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, ic, world);
    }
}
