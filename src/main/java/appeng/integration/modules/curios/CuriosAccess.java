package appeng.integration.modules.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Abstracts access to Curios. The real implementation lives in {@link CuriosEntrypoint}, which is only loaded when
 * Curios is present.
 */
interface CuriosAccess {
    /**
     * @return The live stack in the given curios slot, or {@link ItemStack#EMPTY}.
     */
    ItemStack getStack(Player player, String slotType, int index);

    /**
     * Visits the live stacks of all curios slots of the player.
     */
    void forEachStack(Player player, CuriosIntegration.StackVisitor visitor);
}
