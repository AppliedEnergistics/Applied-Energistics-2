
package appeng.core.network.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

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

    public static final StreamCodec<RegistryFriendlyByteBuf, PatternAccessTerminalPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    PatternAccessTerminalPacket::write,
                    PatternAccessTerminalPacket::decode);

    private static final StreamCodec<RegistryFriendlyByteBuf, Int2ObjectMap<ItemStack>> SLOTS_STREAM_CODEC = ByteBufCodecs
            .map(
                    Int2ObjectOpenHashMap::new, ByteBufCodecs.SHORT.map(Short::intValue, Integer::shortValue),
                    ItemStack.OPTIONAL_STREAM_CODEC, 128);

    public static final Type<PatternAccessTerminalPacket> TYPE = CustomAppEngPayload
            .createType("pattern_access_terminal");

    @Override
    public Type<PatternAccessTerminalPacket> type() {
        return TYPE;
    }

    public static PatternAccessTerminalPacket decode(RegistryFriendlyByteBuf stream) {
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

        var slots = SLOTS_STREAM_CODEC.decode(stream);
        return new PatternAccessTerminalPacket(fullUpdate, inventoryId, inventorySize, sortBy, group, slots);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeVarLong(inventoryId);
        data.writeBoolean(fullUpdate);
        if (fullUpdate) {
            data.writeVarInt(inventorySize);
            data.writeVarLong(sortBy);
            group.writeToPacket(data);
        }
        SLOTS_STREAM_CODEC.encode(data, slots);
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
}
