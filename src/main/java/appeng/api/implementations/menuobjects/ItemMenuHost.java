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

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEKey;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.locator.ItemMenuHostLocator;

/**
 * Base interface for an adapter that connects an item stack in a player inventory with a menu that is opened by it.
 */
public class ItemMenuHost<T extends Item> implements IUpgradeableObject {

    /**
     * To avoid changing the item stack once every tick, we consume idle power for more than just one tick at a time.
     * The default is to consume power twice per second.
     */
    private static final int BUFFER_ENERGY_TICKS = 10;

    private final T item;
    private final Player player;
    private final ItemMenuHostLocator locator;
    private final IUpgradeInventory upgrades;
    private int remainingEnergyTicks = 0;

    public ItemMenuHost(T item, Player player, ItemMenuHostLocator locator) {
        this.player = player;
        this.locator = locator;
        this.item = item;
        var currentStack = getItemStack();
        if (!currentStack.is(item)) {
            throw new IllegalArgumentException("The current item in-slot is " + currentStack.getItem() + " but " +
                    "this menu requires " + item);
        }
        this.upgrades = new DelegateItemUpgradeInventory(this::getItemStack);
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
    public Integer getPlayerInventorySlot() {
        return locator.getPlayerInventorySlot();
    }

    @Nullable
    public ItemMenuHostLocator getLocator() {
        return locator;
    }

    public T getItem() {
        return item;
    }

    /**
     * @return The item stack hosting the menu. This can change.
     */
    public ItemStack getItemStack() {
        return locator.locateItem(player);
    }

    /**
     * @return True if this host is on the client-side.
     */
    public boolean isClientSide() {
        return player.level().isClientSide;
    }

    /**
     * Gives the item hosting the GUI a chance to do periodic actions when the menu is being ticked.
     */
    public void tick() {
    }

    /**
     * Checks if the item underlying this host is still in place.
     */
    public boolean isValid() {
        var currentItem = getItemStack();
        return !currentItem.isEmpty() && currentItem.is(item);
    }

    /**
     * Can only be used with a host that implements {@link IEnergySource}.
     */
    public boolean consumeIdlePower(Actionable action) {
        // Do not drain power for creative players
        if (player.isCreative()) {
            return true;
        }

        // Remaining charge
        if (remainingEnergyTicks > 0) {
            if (action == Actionable.MODULATE) {
                remainingEnergyTicks--;
            }
            return true;
        }

        var powerDrainPerTick = getPowerDrainPerTick();
        if (powerDrainPerTick > 0 && this instanceof IEnergySource energySource) {
            var amt = BUFFER_ENERGY_TICKS * powerDrainPerTick;
            var actualExtracted = energySource.extractAEPower(amt, action, PowerMultiplier.CONFIG);
            var remainingEnergyTicks = (int) Math.ceil(actualExtracted / powerDrainPerTick);
            if (action == Actionable.MODULATE) {
                this.remainingEnergyTicks = remainingEnergyTicks;
            }

            // Return true if we drained enough energy to last one tick
            return remainingEnergyTicks > 0;
        }

        // If no power is being drained, we're never out of power
        return true;
    }

    /**
     * Get power drain per tick.
     */
    protected double getPowerDrainPerTick() {
        return 0.5;
    }

    @Override
    public final IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    /**
     * Insert something into the host of this menu (i.e. by dropping onto the hosting item in the player inventory or by
     * similar mechanisms).
     *
     * @return The amount that was inserted.
     */
    public long insert(Player player, AEKey what, long amount, Actionable mode) {
        return 0;
    }
}
