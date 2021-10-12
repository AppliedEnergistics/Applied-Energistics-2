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

package appeng.menu.me.items;

import com.google.common.base.Preconditions;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.storage.ITerminalHost;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.NullMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.parts.reporting.CraftingTerminalPart;

/**
 * Can only be used with a host that implements {@link ISegmentedInventory} and exposes an inventory named "crafting" to
 * store the crafting grid and output.
 *
 * @see appeng.client.gui.me.items.CraftingTermScreen
 */
public class CraftingTermMenu extends ItemTerminalMenu implements IMenuCraftingPacket {

    public static final MenuType<CraftingTermMenu> TYPE = MenuTypeBuilder
            .create(CraftingTermMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("craftingterm");

    private final ISegmentedInventory craftingInventoryHost;
    private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[9];
    private final CraftingTermSlot outputSlot;
    private Recipe<CraftingContainer> currentRecipe;

    public CraftingTermMenu(int id, final Inventory ip, final ITerminalHost host) {
        super(TYPE, id, ip, host, false);
        this.craftingInventoryHost = (ISegmentedInventory) host;

        var craftingGridInv = this.craftingInventoryHost
                .getSubInventory(CraftingTerminalPart.INV_CRAFTING);

        for (int i = 0; i < 9; i++) {
            this.addSlot(this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv, i),
                    SlotSemantic.CRAFTING_GRID);
        }

        this.addSlot(this.outputSlot = new CraftingTermSlot(this.getPlayerInventory().player, this.getActionSource(),
                this.powerSource, host, craftingGridInv, craftingGridInv, this), SlotSemantic.CRAFTING_RESULT);

        this.createPlayerInventorySlots(ip);

        this.slotsChanged(craftingGridInv.toContainer());
    }

    /**
     * Callback for when the crafting matrix is changed.
     */

    @Override
    public void slotsChanged(Container inventory) {
        final NullMenu cn = new NullMenu();
        final CraftingContainer ic = new CraftingContainer(cn, 3, 3);

        for (int x = 0; x < 9; x++) {
            ic.setItem(x, this.craftingSlots[x].getItem());
        }

        Level level = this.getPlayerInventory().player.level;
        if (this.currentRecipe == null || !this.currentRecipe.matches(ic, level)) {
            this.currentRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, ic, level).orElse(null);
        }

        if (this.currentRecipe == null) {
            this.outputSlot.set(ItemStack.EMPTY);
        } else {
            this.outputSlot.set(this.currentRecipe.assemble(ic));
        }
    }

    @Override
    public InternalInventory getCraftingMatrix() {
        return this.craftingInventoryHost.getSubInventory(CraftingTerminalPart.INV_CRAFTING);
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    public Recipe<CraftingContainer> getCurrentRecipe() {
        return this.currentRecipe;
    }

    /**
     * Clears the crafting grid and moves everything back into the network inventory.
     */
    public void clearCraftingGrid() {
        Preconditions.checkState(isClient());
        CraftingMatrixSlot slot = craftingSlots[0];
        final InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slot.index, 0);
        NetworkHandler.instance().sendToServer(p);
    }

    @Override
    public boolean hasItemType(ItemStack itemStack, int amount) {
        // In addition to the base item repo, also check the crafting grid if it
        // already contains some of the needed items
        for (Slot slot : getSlots(SlotSemantic.CRAFTING_GRID)) {
            ItemStack stackInSlot = slot.getItem();
            if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameTags(itemStack, stackInSlot)) {
                if (itemStack.getCount() >= amount) {
                    return true;
                }
                amount -= itemStack.getCount();
            }

        }

        return super.hasItemType(itemStack, amount);
    }

}
