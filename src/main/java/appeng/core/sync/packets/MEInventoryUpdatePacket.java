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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;
import appeng.core.sync.network.INetworkInfo;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.me.common.MEMonitorableMenu;

public class MEInventoryUpdatePacket extends BasePacket {

    /**
     * Maximum size of a single packet before it will be flushed forcibly.
     */
    private static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 512 * 1024 * 1024;

    /**
     * Initial buffer size for an update packet.
     */
    private static final int INITIAL_BUFFER_CAPACITY = 2 * 1024;

    // input.
    private final StorageList<?> storageList;

    private boolean fullUpdate;

    private int containerId;

    /**
     * @param <T> Type of stack stored in list.
     */
    private record StorageList<T extends AEKey> (
            IStorageChannel<T> storageChannel,
            List<GridInventoryEntry<T>> list) {
        public static StorageList<?> read(FriendlyByteBuf data) {
            var storageChannel = StorageChannels.get(data.readResourceLocation());
            return read(storageChannel, data);
        }

        private static <T extends AEKey> StorageList<T> read(IStorageChannel<T> storageChannel,
                FriendlyByteBuf data) {
            var count = data.readShort();
            var list = new ArrayList<GridInventoryEntry<T>>(count);

            // We need to access the current screen to know which storage channel was used to serialize this data
            for (int i = 0; i < count; i++) {
                list.add(GridInventoryEntry.read(storageChannel, data));
            }

            return new StorageList<>(storageChannel, list);
        }

        @SuppressWarnings("unchecked")
        public void tryApply(MEMonitorableMenu<?> menu, boolean fullUpdate) {
            if (menu.getStorageChannel() != storageChannel) {
                AELog.warn("Ignoring storage update from server because storage channel of opened menu is %s, " +
                        "but update is for %s.", menu.getStorageChannel().getId(), storageChannel.getId());
            } else {
                apply((MEMonitorableMenu<T>) menu, fullUpdate);
            }
        }

        public void apply(MEMonitorableMenu<T> menu, boolean fullUpdate) {
            IClientRepo<T> clientRepo = menu.getClientRepo();
            if (clientRepo == null) {
                AELog.info("Ignoring ME inventory update packet because no client repo is available.");
                return;
            }

            clientRepo.handleUpdate(fullUpdate, list);
        }
    }

    public MEInventoryUpdatePacket(FriendlyByteBuf data) {
        this.containerId = data.readVarInt();
        this.fullUpdate = data.readBoolean();
        this.storageList = StorageList.read(data);
    }

    // api
    private MEInventoryUpdatePacket() {
        this.storageList = null;
    }

    public static class Builder<T extends AEKey> {
        private final List<MEInventoryUpdatePacket> packets = new ArrayList<>();

        private final int containerId;

        private final IStorageChannel<T> storageChannel;

        @Nullable
        private FriendlyByteBuf data;

        // Offset into data where the 16-bit item-count must be written
        // to at the end, before sending the packet
        private int itemCountOffset = -1;

        private int itemCount;

        public Builder(IStorageChannel<T> storageChannel, int containerId, boolean fullUpdate) {
            this.containerId = containerId;
            this.storageChannel = storageChannel;

            // If we are to send a full update, initialize the data buffer to ensure it is sent even if no
            // items are ever added (this indicates clearing the inventory client-side)
            if (fullUpdate) {
                data = createPacketHeader(true);
            } else {
                data = null;
            }
        }

        public void addFull(IncrementalUpdateHelper<T> updateHelper,
                KeyCounter<T> networkStorage,
                Set<T> craftables,
                KeyCounter<T> requestables) {
            var keys = new HashSet<T>();
            keys.addAll(networkStorage.keySet());
            keys.addAll(craftables);
            keys.addAll(requestables.keySet());

            for (var key : keys) {
                long serial = updateHelper.getOrAssignSerial(key);
                add(new GridInventoryEntry<>(
                        serial,
                        key,
                        networkStorage.get(key),
                        requestables.get(key),
                        craftables.contains(key)));
            }
        }

        public void addChanges(IncrementalUpdateHelper<T> updateHelper,
                KeyCounter<T> networkStorage,
                Set<T> craftables,
                KeyCounter<T> requestables) {
            for (T key : updateHelper) {
                T sendKey;
                Long serial = updateHelper.getSerial(key);

                // Try to serialize the item into the buffer
                if (serial == null) {
                    // This is a new key, not sent to the client
                    sendKey = key;
                    serial = updateHelper.getOrAssignSerial(key);
                } else {
                    // This is an incremental update referring back to the serial
                    sendKey = null;
                }

                // The queued changes are actual differences, but we need to send the real stored properties
                // to the client.
                var storedAmount = networkStorage.get(key);
                var craftable = craftables.contains(key);
                var requestable = requestables.get(key);
                if (storedAmount <= 0 && requestable <= 0 && !craftable) {
                    // This happens when an update is queued but the item is no longer stored
                    add(new GridInventoryEntry<>(serial, sendKey, 0, 0, false));
                    updateHelper.removeSerial(key);
                } else {
                    add(new GridInventoryEntry<>(serial, sendKey, storedAmount, requestable, craftable));
                }
            }

            updateHelper.commitChanges();
        }

        public void add(GridInventoryEntry<T> entry) {
            FriendlyByteBuf data = ensureData();

            // This should only error out if the entire packet exceeds about 2 megabytes of memory,
            // if any item writes that much junk to a share tag, it's acceptable to crash.
            // We'll normaly flush much much earlier (32k)
            entry.write(data);

            ++itemCount;

            if (data.writerIndex() >= UNCOMPRESSED_PACKET_BYTE_LIMIT || itemCount >= Short.MAX_VALUE) {
                flushData();
            }
        }

        private void flushData() {
            if (data != null) {
                // Jump back and fill in the number of items contained in the packet
                data.markWriterIndex();
                data.writerIndex(itemCountOffset);
                data.writeShort(itemCount);
                data.resetWriterIndex();

                // Build a packet and queue it
                var packet = new MEInventoryUpdatePacket();
                packet.configureWrite(data);
                packets.add(packet);

                // Reset
                data = null;
                itemCountOffset = -1;
                itemCount = 0;
            }
        }

        private FriendlyByteBuf ensureData() {
            if (data == null) {
                data = createPacketHeader(false);
            }
            return data;
        }

        private FriendlyByteBuf createPacketHeader(boolean fullUpdate) {
            final FriendlyByteBuf data;
            data = new FriendlyByteBuf(Unpooled.buffer(INITIAL_BUFFER_CAPACITY));
            // Since we don't have an instance of a packet we can't get the packet id the normal way now
            data.writeInt(BasePacketHandler.PacketTypes.ME_INVENTORY_UPDATE.getPacketId());
            data.writeVarInt(containerId);
            data.writeBoolean(fullUpdate);
            data.writeResourceLocation(storageChannel.getId());
            // This is a placeholder for the item count and will be added at the end,
            // so we need to remember where in the stream we have written it
            itemCountOffset = data.writerIndex();
            data.writeShort(0);
            return data;
        }

        public List<MEInventoryUpdatePacket> build() {
            flushData();
            return packets;
        }

        public void buildAndSend(Consumer<MEInventoryUpdatePacket> sender) {
            for (var packet : build()) {
                sender.accept(packet);
            }
        }
    }

    public static <T extends AEKey> Builder<T> builder(IStorageChannel<T> storageChannel,
            int containerId,
            boolean fullUpdate) {
        return new Builder<>(storageChannel, containerId, fullUpdate);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(INetworkInfo network, Player player) {
        if (player.containerMenu.containerId == containerId
                && player.containerMenu instanceof MEMonitorableMenu<?>meMenu) {
            storageList.tryApply(meMenu, fullUpdate);
        }
    }

}
