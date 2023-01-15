package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.sync.BasePacket;

/**
 * Sends the content for a single {@link appeng.helpers.patternprovider.PatternContainer} shown in the pattern access
 * terminal to the client.
 */
public class PatternAccessTerminalPacket extends BasePacket {

    // input.
    private boolean fullUpdate;
    private long inventoryId;
    private int inventorySize; // Only valid if fullUpdate
    private long sortBy; // Only valid if fullUpdate
    private PatternContainerGroup group; // Only valid if fullUpdate
    private Int2ObjectMap<ItemStack> slots;

    public PatternAccessTerminalPacket(FriendlyByteBuf stream) {
        inventoryId = stream.readVarLong();
        fullUpdate = stream.readBoolean();
        if (fullUpdate) {
            inventorySize = stream.readVarInt();
            sortBy = stream.readVarLong();
            group = PatternContainerGroup.readFromPacket(stream);
        }

        var slotsCount = stream.readVarInt();
        slots = new Int2ObjectArrayMap<>(slotsCount);
        for (int i = 0; i < slotsCount; i++) {
            var slot = stream.readVarInt();
            var item = stream.readItem();
            slots.put(slot, item);
        }
    }

    // api
    private PatternAccessTerminalPacket(boolean fullUpdate,
            long inventoryId,
            int inventorySize,
            long sortBy,
            PatternContainerGroup group,
            Int2ObjectMap<ItemStack> slots) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer(2048));
        data.writeInt(this.getPacketID());
        data.writeVarLong(inventoryId);
        data.writeBoolean(fullUpdate);
        if (fullUpdate) {
            data.writeVarInt(inventorySize);
            data.writeVarLong(sortBy);
            group.writeToPacket(data);
        }
        data.writeVarInt(slots.size());
        for (var entry : slots.int2ObjectEntrySet()) {
            data.writeVarInt(entry.getIntKey());
            data.writeItem(entry.getValue());
        }
        this.configureWrite(data);
    }

    public static PatternAccessTerminalPacket fullUpdate(long inventoryId,
            int inventorySize,
            long sortBy,
            PatternContainerGroup group,
            Int2ObjectMap<ItemStack> slots) {
        return new PatternAccessTerminalPacket(
                true,
                inventoryId,
                inventorySize,
                sortBy,
                group,
                slots);
    }

    public static PatternAccessTerminalPacket incrementalUpdate(long inventoryId,
            Int2ObjectMap<ItemStack> slots) {
        return new PatternAccessTerminalPacket(
                false,
                inventoryId,
                0,
                0,
                null,
                slots);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(Player player) {
        if (Minecraft.getInstance().screen instanceof PatternAccessTermScreen<?>patternAccessTerminal) {
            if (fullUpdate) {
                patternAccessTerminal.postFullUpdate(this.inventoryId, sortBy, group, inventorySize, slots);
            } else {
                patternAccessTerminal.postIncrementalUpdate(this.inventoryId, slots);
            }
        }
    }
}
