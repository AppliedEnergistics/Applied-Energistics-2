package appeng.api.client;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

@OnlyIn(Dist.CLIENT)
public class AEStackRendering {
    private static volatile Map<IStorageChannel<?>, IAEStackRenderHandler<?>> renderers = new IdentityHashMap<>();

    public static synchronized <T extends IAEStack> void register(IStorageChannel<T> channel,
            IAEStackRenderHandler<T> handler) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(handler, "handler");

        var renderersCopy = new IdentityHashMap<>(renderers);
        if (renderersCopy.put(channel, handler) != null) {
            throw new IllegalArgumentException("Duplicate registration of render handler for channel " + channel);
        }
        renderers = renderersCopy;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends IAEStack> IAEStackRenderHandler<T> get(IStorageChannel<T> channel) {
        return (IAEStackRenderHandler<T>) renderers.get(channel);
    }
}
