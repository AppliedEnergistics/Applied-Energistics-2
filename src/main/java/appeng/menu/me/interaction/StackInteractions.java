package appeng.menu.me.interaction;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * TODO: Use approach from {@link appeng.api.client.AEStackRendering} or generalize as AEStackClientFeatures
 */
public final class StackInteractions {
    private static final List<StackInteractionHandler> HANDLERS = List.of(
            new FluidInteractionHandler(),
            new ItemInteractionHandler()
    );

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
