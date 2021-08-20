package appeng.core.sync.packets;

import net.minecraft.network.FriendlyByteBuf;

public interface ICustomEntity {

    default void writeAdditionalSpawnData(FriendlyByteBuf buf) {
    }

    default void readAdditionalSpawnData(FriendlyByteBuf buf) {
    }

}
