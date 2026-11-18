package appeng.integration.modules.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;

import top.theillusivec4.curios.api.CuriosCapability;

import appeng.core.AppEng;

/**
 * Conditional mod entrypoint that is only constructed (and loaded) if Curios is present. This is the only place that
 * may reference the Curios API.
 */
@Mod(value = AppEng.MOD_ID, depends = "curios")
public class CuriosEntrypoint {
    public CuriosEntrypoint() {
        CuriosIntegration.setAccess(new Impl());
    }

    /**
     * Accesses stacks through the Curios inventory API rather than its {@code ResourceHandler} capability, since the
     * latter can only hand out copies, while AE2 needs the live stacks so it can modify them in-place.
     */
    private static final class Impl implements CuriosAccess {
        @Override
        public ItemStack getStack(Player player, String slotType, int index) {
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

        @Override
        public void forEachStack(Player player, CuriosIntegration.StackVisitor visitor) {
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
}
