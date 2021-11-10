/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEFluidKey;
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
class GhostIngredientHandler implements DraggableStackVisitor<AEBaseScreen> {

    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return screen instanceof AEBaseScreen;
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<AEBaseScreen> context,
            DraggableStack stack) {
        List<DropTarget> targets = getTargets(context, stack);

        return targets.stream().map(target -> BoundsProvider.ofRectangle(target.getArea()));
    }

    @Override
    public boolean acceptDraggedStack(DraggingContext<AEBaseScreen> context, DraggableStack stack) {
        var targets = getTargets(context, stack);
        var pos = context.getCurrentPosition();

        for (var target : targets) {
            if (target.getArea().contains(pos)) {
                if (target.accept(stack)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    private ItemStack wrapDraggedItem(EntryStack<?> entryStack) {
        if (entryStack.getType() == VanillaEntryTypes.ITEM) {
            return entryStack.castValue();
        } else if (entryStack.getType() == VanillaEntryTypes.FLUID) {
            FluidStack fluidStack = entryStack.castValue();

            // Wrap in a generic stack to set it anyway
            return GenericStack.wrapInItemStack(
                    new GenericStack(AEFluidKey.of(fluidStack.getFluid()), fluidStack.getAmount()));
        }
        return null;
    }

    private List<DropTarget> getTargets(DraggingContext<AEBaseScreen> context, DraggableStack stack) {
        var wrapped = wrapDraggedItem(stack.getStack());
        if (wrapped == null) {
            return Collections.emptyList();
        }

        List<DropTarget> targets = new ArrayList<>();
        addItemStackTargets(context.getScreen(), targets, wrapped);
        return targets;
    }

    /**
     * Returns possible drop-targets for ghost items.
     */
    private static void addItemStackTargets(AEBaseScreen<?> gui, List<DropTarget> targets, ItemStack draggedStack) {
        for (Slot slot : gui.getMenu().slots) {
            if (!(slot instanceof AppEngSlot appEngSlot)) {
                continue;
            }

            if (!appEngSlot.isSlotEnabled()) {
                continue;
            }

            if (appEngSlot instanceof FakeSlot fakeSlot) {
                // Use the standard inventory function to test if the dragged stack would in theory be accepted
                if (!fakeSlot.canSetFilterTo(draggedStack)) {
                    continue;
                }

                targets.add(new ItemSlotTarget(gui, appEngSlot));
            }
        }
    }

    private static class ItemSlotTarget implements DropTarget {
        private final AppEngSlot slot;
        private final Rectangle area;

        public ItemSlotTarget(AEBaseScreen<?> screen, AppEngSlot slot) {
            this.slot = slot;
            this.area = new Rectangle(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        public Rectangle getArea() {
            return area;
        }

        @Override
        public boolean accept(DraggableStack ingredient) {
            var entryStack = ingredient.getStack();
            if (entryStack.getType() == VanillaEntryTypes.ITEM) {
                ItemStack itemStack = entryStack.castValue();
                NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER,
                        slot.index, itemStack));
            } else if (entryStack.getType() == VanillaEntryTypes.FLUID) {
                FluidStack fluidStack = entryStack.castValue();

                // Wrap in a generic stack to set it anyway
                var wrappedFluid = GenericStack.wrapInItemStack(
                        new GenericStack(AEFluidKey.of(fluidStack.getFluid()), fluidStack.getAmount()));
                NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER,
                        slot.index, wrappedFluid));
            }
            return false;
        }
    }

    interface DropTarget {
        Rectangle getArea();

        boolean accept(DraggableStack stack);
    }

}
