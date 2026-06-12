package appeng.menu.guisync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class SynchronizedValue<T> {
    private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
    private final T initialValue;
    private T value;

    private SynchronizedValue(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, T initialValue) {
        this.streamCodec = streamCodec;
        this.initialValue = initialValue;
        this.value = initialValue;
    }

    public T get() {
        return value;
    }

    public static <T> SynchronizedValue<T> create(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec,
            T initialValue) {
        return new SynchronizedValue<>(streamCodec, initialValue);
    }

    public void set(T value) {
        this.value = value;
    }
}
