package appeng.core.network;

import java.util.Locale;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

public interface CustomAppEngPayload extends CustomPacketPayload {
    static ResourceLocation makeId(Class<? extends CustomPacketPayload> payloadClass) {
        return AppEng.makeId(payloadClass.getSimpleName().toLowerCase(Locale.ROOT));
    }

    @Override
    default ResourceLocation id() {
        return makeId(getClass());
    }
}
