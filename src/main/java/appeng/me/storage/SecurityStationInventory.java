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

package appeng.me.storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.blockentity.misc.SecurityStationBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.core.localization.GuiText;
import appeng.items.tools.BiometricCardItem;

/**
 * Inventory for biometric cards. It does not allow duplicates/stacking, as such it simply stores the item keys for the
 * cards in a set.
 */
public class SecurityStationInventory implements MEStorage {

    private final Set<AEItemKey> storedItems = new HashSet<>();
    private final SecurityStationBlockEntity blockEntity;

    public SecurityStationInventory(SecurityStationBlockEntity ts) {
        this.blockEntity = ts;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (this.hasPermission(source)
                && what instanceof AEItemKey itemKey
                && AEItems.BIOMETRIC_CARD.isSameAs(itemKey)
                && canAccept(itemKey)
                && amount > 0) {
            if (mode == Actionable.MODULATE) {
                storedItems.add(itemKey);
                blockEntity.inventoryChanged();
            }
            return 1;
        }
        return 0;
    }

    private boolean hasPermission(IActionSource src) {
        if (src.player().isPresent()) {
            var grid = this.blockEntity.getMainNode().getGrid();
            if (grid != null) {
                return grid.getSecurityService().hasPermission(src.player().get(), SecurityPermissions.SECURITY);
            }
        }
        return false;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (this.hasPermission(source) && amount > 0 && what instanceof AEItemKey itemKey) {
            if (mode == Actionable.SIMULATE && storedItems.contains(itemKey)) {
                return 1;
            } else if (storedItems.remove(itemKey)) {
                this.blockEntity.inventoryChanged();
                return 1;
            }
        }
        return 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var storedItem : storedItems) {
            out.add(storedItem, 1);
        }
    }

    public boolean canAccept(AEItemKey what) {
        // This is a very simple check to prevent the same stack from being
        // entered twice (and being voided in the process)
        if (storedItems.contains(what)) {
            return false;
        }

        if (what.getItem() instanceof BiometricCardItem biometricCard) {
            var newUser = biometricCard.getProfile(what);

            var pr = IPlayerRegistry.getMapping(blockEntity.getLevel());
            if (pr == null) {
                // Don't do further checks on the client-side, which doesn't have access to the player registry
                return true;
            }

            // The profile might be null in case the card is unbound, otherwise don't allow adding a card
            // for the owner, since they always are fully authorized
            if (newUser != null) {
                int playerId = pr.getPlayerId(newUser);
                if (this.blockEntity.getOwner() == playerId) {
                    return false;
                }
            }

            // This in-depth check is needed because in theory the biometric card can have additional NBT
            // preventing the simple storedItems.contains check to fail.
            for (var entry : storedItems) {
                var existingUser = biometricCard.getProfile(entry);
                if (existingUser == newUser) {
                    // This also catches both being null (for the fallback card)
                    return false;
                }

                if (existingUser != null && existingUser.equals(newUser)) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    public Collection<AEItemKey> getStoredItems() {
        return this.storedItems;
    }

    @Override
    public Component getDescription() {
        return GuiText.Security.text();
    }
}
