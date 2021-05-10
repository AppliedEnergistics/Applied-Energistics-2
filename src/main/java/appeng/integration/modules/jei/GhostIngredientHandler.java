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

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.CustomSlotWidget;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.FakeSlot;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.InventoryAction;

/**
 * JEI allows ingredients to be dragged from a JEI panel onto compatible slots to set filters and the like without
 * having the actual item in hand.
 */
@SuppressWarnings("rawtypes")
class GhostIngredientHandler implements IGhostIngredientHandler<AEBaseScreen> {
    @Override
    public <I> List<Target<I>> getTargets(AEBaseScreen gui, I ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();

        if (ingredient instanceof ItemStack) {
            addItemStackTargets(gui, targets);
        } else if (ingredient instanceof FluidStack) {
            addFluidStackTargets(gui, targets);
        }

        return targets;
    }

    /**
     * Returns possible drop-targets for ghost items.
     */
    @SuppressWarnings("unchecked")
    private static <I> void addItemStackTargets(AEBaseScreen<?> gui, List<Target<I>> targets) {
        for (Slot slot : gui.getContainer().inventorySlots) {
            if (!(slot instanceof AppEngSlot)) {
                continue;
            }

            AppEngSlot appEngSlot = (AppEngSlot) slot;

            if (!appEngSlot.isSlotEnabled()) {
                continue;
            }

            if (appEngSlot instanceof FakeSlot) {
                targets.add((Target<I>) new ItemSlotTarget(gui, appEngSlot));
            }
        }
    }

    /**
     * Returns possible drop-targets for ghost fluids.
     */
    @SuppressWarnings("unchecked")
    private static <I> void addFluidStackTargets(AEBaseScreen<?> gui, List<Target<I>> targets) {
        for (CustomSlotWidget slot : gui.getGuiSlots()) {
            if (!slot.isSlotEnabled()) {
                continue;
            }

            if (slot instanceof FluidSlotWidget) {
                targets.add((Target<I>) new FluidSlotTarget(gui, (FluidSlotWidget) slot));
            }
        }
    }

    @Override
    public void onComplete() {
    }

    private static class ItemSlotTarget implements Target<ItemStack> {
        private final AppEngSlot slot;
        private final Rectangle2d area;

        public ItemSlotTarget(AEBaseScreen<?> screen, AppEngSlot slot) {
            this.slot = slot;
            this.area = new Rectangle2d(screen.getGuiLeft() + slot.xPos, screen.getGuiTop() + slot.yPos, 16, 16);
        }

        @Override
        public Rectangle2d getArea() {
            return area;
        }

        @Override
        public void accept(ItemStack ingredient) {
            NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER,
                    slot.slotNumber, ingredient));
        }
    }

    private static class FluidSlotTarget implements Target<FluidStack> {
        private final FluidSlotWidget slot;
        private final Rectangle2d area;

        public FluidSlotTarget(AEBaseScreen<?> screen, FluidSlotWidget slot) {
            this.slot = slot;
            this.area = new Rectangle2d(screen.getGuiLeft() + slot.getTooltipAreaX(),
                    screen.getGuiTop() + slot.getTooltipAreaY(),
                    slot.getTooltipAreaWidth(),
                    slot.getTooltipAreaWidth());
        }

        @Override
        public Rectangle2d getArea() {
            return area;
        }

        @Override
        public void accept(FluidStack ingredient) {
            slot.setFluidStack(AEFluidStack.fromFluidStack(ingredient));
        }
    }

}
