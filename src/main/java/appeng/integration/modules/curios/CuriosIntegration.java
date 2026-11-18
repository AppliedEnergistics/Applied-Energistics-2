package appeng.integration.modules.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/**
 * Optional integration with Curios. Curios is not a runtime requirement, so all access to its API is funneled through
 * this class, which only touches {@link CuriosAccess} (the only class that references Curios types) if Curios is
 * loaded.
 * <p>
 * Stacks are accessed through the Curios API rather than its {@code ResourceHandler} capability, since the latter can
 * only hand out copies, while AE2 needs the live stack so it can be modified in-place.
 */
public final class CuriosIntegration {
    private static Boolean loaded;

    private CuriosIntegration() {
    }

    @FunctionalInterface
    public interface StackVisitor {
        void visit(String slotType, int index, ItemStack stack);
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get() != null && ModList.get().isLoaded("curios");
        }
        return loaded;
    }

    /**
     * @return The live stack in the given curios slot, or {@link ItemStack#EMPTY}.
     */
    public static ItemStack getStack(Player player, String slotType, int index) {
        return isLoaded() ? CuriosAccess.getStack(player, slotType, index) : ItemStack.EMPTY;
    }

    /**
     * Visits the live stacks of all curios slots of the player.
     */
    public static void forEachStack(Player player, StackVisitor visitor) {
        if (isLoaded()) {
            CuriosAccess.forEachStack(player, visitor);
        }
    }
}
