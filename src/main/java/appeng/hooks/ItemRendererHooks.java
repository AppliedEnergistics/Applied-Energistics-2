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
package appeng.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

import appeng.client.gui.style.FluidBlitter;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.items.misc.FluidDummyItem;

public final class ItemRendererHooks {

    // Prevents recursion in the hook below
    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    private ItemRendererHooks() {
    }

    /**
     * This hook will exchange the rendered item model for encoded patterns to the item being crafted by them if shift
     * is held.
     */
    public static boolean onRenderGuiItemModel(ItemRenderer renderer, ItemStack stack, int x, int y) {
        if (OVERRIDING_FOR.get() == stack) {
            return false; // Don't allow recursive model replacements
        }

        if (stack.getItem() instanceof EncodedPatternItem) {
            boolean shiftHeld = Screen.hasShiftDown();
            var level = Minecraft.getInstance().level;
            if (shiftHeld && level != null) {
                var encodedPattern = (EncodedPatternItem) stack.getItem();
                var output = encodedPattern.getOutput(stack);
                if (!output.isEmpty()) {
                    var realModel = renderer.getItemModelShaper().getItemModel(output);
                    renderInstead(renderer, stack, x, y, realModel);
                    return true;
                }
            }
        } else if (stack.getItem() instanceof FluidDummyItem dummyItem) {
            var fluid = dummyItem.getFluidStack(stack);
            if (!fluid.isEmpty()) {
                FluidBlitter.create(fluid)
                        .dest(x, y, 16, 16)
                        .blit((int) (100.0 + renderer.blitOffset));
            }
            return true;
        }

        return false;
    }

    private static void renderInstead(ItemRenderer renderer, ItemStack stack, int x, int y, BakedModel realModel) {
        OVERRIDING_FOR.set(stack);
        try {
            renderer.renderGuiItem(stack, x, y, realModel);
        } finally {
            OVERRIDING_FOR.remove();
        }
    }

}
