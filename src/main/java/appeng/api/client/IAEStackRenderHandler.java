/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEKey;
import appeng.util.Platform;

/**
 * Client-side rendering of AE stacks. Must be registered in {@link AEStackRendering} for each storage channel!
 */
@OnlyIn(Dist.CLIENT)
public interface IAEStackRenderHandler<T extends AEKey> {
    /**
     * Draw the stack, for example the item or the fluid sprite, but not the amount.
     */
    void drawRepresentation(Minecraft minecraft, PoseStack poseStack, int x, int y, int zIndex, T stack);

    /**
     * Name of the stack, ignoring the amount.
     */
    Component getDisplayName(T stack);

    /**
     * Return the full tooltip, with the name of the stack and any additional lines.
     */
    default List<Component> getTooltip(T stack) {
        return List.of(
                getDisplayName(stack),
                // Append the name of the mod by default as mods such as REI would also add that
                new TextComponent(Platform.formatModName(stack.getModId())));
    }

    /**
     * Format the amount into a user-readable string. See {@link AmountFormat} for an explanation of the different
     * formats.
     */
    String formatAmount(long amount, AmountFormat format);
}
