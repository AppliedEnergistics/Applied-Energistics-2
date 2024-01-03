
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.network.ClientboundPacket;

/**
 * Sends the content for a single {@link appeng.helpers.patternprovider.PatternContainer} shown in the pattern access
 * terminal to the client.
 */
public record PatternAccessTerminalPacket(
        boolean fullUpdate,
        long inventoryId,
        int inventorySize, // Only valid if fullUpdate
        long sortBy, // Only valid if fullUpdate
        PatternContainerGroup group, // Only valid if fullUpdate
        Int2ObjectMap<ItemStack> slots) implements ClientboundPacket {

    public static PatternAccessTerminalPacket decode(FriendlyByteBuf stream) {
        var inventoryId = stream.readVarLong();
        var fullUpdate = stream.readBoolean();
        int inventorySize = 0;
        long sortBy = 0;
        PatternContainerGroup group = null;
        if (fullUpdate) {
            inventorySize = stream.readVarInt();
            sortBy = stream.readVarLong();
            group = PatternContainerGroup.readFromPacket(stream);
        }

        var slotsCount = stream.readVarInt();
        var slots = new Int2ObjectArrayMap<ItemStack>(slotsCount);
        for (int i = 0; i < slotsCount; i++) {
            var slot = stream.readVarInt();
            var item = stream.readItem();
            slots.put(slot, item);
        }
        return new PatternAccessTerminalPacket(fullUpdate, inventoryId, inventorySize, sortBy, group, slots);
    }

    @Override
    public void write(FriendlyByteBuf data) {
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
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        if (Minecraft.getInstance().screen instanceof PatternAccessTermScreen<?>patternAccessTerminal) {
            if (fullUpdate) {
                patternAccessTerminal.postFullUpdate(this.inventoryId, sortBy, group, inventorySize, slots);
            } else {
                patternAccessTerminal.postIncrementalUpdate(this.inventoryId, slots);
            }
        }
    }
}
