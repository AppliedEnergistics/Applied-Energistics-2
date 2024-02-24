package appeng.menu.interfaces;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.stacks.AEKeyType;
import appeng.api.util.KeyTypeSelection;
import appeng.core.network.NetworkHandler;
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
        NetworkHandler.instance().sendToServer(new SelectKeyTypePacket(keyType, enabled));
        // Update client
        getClientKeyTypeSelection().keyTypes().put(keyType, enabled);
    }

    record SyncedKeyTypes(Map<AEKeyType, Boolean> keyTypes) implements PacketWritable {
        public SyncedKeyTypes() {
            this(new LinkedHashMap<>());
        }

        public SyncedKeyTypes(FriendlyByteBuf buf) {
            this(buf.<AEKeyType, Boolean, Map<AEKeyType, Boolean>>readMap(LinkedHashMap::new,
                    b -> AEKeyType.fromRawId(b.readVarInt()), FriendlyByteBuf::readBoolean));
        }

        @Override
        public void writeToPacket(FriendlyByteBuf buf) {
            buf.writeMap(
                    keyTypes,
                    (b, keyType) -> b.writeVarInt(keyType.getRawId()),
                    FriendlyByteBuf::writeBoolean);
        }

        public List<AEKeyType> enabledSet() {
            return keyTypes.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
        }
    }
}
