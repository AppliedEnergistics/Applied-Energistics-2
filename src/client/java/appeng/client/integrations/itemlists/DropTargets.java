package appeng.client.integrations.itemlists;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Ints;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.menu.slot.FakeSlot;

public final class DropTargets {
    private DropTargets() {
    }

    public static List<DropTarget> getTargets(AEBaseScreen<?> aeScreen) {
        List<DropTarget> targets = new ArrayList<>();
        for (var slot : aeScreen.getMenu().slots) {
            if (slot.isActive() && slot instanceof FakeSlot fakeSlot) {
                var area = new Rect2i(aeScreen.getGuiLeft() + slot.x, aeScreen.getGuiTop() + slot.y, 16, 16);
                targets.add(new FakeSlotDropTarget(area, fakeSlot));
            }
        }
        return targets;
    }

    private record FakeSlotDropTarget(Rect2i area, FakeSlot slot) implements DropTarget {
        @Override
        public boolean canDrop(GenericStack stack) {
            // Use the standard inventory function to test if the dragged stack would in theory be accepted
            return slot.canSetFilterTo(wrapFilterAsItem(stack));
        }

        @Override
        public boolean drop(GenericStack stack) {
            var itemStack = wrapFilterAsItem(stack);

            if (slot.canSetFilterTo(itemStack)) {
                slot.setFilterTo(itemStack);
                return true;
            }
            return false;
        }

        // Fake slots use GenericStacks wrapped in ItemStack for non-items internally
        private static ItemStack wrapFilterAsItem(GenericStack genericStack) {
            if (genericStack.what() instanceof AEItemKey itemKey) {
                return itemKey.toStack(Ints.saturatedCast(Math.max(1, genericStack.amount())));
            } else {
                return GenericStack.wrapInItemStack(genericStack.what(), Math.max(1, genericStack.amount()));
            }
        }

    }
}
