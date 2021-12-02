package appeng.menu.me.interaction;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

/**
 * TODO: Use approach from {@link appeng.api.client.AEStackRendering} or generalize as AEStackClientFeatures
 */
public final class StackInteractions {
    private static final List<StackInteractionHandler> HANDLERS = List.of(
            new FluidInteractionHandler(),
            new ItemInteractionHandler());

    @Nullable
    public static EmptyingAction getEmptyingAction(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        for (StackInteractionHandler handler : HANDLERS) {
            var result = handler.getEmptyingResult(stack);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
