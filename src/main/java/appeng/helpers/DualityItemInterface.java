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

package appeng.helpers;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.helpers.iface.GenericStackInvStorage;
import appeng.me.storage.StorageAdapter;
import appeng.util.IVariantConversion;

public class DualityItemInterface extends DualityInterface<AEItemKey> {

    public static final int NUMBER_OF_SLOTS = 9;

    @Nullable
    private InterfaceInventory localInvHandler;

    /**
     * Used to expose the local storage of this interface to external machines.
     */
    private final GenericStackInvStorage<ItemVariant, AEItemKey> localStorage;

    public DualityItemInterface(IManagedGridNode gridNode, IItemInterfaceHost ih, ItemStack is) {
        super(gridNode, ih, is);
        getConfig().setCapacity(Container.LARGE_MAX_STACK_SIZE);
        getStorage().setCapacity(Container.LARGE_MAX_STACK_SIZE);
        this.localStorage = GenericStackInvStorage.items(getStorage());
    }

    @Override
    public void saveChanges() {
        this.host.saveChanges();
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this.host.getBlockEntity());
    }

    @Override
    protected IStorageChannel<AEItemKey> getChannel() {
        return StorageChannels.items();
    }

    @Override
    protected int getStorageSlots() {
        return NUMBER_OF_SLOTS;
    }

    public GenericStackInvStorage<ItemVariant, AEItemKey> getLocalStorage() {
        return localStorage;
    }

    /**
     * Returns an ME compatible monitor for the interfaces local storage.
     */
    @Override
    protected <T extends AEKey> IMEMonitor<T> getLocalInventory(IStorageChannel<T> channel) {
        if (channel == StorageChannels.items()) {
            if (localInvHandler == null) {
                localInvHandler = new InterfaceInventory();
            }
            return localInvHandler.cast(channel);
        }
        return null;
    }

    /**
     * An adapter that makes the interface's local storage available to an AE-compatible client, such as a storage bus.
     */
    private class InterfaceInventory extends StorageAdapter<ItemVariant, AEItemKey>
            implements IMEMonitor<AEItemKey> {

        InterfaceInventory() {
            super(IVariantConversion.ITEM, localStorage, false);
            this.setActionSource(actionSource);
        }

        @Override
        public long insert(AEItemKey what, long amount, Actionable mode, IActionSource source) {
            // Prevents other interfaces from injecting their items into this interface when they push
            // their local inventory into the network. This prevents items from bouncing back and forth
            // between interfaces.
            if (getRequestInterfacePriority(source).isPresent()) {
                return 0;
            }

            return super.insert(what, amount, mode, source);
        }

        @Override
        public long extract(AEItemKey what, long amount, Actionable mode, IActionSource source) {
            // Prevents interfaces of lower priority fullfilling their item stocking requests from this interface
            // Otherwise we'd see a "ping-pong" effect where two interfaces could start pulling items back and
            // forth of they wanted to stock the same item and happened to have storage buses on them.
            var requestPriority = getRequestInterfacePriority(source);
            if (requestPriority.isPresent() && requestPriority.getAsInt() <= getPriority()) {
                return 0;
            }

            return super.extract(what, amount, mode, source);
        }

        @Override
        protected void onInjectOrExtract() {
            // Rebuild cache immediately
            this.onTick();
        }

        @Override
        public KeyCounter<AEItemKey> getCachedAvailableStacks() {
            return getAvailableStacks();
        }
    }

}
