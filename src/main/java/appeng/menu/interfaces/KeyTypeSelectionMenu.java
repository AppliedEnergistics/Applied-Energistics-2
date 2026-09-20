package appeng.menu.interfaces;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import appeng.api.stacks.AEKeyType;
import appeng.api.util.KeyTypeSelection;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.SelectKeyTypePacket;
import appeng.menu.guisync.PacketWritable;

/**
 * Implemented by menus that allow the user to select key types.
 */
public interface KeyTypeSelectionMenu {
    /**
     * Used on the server side to update the key types from {@link SelectKeyTypePacket}.
     */
    KeyTypeSelection getServerKeyTypeSelection();

    /**
     * Used on the client side to read and <b>write</b> the selected key types.
     */
    SyncedKeyTypes getClientKeyTypeSelection();

    /**
     * Update a key type on the client side.
     */
    @ApiStatus.NonExtendable
    default void selectKeyType(AEKeyType keyType, boolean enabled) {
        // Send to server
        ServerboundPacket message = new SelectKeyTypePacket(keyType, enabled);
        ClientPacketDistributor.sendToServer(message);
        // Update client
        getClientKeyTypeSelection().keyTypes().put(keyType, enabled);
    }

    record SyncedKeyTypes(Map<AEKeyType, Boolean> keyTypes) implements PacketWritable {
        // FriendlyByteBuf#readMap/#writeMap were removed in 26.3. This stream codec keeps the
        // previous wire format (var-int count, var-int raw key type id, boolean) and the insertion order.
        private static final StreamCodec<ByteBuf, Map<AEKeyType, Boolean>> STREAM_CODEC = ByteBufCodecs.map(
                LinkedHashMap::new,
                ByteBufCodecs.VAR_INT.<AEKeyType>map(AEKeyType::fromRawId, keyType -> (int) keyType.getRawId()),
                ByteBufCodecs.BOOL);

        public SyncedKeyTypes() {
            this(new LinkedHashMap<>());
        }

        public SyncedKeyTypes(RegistryFriendlyByteBuf buf) {
            this(STREAM_CODEC.decode(buf));
        }

        @Override
        public void writeToPacket(RegistryFriendlyByteBuf buf) {
            STREAM_CODEC.encode(buf, keyTypes);
        }

        public List<AEKeyType> enabledSet() {
            return keyTypes.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
        }
    }
}
