package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;

/**
 * JEI allows ingredients to be dragged from a JEI panel onto compatible slots to set filters and the like without
 * having the actual item in hand.
 */
@SuppressWarnings("rawtypes")
class GhostIngredientHandler implements IGhostIngredientHandler<AEBaseScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(AEBaseScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
        var wrapped = wrapDraggedItem(ingredient.getType(), ingredient.getIngredient());
        if (wrapped == null) {
            return Collections.emptyList();
        }

        List<Target<I>> targets = new ArrayList<>();

        addItemStackTargets(gui, targets, wrapped, ingredient.getType());

        return targets;
    }

    @Nullable
    private static <T> ItemStack wrapDraggedItem(IIngredientType<T> type, T ingredient) {
        return VanillaTypes.ITEM_STACK.castIngredient(ingredient)
                .or(() -> {
                    var genericStack = GenericEntryStackHelper.ingredientToStack(type, ingredient);
                    if (genericStack != null) {
                        return Optional.of(GenericStack.wrapInItemStack(genericStack));
                    } else {
                        return Optional.empty();
                    }
                })
                .orElse(null);
    }

    /**
     * Returns possible drop-targets for ghost items.
     */
    @SuppressWarnings("unchecked")
    private static <I> void addItemStackTargets(AEBaseScreen<?> gui, List<Target<I>> targets, ItemStack draggedStack,
            IIngredientType<I> type) {
        for (var slot : gui.getMenu().slots) {
            if (slot.isActive() && slot instanceof FakeSlot fakeSlot) {
                // Use the standard inventory function to test if the dragged stack would in theory be accepted
                if (!fakeSlot.canSetFilterTo(draggedStack)) {
                    continue;
                }

                targets.add(new ItemSlotTarget<>(type, gui, fakeSlot));
            }
        }
    }

    @Override
    public void onComplete() {
    }

    private static class ItemSlotTarget<I> implements Target<I> {
        private final IIngredientType<I> type;
        private final AppEngSlot slot;
        private final Rect2i area;

        public ItemSlotTarget(IIngredientType<I> type, AEBaseScreen<?> screen, AppEngSlot slot) {
            this.type = type;
            this.slot = slot;
            this.area = new Rect2i(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(I ingredient) {
            var wrapped = wrapDraggedItem(type, ingredient);

            if (wrapped != null) {
                NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER,
                        slot.index, wrapped));
            }
        }
    }

}
