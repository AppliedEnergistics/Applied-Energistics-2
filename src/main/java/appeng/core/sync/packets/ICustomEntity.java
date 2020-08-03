package appeng.core.sync.packets;

import net.minecraft.network.PacketByteBuf;

public interface ICustomEntity {

    default void writeAdditionalSpawnData(PacketByteBuf buf) {
    }

    default void readAdditionalSpawnData(PacketByteBuf buf) {
    }

}
