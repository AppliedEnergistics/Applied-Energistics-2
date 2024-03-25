package appeng.api.storage;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;

public record LinkStatus(boolean connected, @Nullable Component statusDescription) implements ILinkStatus {
    static final LinkStatus CONNECTED = new LinkStatus(true, null);
}
