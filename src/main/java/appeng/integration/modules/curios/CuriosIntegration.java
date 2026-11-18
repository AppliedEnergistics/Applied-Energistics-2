package appeng.integration.modules.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Optional integration with Curios. Curios is not a runtime requirement; the real implementation is installed by
 * {@link CuriosEntrypoint}, a conditional mod entrypoint that only runs if Curios is loaded. Without it, a no-op
 * implementation is used.
 * <p>
 * Stacks are accessed through the Curios API rather than its {@code ResourceHandler} capability, since the latter can
 * only hand out copies, while AE2 needs the live stack so it can be modified in-place.
 */
public final class CuriosIntegration {
    private static final CuriosAccess NOOP = new CuriosAccess() {
        @Override
        public ItemStack getStack(Player player, String slotType, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public void forEachStack(Player player, StackVisitor visitor) {
        }
    };

    private static volatile CuriosAccess access = NOOP;

    private CuriosIntegration() {
    }

    @FunctionalInterface
    public interface StackVisitor {
        void visit(String slotType, int index, ItemStack stack);
    }

    static void setAccess(CuriosAccess access) {
        CuriosIntegration.access = access;
    }

    /**
     * @return The live stack in the given curios slot, or {@link ItemStack#EMPTY}.
     */
    public static ItemStack getStack(Player player, String slotType, int index) {
        return access.getStack(player, slotType, index);
    }

    /**
     * Visits the live stacks of all curios slots of the player.
     */
    public static void forEachStack(Player player, StackVisitor visitor) {
        access.forEachStack(player, visitor);
    }
}
