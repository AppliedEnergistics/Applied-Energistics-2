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

import java.util.*;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;

/**
 * Registry for {@link IAEStackRenderHandler}. Also contains convenience functions to render a stack without having to
 * query the render handler first.
 */
@Environment(EnvType.CLIENT)
@ThreadSafe
public class AEStackRendering {
    private static volatile Map<IStorageChannel<?>, IAEStackRenderHandler<?>> renderers = new IdentityHashMap<>();

    public static synchronized <T extends IAEStack> void register(IStorageChannel<T> channel,
            IAEStackRenderHandler<T> handler) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(handler, "handler");

        var renderersCopy = new IdentityHashMap<>(renderers);
        if (renderersCopy.put(channel, handler) != null) {
            throw new IllegalArgumentException("Duplicate registration of render handler for channel " + channel);
        }
        renderers = renderersCopy;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends IAEStack> IAEStackRenderHandler<T> get(IStorageChannel<T> channel) {
        return (IAEStackRenderHandler<T>) renderers.get(channel);
    }

    public static <T extends IAEStack> IAEStackRenderHandler<T> getOrThrow(IStorageChannel<T> channel) {
        var renderHandler = get(channel);

        if (renderHandler == null) {
            throw new IllegalArgumentException("Missing render handler for channel " + channel);
        }

        return renderHandler;
    }

    @SuppressWarnings("rawtypes")
    private static IAEStackRenderHandler getUnchecked(IAEStack stack) {
        return getOrThrow(stack.getChannel());
    }

    @SuppressWarnings("unchecked")
    public static void drawRepresentation(Minecraft minecraft, PoseStack poseStack, int x, int y, IAEStack stack) {
        getUnchecked(stack).drawRepresentation(minecraft, poseStack, x, y, stack);
    }

    @SuppressWarnings("unchecked")
    public static Component getDisplayName(IAEStack stack) {
        return getUnchecked(stack).getDisplayName(stack);
    }

    @SuppressWarnings("unchecked")
    public static List<Component> getTooltip(IAEStack stack) {
        // The array list is used to ensure mutability of the returned tooltip.
        return new ArrayList<Component>(getUnchecked(stack).getTooltip(stack));
    }

    public static String formatAmount(IAEStack stack, AmountFormat format) {
        return formatAmount(stack, stack.getStackSize(), format);
    }

    public static String formatAmount(IAEStack stack, long amount, AmountFormat format) {
        return getUnchecked(stack).formatAmount(amount, format);
    }

    @SuppressWarnings("unchecked")
    public static String getModid(IAEStack stack) {
        return getUnchecked(stack).getModid(stack);
    }
}
