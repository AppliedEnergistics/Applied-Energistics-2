
package appeng.core.network.serverbound;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.api.util.AEColor;
import appeng.core.network.ServerboundPacket;
import appeng.items.tools.powered.ColorApplicatorItem;

/**
 * Switches the color of any held color applicator to the desired color.
 */
public record ColorApplicatorSelectColorPacket(@Nullable AEColor color) implements ServerboundPacket {
    public static ColorApplicatorSelectColorPacket decode(FriendlyByteBuf stream) {
        AEColor color = null;
        if (stream.readBoolean()) {
            color = stream.readEnum(AEColor.class);
        }
        return new ColorApplicatorSelectColorPacket(color);
    }

    @Override
    public void write(FriendlyByteBuf data) {
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
