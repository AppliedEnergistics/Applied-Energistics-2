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
 *
 * @param slotType  The Curios slot type identifier. If empty, {@code curioSlot} is instead an index across all slot
 *                  types (legacy behavior, see {@link MenuLocators#forCurioSlot(int)}).
 * @param curioSlot The index within the slot type.
 */
record CuriosItemLocator(String slotType, int curioSlot,
        @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator {
    public ItemStack locateItem(Player player) {
        return CuriosIntegration.getStack(player, slotType, curioSlot);
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeUtf(slotType);
        buf.writeInt(curioSlot);
        buf.writeOptional(Optional.ofNullable(hitResult), FriendlyByteBuf::writeBlockHitResult);
    }

    public static CuriosItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new CuriosItemLocator(
                buf.readUtf(),
                buf.readInt(),
                buf.readOptional(FriendlyByteBuf::readBlockHitResult).orElse(null));
    }

    @Override
    public String toString() {
        return "curios slot " + slotType + "#" + curioSlot;
    }
}
