package appeng.core.sync.packets;

import net.minecraft.network.PacketBuffer;

public interface ICustomEntity {

    default void writeAdditionalSpawnData(PacketBuffer buf) {
    }

    default void readAdditionalSpawnData(PacketBuffer buf) {
    }

}
