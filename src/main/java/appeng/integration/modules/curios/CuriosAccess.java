package appeng.integration.modules.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.CuriosCapability;

/**
 * Directly touches the Curios API. <strong>Must only be referenced from {@link CuriosIntegration}, and only after
 * checking that Curios is loaded</strong>, otherwise class loading will fail.
 */
final class CuriosAccess {
    private CuriosAccess() {
    }

    static ItemStack getStack(Player player, String slotType, int index) {
        var inventory = player.getCapability(CuriosCapability.INVENTORY);
        if (inventory == null) {
            return ItemStack.EMPTY;
        }
        return inventory.getStacksHandler(slotType).map(handler -> {
            var stacks = handler.getStacks();
            if (index < 0 || index >= stacks.getSlots()) {
                return ItemStack.EMPTY;
            }
            // This is the live stack, not a copy.
            return stacks.getStackInSlot(index);
        }).orElse(ItemStack.EMPTY);
    }

    static void forEachStack(Player player, CuriosIntegration.StackVisitor visitor) {
        var inventory = player.getCapability(CuriosCapability.INVENTORY);
        if (inventory == null) {
            return;
        }
        for (var entry : inventory.getCurios().entrySet()) {
            var stacks = entry.getValue().getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                // Live stack, not a copy
                visitor.visit(entry.getKey(), i, stacks.getStackInSlot(i));
            }
        }
    }
}
