package appeng.core.network.serverbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class FillCraftingGridFromRecipePacketTest {
    private static RegistryFriendlyByteBuf createBuffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(),
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }

    @Test
    void testRoundtrip() {
        var templates = NonNullList.withSize(9, ItemStack.EMPTY);

        var buffer = createBuffer();
        FillCraftingGridFromRecipePacket.STREAM_CODEC.encode(buffer,
                new FillCraftingGridFromRecipePacket(null, templates, true));
        var decoded = FillCraftingGridFromRecipePacket.STREAM_CODEC.decode(buffer);

        assertEquals(9, decoded.ingredientTemplates().size());
        assertTrue(decoded.craftMissing());
    }

    /**
     * A client claiming a huge number of templates must not make the server allocate a matching list.
     */
    @Test
    void testRejectsOversizedTemplateList() {
        var buffer = createBuffer();
        buffer.writeBoolean(false); // No recipe id
        buffer.writeInt(Integer.MAX_VALUE);
        buffer.writeBoolean(false);

        assertThrows(DecoderException.class, () -> FillCraftingGridFromRecipePacket.STREAM_CODEC.decode(buffer));
    }

    @Test
    void testRejectsWrongTemplateCount() {
        var buffer = createBuffer();
        buffer.writeBoolean(false); // No recipe id
        buffer.writeInt(8);

        assertThrows(DecoderException.class, () -> FillCraftingGridFromRecipePacket.STREAM_CODEC.decode(buffer));
    }
}
