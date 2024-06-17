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

package appeng.menu.slot;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;

public class AppEngCraftingSlot extends AppEngSlot implements RecipeCraftingHolder {

    /**
     * The craft matrix inventory linked to this result slot.
     */
    private final InternalInventory craftingGrid;

    /**
     * The player that is using the GUI where this slot resides.
     */
    private final Player player;

    /**
     * The number of items that have been crafted so far. Gets passed to ItemStack.onCrafting before being reset.
     */
    private int amountCrafted;

    @Nullable
    private RecipeHolder<?> recipeUsed;

    public AppEngCraftingSlot(Player player, InternalInventory craftingGrid) {
        super(new AppEngInternalInventory(1), 0);
        this.player = player;
        this.craftingGrid = craftingGrid;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        this.amountCrafted += amount;
        this.checkTakeAchievements(stack);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. This replicates Vanilla
     * {@link net.minecraft.world.inventory.ResultSlot}.
     */
    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        var craftContainer = craftingGrid.toContainer();

        if (amountCrafted > 0) {
            stack.onCraftedBy(this.player.level(), this.player, this.amountCrafted);
            net.neoforged.neoforge.event.EventHooks.firePlayerCraftingEvent(this.player, stack, craftContainer);
        }

        var ingredients = Lists.newArrayList(craftingGrid);
        awardUsedRecipes(this.player, ingredients);

        this.amountCrafted = 0;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        this.amountCrafted += stack.getCount();
        this.checkTakeAchievements(stack);

        var items = NonNullList.withSize(this.craftingGrid.size(), ItemStack.EMPTY);
        for (int i = 0; i < this.craftingGrid.size(); i++) {
            items.set(i, this.craftingGrid.getStackInSlot(i));
        }
        var positioned = CraftingInput.ofPositioned(3, 3, items);

        CommonHooks.setCraftingPlayer(player);
        var remainingItems = this.getRemainingItems(positioned.input(), player.level());
        CommonHooks.setCraftingPlayer(null);

        for (var y = 0; y < 3; y++) {
            for (var x = 0; x < 3; x++) {
                var slotIdx = y * 3 + x;
                var remainderIdx = (y - positioned.top()) * 3 + (x - positioned.left());

                // Consumes the item from the grid
                this.craftingGrid.extractItem(slotIdx, 1, false);

                if (remainderIdx >= 0 && remainderIdx < remainingItems.size()) {
                    var remainingInSlot = remainingItems.get(remainderIdx);
                    if (!remainingInSlot.isEmpty()) {
                        if (this.craftingGrid.getStackInSlot(slotIdx).isEmpty()) {
                            this.craftingGrid.setItemDirect(slotIdx, remainingInSlot);
                        } else if (!this.player.getInventory().add(remainingInSlot)) {
                            this.player.drop(remainingInSlot, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Overrides what is being shown as the crafting output, but doesn't notify parent menu.
     */
    public void setDisplayedCraftingOutput(ItemStack stack) {
        getInventory().setItemDirect(0, stack);
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public ItemStack remove(int par1) {
        if (this.hasItem()) {
            this.amountCrafted += Math.min(par1, this.getItem().getCount());
        }

        return super.remove(par1);
    }

    // TODO: This is really hacky and NEEDS to be solved with a full menu/gui
    // refactoring.
    protected NonNullList<ItemStack> getRemainingItems(CraftingInput ic, Level level) {
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ic, level)
                .map(recipe -> recipe.value().getRemainingItems(ic))
                .orElse(NonNullList.withSize(9, ItemStack.EMPTY));
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipe) {
        this.recipeUsed = recipe;
    }

    @Nullable
    @Override
    public RecipeHolder<?> getRecipeUsed() {
        return recipeUsed;
    }
}
