package appeng.menu.locator;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.integration.modules.curios.CuriosIntegration;

/**
 * Implements {@link ItemMenuHostLocator} for items equipped in curios slots.
 */
record CuriosItemLocator(String slotType, int curioSlot,
        @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator {
    public ItemStack locateItem(Player player) {
        return CuriosIntegration.getStack(player, slotType, curioSlot);
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeUtf(slotType);
        buf.writeInt(curioSlot);
        buf.writeOptional(Optional.ofNullable(hitResult), BlockHitResult.STREAM_CODEC);
    }

    public static CuriosItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new CuriosItemLocator(
                buf.readUtf(),
                buf.readInt(),
                buf.readOptional(BlockHitResult.STREAM_CODEC).orElse(null));
    }

    @Override
    public String toString() {
        return "curios slot " + slotType + "#" + curioSlot;
    }
}
