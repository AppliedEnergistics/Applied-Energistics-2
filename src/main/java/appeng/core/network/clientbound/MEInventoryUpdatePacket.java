
package appeng.core.network.clientbound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.AEKeyFilter;
import appeng.core.AELog;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.menu.me.common.MEStorageMenu;

public record MEInventoryUpdatePacket(
        boolean fullUpdate,
        int containerId,
        @Nullable List<GridInventoryEntry> entries,
        int encodedEntryCount,
        @Nullable RegistryFriendlyByteBuf encodedEntries

) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, MEInventoryUpdatePacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    MEInventoryUpdatePacket::write,
                    MEInventoryUpdatePacket::decode);

    public static final Type<MEInventoryUpdatePacket> TYPE = CustomAppEngPayload.createType("me_inventory_update");

    @Override
    public Type<MEInventoryUpdatePacket> type() {
        return TYPE;
    }

    /**
     * Maximum size of a single packet before it will be flushed forcibly.
     */
    private static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 512 * 1024;

    /**
     * Initial buffer size for an update packet.
     */
    private static final int INITIAL_BUFFER_CAPACITY = 2 * 1024;

    public static MEInventoryUpdatePacket decode(RegistryFriendlyByteBuf data) {
        var containerId = data.readVarInt();
        var fullUpdate = data.readBoolean();
        var encodedEntryCount = data.readVarInt();
        var entries = decodeEntriesPayload(encodedEntryCount, data);
        return new MEInventoryUpdatePacket(fullUpdate, containerId, entries, 0, null);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeVarInt(containerId);
        data.writeBoolean(fullUpdate);
        data.writeVarInt(encodedEntryCount);
        if (encodedEntryCount > 0) {
            if (encodedEntries == null) {
                throw new UnsupportedOperationException("Use the builder");
            }
            data.ensureWritable(encodedEntries.readableBytes());
            encodedEntries.getBytes(encodedEntries.readerIndex(), data, encodedEntries.readableBytes());
        }
    }

    public static class Builder {
        private final List<MEInventoryUpdatePacket> packets = new ArrayList<>();

        private final int containerId;
        private boolean fullUpdate;
        private final RegistryAccess registryAccess;

        @Nullable
        private RegistryFriendlyByteBuf encodedEntries;

        private int entryCount;

        @Nullable
        private AEKeyFilter filter;

        public Builder(int containerId, boolean fullUpdate, RegistryAccess registryAccess) {
            this.containerId = containerId;
            this.fullUpdate = fullUpdate;
            this.registryAccess = registryAccess;
        }

        public void setFilter(@Nullable AEKeyFilter filter) {
            this.filter = filter;
        }

        public void addFull(IncrementalUpdateHelper updateHelper,
                KeyCounter networkStorage,
                Set<AEKey> craftables,
                KeyCounter requestables) {
            var keys = new HashSet<AEKey>();
            keys.addAll(networkStorage.keySet());
            keys.addAll(craftables);
            keys.addAll(requestables.keySet());

            for (var key : keys) {
                if (this.filter != null && !this.filter.matches(key)) {
                    continue;
                }

                long serial = updateHelper.getOrAssignSerial(key);
                add(new GridInventoryEntry(
                        serial,
                        key,
                        networkStorage.get(key),
                        requestables.get(key),
                        craftables.contains(key)));
            }
        }

        public void addChanges(IncrementalUpdateHelper updateHelper,
                KeyCounter networkStorage,
                Set<AEKey> craftables,
                KeyCounter requestables) {
            for (AEKey key : updateHelper) {
                if (this.filter != null && !this.filter.matches(key)) {
                    continue;
                }

                AEKey sendKey;
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
                    add(new GridInventoryEntry(serial, sendKey, 0, 0, false));
                    updateHelper.removeSerial(key);
                } else {
                    add(new GridInventoryEntry(serial, sendKey, storedAmount, requestable, craftable));
                }
            }

            updateHelper.commitChanges();
        }

        public void add(GridInventoryEntry entry) {
            RegistryFriendlyByteBuf data = ensureData();

            // This should only error out if the entire packet exceeds about 2 megabytes of memory,
            // if any item writes that much junk to a share tag, it's acceptable to crash.
            // We'll normally flush much much earlier (32k)
            writeEntry(data, entry);

            ++entryCount;

            if (data.writerIndex() >= UNCOMPRESSED_PACKET_BYTE_LIMIT || entryCount >= Short.MAX_VALUE) {
                flushData();
            }
        }

        private void flushData() {
            if (encodedEntries != null) {
                // Build a packet and queue it
                var packet = new MEInventoryUpdatePacket(fullUpdate, containerId, null, entryCount, encodedEntries);
                packets.add(packet);

                // Reset
                encodedEntries = null;
                entryCount = 0;
                fullUpdate = false; // Only the first packet in a chain is a full update
            }
        }

        private RegistryFriendlyByteBuf ensureData() {
            if (encodedEntries == null) {
                encodedEntries = new RegistryFriendlyByteBuf(Unpooled.buffer(INITIAL_BUFFER_CAPACITY), registryAccess);
            }
            return encodedEntries;
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

    public static Builder builder(int containerId, boolean fullUpdate, RegistryAccess registryAccess) {
        return new Builder(containerId, fullUpdate, registryAccess);
    }

    /**
     * Writes this entry to a packet buffer for shipping it to the client.
     */
    private static void writeEntry(RegistryFriendlyByteBuf buffer, GridInventoryEntry entry) {
        buffer.writeVarLong(entry.getSerial());
        AEKey.writeOptionalKey(buffer, entry.getWhat());
        buffer.writeVarLong(entry.getStoredAmount());
        buffer.writeVarLong(entry.getRequestableAmount());
        buffer.writeBoolean(entry.isCraftable());
    }

    /**
     * Reads an inventory entry from a packet.
     */
    public static GridInventoryEntry readEntry(RegistryFriendlyByteBuf buffer) {
        long serial = buffer.readVarLong();
        AEKey what = AEKey.readOptionalKey(buffer);
        long storedAmount = buffer.readVarLong();
        long requestableAmount = buffer.readVarLong();
        boolean craftable = buffer.readBoolean();
        return new GridInventoryEntry(serial, what, storedAmount, requestableAmount, craftable);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        if (player.containerMenu.containerId == containerId
                && player.containerMenu instanceof MEStorageMenu meMenu) {
            var clientRepo = meMenu.getClientRepo();
            if (clientRepo == null) {
                AELog.info("Ignoring ME inventory update packet because no client repo is available.");
                return;
            }

            // In singleplayer, we're just getting the exact same instance that the builder created
            // so it has the pre-encoded data.
            var actualEntries = entries;
            if (actualEntries == null && encodedEntries != null) {
                actualEntries = decodeEntriesPayload(encodedEntryCount, encodedEntries);
            }

            if (actualEntries != null) {
                clientRepo.handleUpdate(fullUpdate, actualEntries);
            }
        }
    }

    @NotNull
    private static ArrayList<GridInventoryEntry> decodeEntriesPayload(int entryCount, RegistryFriendlyByteBuf data) {
        // We need to access the current screen to know which storage channel was used to serialize this data
        var entries = new ArrayList<GridInventoryEntry>(entryCount);
        for (int i = 0; i < entryCount; i++) {
            entries.add(readEntry(data));
        }
        return entries;
    }

}
