package appeng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import io.netty.buffer.Unpooled;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class CodecTestUtil {
    private CodecTestUtil() {
    }

    public static <V> void testRoundtrip(StreamCodec<? super RegistryFriendlyByteBuf, V> streamCodec, V value) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        streamCodec.encode(buffer, value);
        var decoded = streamCodec.decode(buffer);
        assertEquals(value, decoded);
    }

    public static <T, V> void testRoundtrip(Codec<V> codec, V value, DynamicOps<T> ops, T encodedRepresentation) {
        var encodedType = codec.encodeStart(ops, value).getOrThrow();
        assertEquals(encodedRepresentation, encodedType);
        var decodedType = codec.decode(ops, encodedType).getOrThrow().getFirst();
        assertEquals(value, decodedType);
    }
}
