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
import java.util.List;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.CustomSlotWidget;
import appeng.client.gui.widgets.FluidSlotWidget;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.util.fluid.AEFluidStack;

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

    private List<DropTarget> getTargets(DraggingContext<AEBaseScreen> context, DraggableStack stack) {
        List<DropTarget> targets = new ArrayList<>();

        if (stack.getStack().getType() == VanillaEntryTypes.ITEM) {
            addItemStackTargets(context.getScreen(), targets);
        } else if (stack.getStack().getType() == VanillaEntryTypes.FLUID) {
            addFluidStackTargets(context.getScreen(), targets);
        }
        return targets;
    }

    /**
     * Returns possible drop-targets for ghost items.
     */
    private static void addItemStackTargets(AEBaseScreen<?> gui, List<DropTarget> targets) {
        for (Slot slot : gui.getMenu().slots) {
            if (!(slot instanceof AppEngSlot appEngSlot)) {
                continue;
            }

            if (!appEngSlot.isSlotEnabled()) {
                continue;
            }

            if (appEngSlot instanceof FakeSlot) {
                targets.add(new ItemSlotTarget(gui, appEngSlot));
            }
        }
    }

    /**
     * Returns possible drop-targets for ghost fluids.
     */
    private static void addFluidStackTargets(AEBaseScreen<?> gui, List<DropTarget> targets) {
        for (CustomSlotWidget slot : gui.getGuiSlots()) {
            if (!slot.isSlotEnabled()) {
                continue;
            }

            if (slot instanceof FluidSlotWidget) {
                targets.add(new FluidSlotTarget(gui, (FluidSlotWidget) slot));
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
            if (entryStack.getType() != VanillaEntryTypes.ITEM) {
                return false;
            }
            ItemStack itemStack = entryStack.castValue();

            NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER,
                    slot.index, itemStack));
            return false;
        }
    }

    private static class FluidSlotTarget implements DropTarget {
        private final FluidSlotWidget slot;
        private final Rectangle area;

        public FluidSlotTarget(AEBaseScreen<?> screen, FluidSlotWidget slot) {
            this.slot = slot;
            this.area = new Rectangle(screen.getGuiLeft() + slot.getTooltipAreaX(),
                    screen.getGuiTop() + slot.getTooltipAreaY(),
                    slot.getTooltipAreaWidth(),
                    slot.getTooltipAreaHeight());
        }

        @Override
        public Rectangle getArea() {
            return area;
        }

        @Override
        public boolean accept(DraggableStack ingredient) {
            var entryStack = ingredient.getStack();
            if (entryStack.getType() != VanillaEntryTypes.FLUID) {
                return false;
            }
            FluidStack fluidStack = entryStack.castValue();

            slot.setFluidStack(AEFluidStack.of(
                    FluidVariant.of(fluidStack.getFluid(), fluidStack.getTag()),
                    fluidStack.getAmount()));
            return false;
        }
    }

    interface DropTarget {
        Rectangle getArea();

        boolean accept(DraggableStack stack);
    }

}
