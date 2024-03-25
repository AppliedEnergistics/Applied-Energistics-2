package appeng.api.stacks;

import appeng.util.BootstrapMinecraft;
import appeng.util.CodecTestUtil;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@BootstrapMinecraft
class AEKeyTypeTest {
    @Test
    void testItemJsonRoundtrip() {
        testKeyTypeRoundtrip(AEKeyType.items(), JsonOps.INSTANCE, new JsonPrimitive("ae2:i"));
    }

    @Test
    void testFluidJsonRoundtrip() {
        testKeyTypeRoundtrip(AEKeyType.fluids(), JsonOps.INSTANCE, new JsonPrimitive("ae2:f"));
    }

    @Test
    void testItemNbtRoundtrip() {
        testKeyTypeRoundtrip(AEKeyType.items(), NbtOps.INSTANCE, StringTag.valueOf("ae2:i"));
    }

    @Test
    void testFluidNbtRoundtrip() {
        testKeyTypeRoundtrip(AEKeyType.fluids(), NbtOps.INSTANCE, StringTag.valueOf("ae2:f"));
    }

    private static <T> void testKeyTypeRoundtrip(AEKeyType type, DynamicOps<T> ops, T encodedValue) {
        CodecTestUtil.testRoundtrip(AEKeyType.CODEC, type, ops, encodedValue);
    }

    @Test
    void testItemNetworkRoundtrip() {
        CodecTestUtil.testRoundtrip(AEKeyType.STREAM_CODEC, AEKeyType.items());
    }

    @Test
    void testFluidNetworkRoundtrip() {
        CodecTestUtil.testRoundtrip(AEKeyType.STREAM_CODEC, AEKeyType.fluids());
    }
}