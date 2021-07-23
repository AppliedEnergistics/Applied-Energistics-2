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

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.core.definitions.AEItems;
import appeng.tile.misc.SecurityStationTileEntity;

public class SecurityStationInventory implements IMEInventoryHandler<IAEItemStack> {

    private final IItemList<IAEItemStack> storedItems = Api.instance().storage()
            .getStorageChannel(IItemStorageChannel.class).createList();
    private final SecurityStationTileEntity securityTile;

    public SecurityStationInventory(final SecurityStationTileEntity ts) {
        this.securityTile = ts;
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
            this.securityTile.inventoryChanged();
            return null;
        }
        return input;
    }

    private boolean hasPermission(final IActionSource src) {
        if (src.player().isPresent()) {
            var grid = this.securityTile.getMainNode().getGrid();
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
                this.securityTile.inventoryChanged();
                return output;
            }
        }
        return null;
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(final IItemList out) {
        for (final IAEItemStack ais : this.getStoredItems()) {
            out.add(ais);
        }

        return out;
    }

    @Override
    public IStorageChannel getChannel() {
        return Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(final IAEItemStack input) {
        return false;
    }

    @Override
    public boolean canAccept(final IAEItemStack input) {
        if (input.getItem() instanceof IBiometricCard) {
            final IBiometricCard tbc = (IBiometricCard) input.getItem();
            final GameProfile newUser = tbc.getProfile(input.createItemStack());

            final int PlayerID = Api.instance().registries().players().getID(newUser);
            if (this.securityTile.getOwner() == PlayerID) {
                return false;
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

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(final int i) {
        return true;
    }

    public IItemList<IAEItemStack> getStoredItems() {
        return this.storedItems;
    }
}
