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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.crafting.pattern.EncodedPatternItem;

public final class GuiGraphicsHooks {
    // Prevents recursion in the hook below
    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    private GuiGraphicsHooks() {
    }

    /**
     * This hook will exchange the rendered item model for encoded patterns to the item being crafted by them if shift
     * is held.
     */
    public static boolean onRenderGuiItem(GuiGraphics guiGraphics, @Nullable LivingEntity livingEntity,
            @Nullable Level level, ItemStack stack, int x, int y, int seed, int z) {
        var minecraft = Minecraft.getInstance();

        if (stack.getItem() instanceof EncodedPatternItem encodedPattern) {
            if (OVERRIDING_FOR.get() == stack) {
                return false; // Don't allow recursive model replacements
            }

            boolean shiftHeld = Screen.hasShiftDown();
            if (shiftHeld && level != null) {
                var output = encodedPattern.getOutput(stack);
                // If output would be identical to stack, we'd infinitely loop
                if (!output.isEmpty() && output != stack) {
                    renderInstead(guiGraphics, livingEntity, level, output, x, y, seed, z);
                    return true;
                }
            }
        }

        var unwrapped = GenericStack.unwrapItemStack(stack);
        if (unwrapped != null) {
            AEKeyRendering.drawInGui(
                    minecraft,
                    guiGraphics,
                    x,
                    y, unwrapped.what());

            if (unwrapped.amount() > 0) {
                String amtText = unwrapped.what().formatAmount(unwrapped.amount(), AmountFormat.SLOT);
                StackSizeRenderer.renderSizeLabel(guiGraphics, minecraft.font, x, y, amtText, false);
            }

            return true;
        }

        return false;
    }

    private static void renderInstead(GuiGraphics guiGraphics, @Nullable LivingEntity livingEntity,
            @Nullable Level level, ItemStack stack, int x, int y, int seed, int z) {
        OVERRIDING_FOR.set(stack);
        try {
            guiGraphics.renderItem(livingEntity, level, stack, x, y, seed, z);
        } finally {
            OVERRIDING_FOR.remove();
        }
    }
}
