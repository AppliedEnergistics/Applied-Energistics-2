/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.helpers.ICraftingGridMenu;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.Platform;
import appeng.util.inv.CarriedItemInventory;
import appeng.util.inv.PlayerInternalInventory;
import appeng.util.prioritylist.IPartitionList;

/**
 * This is the crafting result slot of the crafting terminal, which also handles performing the actual crafting when a
 * player clicks it.
 */
public class CraftingTermSlot extends AppEngCraftingSlot {

    private final InternalInventory craftInv;
    private final InternalInventory pattern;

    private final IActionSource mySrc;
    private final IEnergySource energySrc;
    private final MEStorage storage;
    private final ICraftingGridMenu menu;

    public CraftingTermSlot(Player player, IActionSource mySrc, IEnergySource energySrc,
            MEStorage storage, InternalInventory cMatrix, InternalInventory secondMatrix,
            ICraftingGridMenu ccp) {
        super(player, cMatrix);
        this.energySrc = energySrc;
        this.storage = storage;
        this.mySrc = mySrc;
        this.pattern = cMatrix;
        this.craftInv = secondMatrix;
        this.menu = ccp;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack is) {
    }

    public void doClick(InventoryAction action, Player who) {
        if (this.getItem().isEmpty()) {
            return;
        }
        if (isRemote()) {
            return;
        }

        final var howManyPerCraft = this.getItem().getCount();

        int maxTimesToCraft;
        InternalInventory target;
        if (action == InventoryAction.CRAFT_SHIFT || action == InventoryAction.CRAFT_ALL) // craft into player
                                                                                          // inventory...
        {
            target = new PlayerInternalInventory(who.getInventory());
            if (action == InventoryAction.CRAFT_SHIFT) {
                maxTimesToCraft = (int) Math
                        .floor((double) this.getItem().getMaxStackSize() / (double) howManyPerCraft);
            } else {
                maxTimesToCraft = (int) Math.floor((double) this.getItem().getMaxStackSize() / (double) howManyPerCraft
                        * Inventory.INVENTORY_SIZE);
            }
        } else if (action == InventoryAction.CRAFT_STACK) // craft into hand, full stack
        {
            target = new CarriedItemInventory(getMenu());
            maxTimesToCraft = (int) Math.floor((double) this.getItem().getMaxStackSize() / (double) howManyPerCraft);
        } else
        // pick up what was crafted...
        {
            // This is a shortcut to ensure that for mods that create recipes with result counts larger than
            // the max stack size, it remains possible to pick up those items at least _once_.
            if (getMenu().getCarried().isEmpty()) {
                getMenu().setCarried(craftItem(who, storage, storage.getAvailableStacks()));
                return;
            }

            target = new CarriedItemInventory(getMenu());
            maxTimesToCraft = 1;
        }

        // Since we may be crafting multiple times, we have to ensure that we keep crafting the same item.
        // This may not be the case if not all crafting grid slots have the same number of items in them,
        // and some ingredients run-out after a few crafts.
        var itemAtStart = this.getItem().copy();
        if (itemAtStart.isEmpty()) {
            return;
        }

        for (var x = 0; x < maxTimesToCraft; x++) {
            // Stop if the recipe output has changed (i.e. due to fully consumed input slots)
            if (!ItemStack.isSameItemSameComponents(itemAtStart, getItem())) {
                return;
            }

            // Stop if the target inventory is full
            if (!target.simulateAdd(itemAtStart).isEmpty()) {
                return;
            }

            var all = storage.getAvailableStacks();
            var extra = target.addItems(craftItem(who, storage, all));

            // If we couldn't actually add what we crafted, we drop it and stop
            if (!extra.isEmpty()) {
                Platform.spawnDrops(who.level(), who.blockPosition(), List.of(extra));
                return;
            }
        }
    }

    // TODO: This is really hacky and NEEDS to be solved with a full menu/gui
    // refactoring.
    protected RecipeHolder<CraftingRecipe> findRecipe(CraftingInput ic, Level level) {
        if (this.menu instanceof CraftingTermMenu terminalMenu) {
            var recipe = terminalMenu.getCurrentRecipe();

            if (recipe != null && recipe.value().matches(ic, level)) {
                return terminalMenu.getCurrentRecipe();
            }
        }

        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ic, level).orElse(null);
    }

    // TODO: This is really hacky and NEEDS to be solved with a full menu/gui
    // refactoring.
    @Override
    protected NonNullList<ItemStack> getRemainingItems(CraftingInput ic, Level level) {
        if (this.menu instanceof CraftingTermMenu terminalMenu) {
            var recipe = terminalMenu.getCurrentRecipe();

            if (recipe != null && recipe.value().matches(ic, level)) {
                return terminalMenu.getCurrentRecipe().value().getRemainingItems(ic);
            }
        }

        return super.getRemainingItems(ic, level);
    }

    private ItemStack craftItem(Player p, MEStorage inv, KeyCounter all) {
        // update crafting matrix...
        var is = this.getItem().copy();
        if (is.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Make sure the item in the slot is still the same item as before
        final var set = new ItemStack[this.getPattern().size()];
        // Safeguard for empty slots in the inventory for now
        Arrays.fill(set, ItemStack.EMPTY);

        // add one of each item to the items on the board...
        var level = p.level();
        if (!level.isClientSide()) {
            final var ic = NonNullList.withSize(9, ItemStack.EMPTY);
            for (var x = 0; x < 9; x++) {
                ic.set(x, this.getPattern().getStackInSlot(x));
            }
            var recipeInput = CraftingInput.of(3, 3, ic);

            final var r = this.findRecipe(recipeInput, level);
            setRecipeUsed(r);

            if (r == null) {
                return ItemStack.EMPTY;
            }

            is = r.value().assemble(recipeInput, level.registryAccess());

            if (inv != null) {
                var filter = ViewCellItem.createItemFilter(this.menu.getViewCells());
                for (var x = 0; x < this.getPattern().size(); x++) {
                    if (!this.getPattern().getStackInSlot(x).isEmpty()) {
                        set[x] = extractItemsByRecipe(this.energySrc, this.mySrc, inv, level, r.value(), is,
                                recipeInput.width(), recipeInput.height(),
                                ic,
                                this.getPattern().getStackInSlot(x), x, all,
                                filter);
                        ic.set(x, set[x]);
                    }
                }
            }
        }

        if (this.preCraft(p, inv, set, is)) {
            this.makeItem(p, is);
            this.postCraft(p, inv, set, is);
        }

        p.containerMenu.slotsChanged(this.craftInv.toContainer());

        return is;
    }

    private static ItemStack extractItemsByRecipe(IEnergySource energySrc,
            IActionSource mySrc,
            MEStorage src,
            Level level,
            Recipe<CraftingInput> r,
            ItemStack output,
            int gridWidth, int gridHeight,
            List<ItemStack> craftingItems,
            ItemStack providedTemplate,
            int slot,
            KeyCounter items,
            IPartitionList filter) {

        if (energySrc.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.9) {
            if (providedTemplate == null) {
                return ItemStack.EMPTY;
            }

            var ae_req = AEItemKey.of(providedTemplate);

            if (filter == null || filter.isListed(ae_req)) {
                var extracted = src.extract(ae_req, 1, Actionable.MODULATE, mySrc);
                if (extracted > 0) {
                    energySrc.extractAEPower(1, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    return ae_req.toStack();
                }
            }

            var checkFuzzy = !providedTemplate.getComponents().isEmpty() || providedTemplate.isDamageableItem();

            if (items != null && checkFuzzy) {
                var craftingInputItems = new ArrayList<>(craftingItems);

                for (var x : items) {
                    if (x.getKey() instanceof AEItemKey itemKey) {
                        if (providedTemplate.getItem() == itemKey.getItem() && !itemKey.matches(output)) {

                            craftingInputItems.set(slot, itemKey.toStack());
                            var adjustedCraftingInput = CraftingInput.of(gridWidth, gridHeight, craftingInputItems);
                            if (r.matches(adjustedCraftingInput, level)
                                    && ItemStack.matches(r.assemble(adjustedCraftingInput, level.registryAccess()),
                                            output)) {
                                if (filter == null || filter.isListed(itemKey)) {
                                    var ex = src.extract(itemKey, 1, Actionable.MODULATE, mySrc);
                                    if (ex > 0) {
                                        energySrc.extractAEPower(1, Actionable.MODULATE, PowerMultiplier.CONFIG);
                                        return itemKey.toStack();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean preCraft(Player p, MEStorage inv, ItemStack[] set,
            ItemStack result) {

        return true;
    }

    private void makeItem(Player p, ItemStack is) {
        super.onTake(p, is);
    }

    private void postCraft(Player p, MEStorage inv, ItemStack[] set,
            ItemStack result) {
        final List<ItemStack> drops = new ArrayList<>();

        // add one of each item to the items on the board...
        if (!p.getCommandSenderWorld().isClientSide()) {
            // set new items onto the crafting table...
            for (var x = 0; x < this.craftInv.size(); x++) {
                if (this.craftInv.getStackInSlot(x).isEmpty()) {
                    this.craftInv.setItemDirect(x, set[x]);
                } else if (!set[x].isEmpty()) {
                    var what = AEItemKey.of(set[x]);
                    var amount = set[x].getCount();
                    var inserted = inv.insert(what, amount, Actionable.MODULATE,
                            this.mySrc);
                    // eek! put it back!
                    if (inserted < amount) {
                        drops.add(what.toStack((int) (amount - inserted)));
                    }
                }
            }
        }

        if (drops.size() > 0) {
            Platform.spawnDrops(p.level(), new BlockPos((int) p.getX(), (int) p.getY(), (int) p.getZ()), drops);
        }
    }

    InternalInventory getPattern() {
        return this.pattern;
    }
}
