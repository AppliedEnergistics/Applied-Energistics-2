
package appeng.core.network.serverbound;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.api.util.AEColor;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.items.tools.powered.ColorApplicatorItem;

/**
 * Switches the color of any held color applicator to the desired color.
 */
public record ColorApplicatorSelectColorPacket(@Nullable AEColor color) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ColorApplicatorSelectColorPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    ColorApplicatorSelectColorPacket::write,
                    ColorApplicatorSelectColorPacket::decode);

    public static final Type<ColorApplicatorSelectColorPacket> TYPE = CustomAppEngPayload
            .createType("color_applicator_select_color");

    @Override
    public Type<ColorApplicatorSelectColorPacket> type() {
        return TYPE;
    }

    public static ColorApplicatorSelectColorPacket decode(RegistryFriendlyByteBuf stream) {
        AEColor color = null;
        if (stream.readBoolean()) {
            color = stream.readEnum(AEColor.class);
        }
        return new ColorApplicatorSelectColorPacket(color);
    }

    public void write(RegistryFriendlyByteBuf data) {
        if (color != null) {
            data.writeBoolean(true);
            data.writeEnum(color);
        } else {
            data.writeBoolean(false);
        }
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        switchColor(player.getMainHandItem(), color);
        switchColor(player.getOffhandItem(), color);
    }

    private static void switchColor(ItemStack stack, AEColor color) {
        if (!stack.isEmpty() && stack.getItem() instanceof ColorApplicatorItem colorApplicator) {
            colorApplicator.setActiveColor(stack, color);
        }
    }
}
