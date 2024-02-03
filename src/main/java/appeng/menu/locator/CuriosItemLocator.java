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
record CuriosItemLocator(int curioSlot, @Nullable BlockHitResult hitResult) implements ItemMenuHostLocator {
    public ItemStack locateItem(Player player) {
        var cap = player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (cap == null || curioSlot >= cap.getSlots()) {
            return ItemStack.EMPTY;
        }
        return cap.getStackInSlot(curioSlot);
    }

    @Override
    public boolean setItem(Player player, ItemStack stack) {
        var cap = player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (cap == null || curioSlot >= cap.getSlots()) {
            return false;
        }
        cap.extractItem(curioSlot, Integer.MAX_VALUE, false);
        return cap.insertItem(curioSlot, stack, false).isEmpty();
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
