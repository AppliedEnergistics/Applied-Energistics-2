package appeng.api.storage;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record LinkStatus(boolean connected, @Nullable Component statusDescription) implements ILinkStatus {
    static final LinkStatus CONNECTED = new LinkStatus(true, null);
}
