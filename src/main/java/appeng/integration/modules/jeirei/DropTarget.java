package appeng.integration.modules.jeirei;

import net.minecraft.client.renderer.Rect2i;

import appeng.api.stacks.GenericStack;

public interface DropTarget {
    Rect2i area();

    boolean canDrop(GenericStack stack);

    boolean drop(GenericStack stack);
}
