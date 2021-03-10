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

package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.CraftAmountContainer;
import appeng.core.AppEng;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class InventoryActionPacket extends BasePacket {

    private final InventoryAction action;
    private final int slot;
    private final long id;
    private final IAEItemStack slotItem;

    public InventoryActionPacket(final PacketBuffer stream) {
        this.action = InventoryAction.values()[stream.readInt()];
        this.slot = stream.readInt();
        this.id = stream.readLong();
        final boolean hasItem = stream.readBoolean();
        if (hasItem) {
            this.slotItem = AEItemStack.fromPacket(stream);
        } else {
            this.slotItem = null;
        }
    }

    // api
    public InventoryActionPacket(final InventoryAction action, final int slot, final IAEItemStack slotItem) {

        if (Platform.isClient()) {
            throw new IllegalStateException("invalid packet, client cannot post inv actions with stacks.");
        }

        this.action = action;
        this.slot = slot;
        this.id = 0;
        this.slotItem = slotItem;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeInt(action.ordinal());
        data.writeInt(slot);
        data.writeLong(this.id);

        if (slotItem == null) {
            data.writeBoolean(false);
        } else {
            data.writeBoolean(true);
            slotItem.writeToPacket(data);
        }

        this.configureWrite(data);
    }

    // api
    public InventoryActionPacket(final InventoryAction action, final int slot, final long id) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.slotItem = null;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeInt(action.ordinal());
        data.writeInt(slot);
        data.writeLong(id);
        data.writeBoolean(false);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        final ServerPlayerEntity sender = (ServerPlayerEntity) player;
        if (sender.containerMenu instanceof AEBaseContainer) {
            final AEBaseContainer baseContainer = (AEBaseContainer) sender.containerMenu;
            if (this.action == InventoryAction.AUTO_CRAFT) {
                final ContainerLocator locator = baseContainer.getLocator();
                if (locator != null) {
                    ContainerOpener.openContainer(CraftAmountContainer.TYPE, player, locator);

                    if (sender.containerMenu instanceof CraftAmountContainer) {
                        final CraftAmountContainer cca = (CraftAmountContainer) sender.containerMenu;

                        if (baseContainer.getTargetStack() != null) {
                            cca.getCraftingItem().set(baseContainer.getTargetStack().asItemStackRepresentation());
                            // This is the *actual* item that matters, not the display item above
                            cca.setItemToCraft(baseContainer.getTargetStack());
                        }

                        cca.broadcastChanges();
                    }
                }
            } else {
                baseContainer.doAction(sender, this.action, this.slot, this.id);
            }
        }
    }

    @Override
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        if (this.action == InventoryAction.UPDATE_HAND) {
            if (this.slotItem == null) {
                AppEng.proxy.getPlayers().get(0).inventory.setCarried(ItemStack.EMPTY);
            } else {
                AppEng.proxy.getPlayers().get(0).inventory.setCarried(this.slotItem.createItemStack());
            }
        }
    }
}
