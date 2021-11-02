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

import com.mojang.authlib.GameProfile;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.IPlayerRegistry;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStackList;
import appeng.blockentity.misc.SecurityStationBlockEntity;
import appeng.core.definitions.AEItems;

public class SecurityStationInventory implements IMEInventoryHandler<IAEItemStack> {

    private final IAEStackList<IAEItemStack> storedItems = StorageChannels.items().createList();
    private final SecurityStationBlockEntity blockEntity;

    public SecurityStationInventory(final SecurityStationBlockEntity ts) {
        this.blockEntity = ts;
    }

    @Override
    public IAEItemStack injectItems(final IAEItemStack input, final Actionable type, final IActionSource src) {
        if (this.hasPermission(src)
                && AEItems.BIOMETRIC_CARD.isSameAs(input.createItemStack())
                && this.canAccept(input)) {
            if (type == Actionable.SIMULATE) {
                return null;
            }

            this.getStoredItems().add(input);
            this.blockEntity.inventoryChanged();
            return null;
        }
        return input;
    }

    private boolean hasPermission(final IActionSource src) {
        if (src.player().isPresent()) {
            var grid = this.blockEntity.getMainNode().getGrid();
            if (grid != null) {
                return grid.getSecurityService().hasPermission(src.player().get(), SecurityPermissions.SECURITY);
            }
        }
        return false;
    }

    @Override
    public IAEItemStack extractItems(final IAEItemStack request, final Actionable mode, final IActionSource src) {
        if (this.hasPermission(src)) {
            final IAEItemStack target = this.getStoredItems().findPrecise(request);
            if (target != null) {
                final IAEItemStack output = target.copy();

                if (mode == Actionable.SIMULATE) {
                    return output;
                }

                target.setStackSize(0);
                this.blockEntity.inventoryChanged();
                return output;
            }
        }
        return null;
    }

    @Override
    public IAEStackList<IAEItemStack> getAvailableStacks(final IAEStackList out) {
        for (final IAEItemStack ais : this.getStoredItems()) {
            out.add(ais);
        }

        return out;
    }

    @Override
    public IStorageChannel getChannel() {
        return StorageChannels.items();
    }

    @Override
    public boolean canAccept(final IAEItemStack input) {
        if (input.getItem() instanceof IBiometricCard tbc) {
            var newUser = tbc.getProfile(input.createItemStack());

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

            for (final IAEItemStack ais : this.getStoredItems()) {
                if (ais.isMeaningful()) {
                    final GameProfile thisUser = tbc.getProfile(ais.createItemStack());
                    if (thisUser == newUser) {
                        return false;
                    }

                    if (thisUser != null && thisUser.equals(newUser)) {
                        return false;
                    }
                }
            }

            return true;
        }
        return false;
    }

    public IAEStackList<IAEItemStack> getStoredItems() {
        return this.storedItems;
    }
}
