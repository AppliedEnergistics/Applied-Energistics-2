/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.api.implementations.menuobjects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;

/**
 * Base interface for an adapter that connects an item stack in a player inventory with a menu that is opened by it.
 */
public class ItemMenuHost {

    private final Player player;
    @Nullable
    private final Integer slot;
    private final ItemStack itemStack;
    private int powerTicks = 0;
    private double powerDrainPerTick = 0.5;

    public ItemMenuHost(Player player, @Nullable Integer slot, ItemStack itemStack) {
        this.player = player;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    /**
     * @return The player holding the item.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The index of the item hosting the menu in the {@link #getPlayer() players} inventory. Null if the item is
     *         not directly accessible via the inventory.
     */
    @Nullable
    public Integer getSlot() {
        return slot;
    }

    /**
     * @return The item stack hosting the menu.
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * @return True if this host is on the client-side.
     */
    public boolean isClientSide() {
        return player.level.isClientSide;
    }

    /**
     * Gives the item hosting the GUI a chance to do periodic actions when the menu is being ticked.
     *
     * @return False to close the menu.
     */
    public boolean onBroadcastChanges(AbstractContainerMenu menu) {
        return true;
    }

    /**
     * Ensures that the item stack hosting the menu is still in the expected player inventory slot. If necessary,
     * referential equality is restored by overwriting the item in the player inventory if it is equal to the expected
     * item.
     *
     * @return True if {@link #getItemStack()} is still in the expected slot.
     */
    protected boolean ensureItemStillInSlot() {
        if (slot == null) {
            return true;
        }

        ItemStack expectedItem = getItemStack();

        Inventory inventory = getPlayer().getInventory();
        ItemStack currentItem = inventory.getItem(slot);
        if (!currentItem.isEmpty() && !expectedItem.isEmpty()) {
            if (currentItem == expectedItem) {
                return true;
            } else if (ItemStack.isSame(expectedItem, currentItem)) {
                // If the items are still equivalent, we just restore referential equality so that modifications
                // to the GUI item are reflected in the slot
                inventory.setItem(slot, expectedItem);
                return true;
            }
        }
        return false;
    }

    /**
     * Can only be used with a host that implements {@link IEnergySource} only call once per broadcastChanges()
     */
    public boolean drainPower() {
        if (this instanceof IEnergySource energySource) {
            this.powerTicks++;
            if (this.powerTicks > 10) {
                var amt = this.powerTicks * this.powerDrainPerTick;
                this.powerTicks = 0;
                return energySource.extractAEPower(amt, Actionable.MODULATE, PowerMultiplier.CONFIG) > 0;
            }
        }
        return true;
    }

    /**
     * Sets how much AE is drained per tick.
     */
    protected void setPowerDrainPerTick(double powerDrainPerTick) {
        this.powerDrainPerTick = powerDrainPerTick;
    }
}
