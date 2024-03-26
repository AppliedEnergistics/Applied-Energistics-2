package appeng.core.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.core.AppEng;

public interface CustomAppEngPayload extends CustomPacketPayload {
    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String name) {
        return new CustomPacketPayload.Type<>(AppEng.makeId(name));
    }
}
