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

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.me.items.MEMonitorableScreen;
import appeng.container.me.common.IncrementalUpdateHelper;
import appeng.container.me.items.GridInventoryEntry;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;
import appeng.core.sync.network.INetworkInfo;
import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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
    private final List<GridInventoryEntry> list;

    private boolean fullUpdate;

    public MEInventoryUpdatePacket(final PacketBuffer data) {
        this.fullUpdate = data.readBoolean();
        int itemCount = data.readShort();
        this.list = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            this.list.add(GridInventoryEntry.read(data));
        }
    }

    // api
    private MEInventoryUpdatePacket() {
        this.list = Collections.emptyList();
    }

    // Byte offset to the field in the packet that contains the item count
    private static final int ITEM_COUNT_FIELD_OFFSET = 5;

    public static class Builder {
        private final List<MEInventoryUpdatePacket> packets = new ArrayList<>();

        @Nullable
        private PacketBuffer data;

        private int itemCount;

        public Builder(boolean fullUpdate) {
            // If we are to send a full update, initialize the data buffer to ensure it is sent even if no
            // items are ever added (this indicates clearing the inventory client-side)
            if (fullUpdate) {
                data = createPacketHeader(fullUpdate);
            } else {
                data = null;
            }
        }

        public void addFull(IncrementalUpdateHelper<IAEItemStack> updateHelper,
                            IItemList<IAEItemStack> stacks) {
            for (IAEItemStack item : stacks) {
                long serial = updateHelper.getOrAssignSerial(item);
                add(new GridInventoryEntry(serial, item.getDefinition(), item.getStackSize(), item.getCountRequestable(),
                        item.isCraftable()));
            }
        }

        public void addChanges(IncrementalUpdateHelper<IAEItemStack> updateHelper, IItemList<IAEItemStack> stacks) {
            for (IAEItemStack key : updateHelper) {
                ItemStack displayItem;
                Long serial = updateHelper.getSerial(key);

                // Try to serialize the item into the buffer
                if (serial == null) {
                    // This is a new item
                    displayItem = key.getDefinition();
                    serial = updateHelper.getOrAssignSerial(key);
                } else {
                    // This is an incremental update refering back to the serial
                    displayItem = ItemStack.EMPTY;
                }

                // The queued changes are actual differences, but we need to send the real stored properties
                // to the client.
                IAEItemStack stored = stacks.findPrecise(key);
                if (stored == null || !stored.isMeaningful()) {
                    // This happens when an update is queued but the item is no longer stored
                    add(new GridInventoryEntry(serial, displayItem, 0, 0, false));
                    key.reset(); // Ensure it is deleted on commit, since the client will also clear it
                } else {
                    add(new GridInventoryEntry(serial, displayItem, stored.getStackSize(), stored.getCountRequestable(),
                            stored.isCraftable()));
                }
            }

            updateHelper.commitChanges();
        }

        public void add(GridInventoryEntry entry) {
            PacketBuffer data = ensureData();

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
                data.writerIndex(ITEM_COUNT_FIELD_OFFSET);
                data.writeShort(itemCount);
                data.resetWriterIndex();

                // Build a packet and queue it
                MEInventoryUpdatePacket packet = new MEInventoryUpdatePacket();
                packet.configureWrite(data);
                packets.add(packet);

                // Reset
                data = null;
                itemCount = 0;
            }
        }

        private PacketBuffer ensureData() {
            if (data == null) {
                data = createPacketHeader(false);
            }
            return data;
        }

        private static PacketBuffer createPacketHeader(boolean fullUpdate) {
            final PacketBuffer data;
            data = new PacketBuffer(Unpooled.buffer(INITIAL_BUFFER_CAPACITY));
            // Since we don't have an instance of a packet we can't get the packet id the normal way now
            data.writeInt(BasePacketHandler.PacketTypes.PACKET_ME_INVENTORY_UPDATE.getPacketId());
            data.writeBoolean(fullUpdate);
            Preconditions.checkState(data.writerIndex() == ITEM_COUNT_FIELD_OFFSET);
            // This is a placeholder for the item count and will be added at the end
            data.writeShort(0);
            return data;
        }

        public List<MEInventoryUpdatePacket> build() {
            flushData();
            return packets;
        }

        public void buildAndSend(Consumer<MEInventoryUpdatePacket> sender) {
            for (MEInventoryUpdatePacket packet : build()) {
                sender.accept(packet);
            }
        }
    }

    public static Builder builder(boolean fullUpdate) {
        return new Builder(fullUpdate);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final Screen gs = Minecraft.getInstance().currentScreen;

        if (gs instanceof MEMonitorableScreen) {
            ((MEMonitorableScreen<?>) gs).postUpdate(fullUpdate, list);
        }
    }

}
