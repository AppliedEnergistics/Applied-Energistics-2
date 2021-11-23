package appeng.menu.me.interaction;

import appeng.helpers.FluidContainerHelper;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;


public class FluidInteractionHandler implements StackInteractionHandler {
    @Override
    public EmptyingAction getEmptyingResult(ItemStack stack) {
        var fluidStack = FluidContainerHelper.getContainedStack(stack);
        if (fluidStack == null) {
            return null;
        }

        var description = new TextComponent("Store ").append(fluidStack.what().getDisplayName());
        return new EmptyingAction(description);
    }
}
