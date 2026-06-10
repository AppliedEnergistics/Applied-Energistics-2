package appeng.api.integrations.curios;

import appeng.menu.locator.ItemMenuHostLocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;

import java.util.Optional;

/**
 * Implements {@link ItemMenuHostLocator} for items equipped in curios slots.
 */
record CuriosItemLocator(int curioSlot, @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator {
    public static ItemMenuHostLocator forCurioSlot(int curioSlot) {
        return new CuriosItemLocator(curioSlot, null);
    }

    public static ItemMenuHostLocator forCurioSlot(int curioSlot, UseOnContext context) {
        var hitResult = new BlockHitResult(context.getClickLocation(), context.getHorizontalDirection(), context.getClickedPos(),
                context.isInside());

        return new CuriosItemLocator(curioSlot, hitResult);
    }

    public ItemStack locateItem(Player player) {
        var cap = player.getCapability(CuriosCapability.ITEM_HANDLER);
        if (cap == null || curioSlot >= cap.size()) {
            return ItemStack.EMPTY;
        }
        return cap.getResource(curioSlot).toStack();
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeInt(curioSlot);
        buf.writeOptional(Optional.ofNullable(hitResult), FriendlyByteBuf::writeBlockHitResult);
    }

    public static CuriosItemLocator readFromPacket(FriendlyByteBuf buf) {
        return new CuriosItemLocator(
                buf.readInt(),
                buf.readOptional(FriendlyByteBuf::readBlockHitResult).orElse(null));
    }

    @Override
    public String toString() {
        return "curios slot " + curioSlot;
    }
}
