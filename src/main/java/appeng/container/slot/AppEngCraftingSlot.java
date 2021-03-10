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
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.items.IItemHandler;

import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperInvItemHandler;

public class AppEngCraftingSlot extends AppEngSlot {

    /**
     * The craft matrix inventory linked to this result slot.
     */
    private final IItemHandler craftMatrix;

    /**
     * The player that is using the GUI where this slot resides.
     */
    private final PlayerEntity thePlayer;

    /**
     * The number of items that have been crafted so far. Gets passed to ItemStack.onCrafting before being reset.
     */
    private int amountCrafted;

    public AppEngCraftingSlot(final PlayerEntity par1PlayerEntity, final IItemHandler par2IInventory,
            final IItemHandler inv, final int invSlot, final int x, final int y) {
        super(inv, invSlot, x, y);
        this.thePlayer = par1PlayerEntity;
        this.craftMatrix = par2IInventory;
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    @Override
    public boolean mayPlace(final ItemStack stack) {
        return false;
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    @Override
    protected void onQuickCraft(final ItemStack par1ItemStack, final int par2) {
        this.amountCrafted += par2;
        this.checkTakeAchievements(par1ItemStack);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    @Override
    protected void checkTakeAchievements(final ItemStack par1ItemStack) {
        par1ItemStack.onCraftedBy(this.thePlayer.level, this.thePlayer, this.amountCrafted);
        this.amountCrafted = 0;
    }

    @Override
    public ItemStack onTake(final PlayerEntity playerIn, final ItemStack stack) {
        BasicEventHooks.firePlayerCraftingEvent(playerIn, stack, new WrapperInvItemHandler(this.craftMatrix));
        this.checkTakeAchievements(stack);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(playerIn);
        final CraftingInventory ic = new CraftingInventory(this.getContainer(), 3, 3);

        for (int x = 0; x < this.craftMatrix.getSlots(); x++) {
            ic.setItem(x, this.craftMatrix.getStackInSlot(x));
        }

        final NonNullList<ItemStack> aitemstack = this.getRemainingItems(ic, playerIn.level);

        ItemHandlerUtil.copy(ic, this.craftMatrix, false);

        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < aitemstack.size(); ++i) {
            final ItemStack itemstack1 = this.craftMatrix.getStackInSlot(i);
            final ItemStack itemstack2 = aitemstack.get(i);

            if (!itemstack1.isEmpty()) {
                this.craftMatrix.extractItem(i, 1, false);
            }

            if (!itemstack2.isEmpty()) {
                if (this.craftMatrix.getStackInSlot(i).isEmpty()) {
                    ItemHandlerUtil.setStackInSlot(this.craftMatrix, i, itemstack2);
                } else if (!this.thePlayer.inventory.add(itemstack2)) {
                    this.thePlayer.drop(itemstack2, false);
                }
            }
        }

        return stack;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public ItemStack remove(final int par1) {
        if (this.hasItem()) {
            this.amountCrafted += Math.min(par1, this.getItem().getCount());
        }

        return super.remove(par1);
    }

    // TODO: This is really hacky and NEEDS to be solved with a full container/gui
    // refactoring.
    protected NonNullList<ItemStack> getRemainingItems(CraftingInventory ic, World world) {
        return world.getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, ic, world)
                .map(iCraftingRecipe -> iCraftingRecipe.getRemainingItems(ic))
                .orElse(NonNullList.withSize(9, ItemStack.EMPTY));
    }
}
