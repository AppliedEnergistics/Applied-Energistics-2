package appeng.menu.me.interaction;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface StackInteractionHandler {

    @Nullable
    default EmptyingAction getEmptyingResult(ItemStack stack) {
        return null;
    }

}
