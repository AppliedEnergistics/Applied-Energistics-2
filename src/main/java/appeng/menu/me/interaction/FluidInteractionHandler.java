package appeng.menu.me.interaction;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import appeng.helpers.FluidContainerHelper;

public class FluidInteractionHandler implements StackInteractionHandler {
    @Override
    public EmptyingAction getEmptyingResult(ItemStack stack) {
        var fluidStack = FluidContainerHelper.getContainedStack(stack);
        if (fluidStack == null) {
            return null;
        }

        var description = fluidStack.what().getDisplayName();
        return new EmptyingAction(description, fluidStack.what(), fluidStack.amount());
    }
}
