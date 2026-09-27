package appeng.menu.guisync;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class NullableSynchronizedValue<T> {
    private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
    @Nullable
    private T value;

    private NullableSynchronizedValue(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        this.streamCodec = streamCodec;
    }

    @Nullable
    public T get() {
        return value;
    }
}
