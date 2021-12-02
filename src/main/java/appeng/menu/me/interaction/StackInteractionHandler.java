package appeng.menu.me.interaction;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

public interface StackInteractionHandler {

    @Nullable
    default EmptyingAction getEmptyingResult(ItemStack stack) {
        return null;
    }

}
