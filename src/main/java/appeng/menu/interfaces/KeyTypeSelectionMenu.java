package appeng.menu.interfaces;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.PacketDistributor;

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
        PacketDistributor.sendToServer(message);
        // Update client
        getClientKeyTypeSelection().keyTypes().put(keyType, enabled);
    }

    record SyncedKeyTypes(Map<AEKeyType, Boolean> keyTypes) implements PacketWritable {
        public SyncedKeyTypes() {
            this(new LinkedHashMap<>());
        }

        public SyncedKeyTypes(RegistryFriendlyByteBuf buf) {
            this(buf.<AEKeyType, Boolean, Map<AEKeyType, Boolean>>readMap(LinkedHashMap::new,
                    b -> AEKeyType.fromRawId(b.readVarInt()), FriendlyByteBuf::readBoolean));
        }

        @Override
        public void writeToPacket(RegistryFriendlyByteBuf buf) {
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
