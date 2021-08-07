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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.blockentities.ISegmentedInventory;
import appeng.api.storage.ITerminalHost;
import appeng.menu.ContainerNull;
import appeng.menu.SlotSemantic;
import appeng.menu.implementations.ContainerTypeBuilder;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.inv.WrapperInvItemHandler;

/**
 * Can only be used with a host that implements {@link ISegmentedInventory} and exposes an inventory named "crafting" to
 * store the crafting grid and output.
 *
 * @see appeng.client.gui.me.items.CraftingTermScreen
 */
public class CraftingTermMenu extends ItemTerminalMenu implements IContainerCraftingPacket {

    public static final MenuType<CraftingTermMenu> TYPE = ContainerTypeBuilder
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

        final IItemHandler craftingGridInv = this.craftingInventoryHost.getInventoryByName("crafting");

        for (int i = 0; i < 9; i++) {
            this.addSlot(this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv, i),
                    SlotSemantic.CRAFTING_GRID);
        }

        this.addSlot(this.outputSlot = new CraftingTermSlot(this.getPlayerInventory().player, this.getActionSource(),
                this.powerSource, host, craftingGridInv, craftingGridInv, this), SlotSemantic.CRAFTING_RESULT);

        this.createPlayerInventorySlots(ip);

        this.slotsChanged(new WrapperInvItemHandler(craftingGridInv));
    }

    /**
     * Callback for when the crafting matrix is changed.
     */

    @Override
    public void slotsChanged(Container inventory) {
        final ContainerNull cn = new ContainerNull();
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
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("player")) {
            return new PlayerInvWrapper(this.getPlayerInventory());
        }
        return this.craftingInventoryHost.getInventoryByName(name);
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
            if (!stackInSlot.isEmpty() && Platform.itemComparisons().isSameItem(itemStack, stackInSlot)) {
                if (itemStack.getCount() >= amount) {
                    return true;
                }
                amount -= itemStack.getCount();
            }

        }

        return super.hasItemType(itemStack, amount);
    }

}
