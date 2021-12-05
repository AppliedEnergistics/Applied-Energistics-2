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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

/**
 * Registry for {@link IAEStackRenderHandler}. Also contains convenience functions to render a stack without having to
 * query the render handler first.
 */
@OnlyIn(Dist.CLIENT)
@ThreadSafe
public class AEStackRendering {
    private static volatile Map<AEKeyType, IAEStackRenderHandler<?>> renderers = new IdentityHashMap<>();

    public static synchronized <T extends AEKey> void register(AEKeyType channel,
            Class<T> keyClass,
            IAEStackRenderHandler<T> handler) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(handler, "handler");
        Objects.requireNonNull(keyClass, "keyClass");
        Preconditions.checkArgument(channel.getKeyClass() == keyClass, "%s != %s",
                channel.getKeyClass(), keyClass);

        var renderersCopy = new IdentityHashMap<>(renderers);
        if (renderersCopy.put(channel, handler) != null) {
            throw new IllegalArgumentException("Duplicate registration of render handler for channel " + channel);
        }
        renderers = renderersCopy;
    }

    @Nullable
    public static IAEStackRenderHandler<?> get(AEKeyType channel) {
        return renderers.get(channel);
    }

    public static IAEStackRenderHandler<?> getOrThrow(AEKeyType channel) {
        var renderHandler = get(channel);

        if (renderHandler == null) {
            throw new IllegalArgumentException("Missing render handler for channel " + channel);
        }

        return renderHandler;
    }

    @SuppressWarnings("rawtypes")
    private static IAEStackRenderHandler getUnchecked(AEKey stack) {
        return getOrThrow(stack.getType());
    }

    @SuppressWarnings("unchecked")
    public static void drawInGui(Minecraft minecraft, PoseStack poseStack, int x, int y, int z, AEKey what) {
        getUnchecked(what).drawInGui(minecraft, poseStack, x, y, z, what);
    }

    public static void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, AEKey what, float scale,
            int combinedLightIn) {
        getUnchecked(what).drawOnBlockFace(poseStack, buffers, what, scale, combinedLightIn);
    }

    @SuppressWarnings("unchecked")
    public static Component getDisplayName(AEKey stack) {
        return getUnchecked(stack).getDisplayName(stack);
    }

    @SuppressWarnings("unchecked")
    public static List<Component> getTooltip(AEKey stack) {
        // The array list is used to ensure mutability of the returned tooltip.
        return new ArrayList<Component>(getUnchecked(stack).getTooltip(stack));
    }

    public static String formatAmount(AEKey what, long amount, AmountFormat format) {
        return getUnchecked(what).formatAmount(amount, format);
    }

}
