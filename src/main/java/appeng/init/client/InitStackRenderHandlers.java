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

package appeng.init.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import appeng.api.client.AEStackRendering;
import appeng.api.client.AmountFormat;
import appeng.api.client.IAEStackRenderHandler;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.client.gui.me.common.FluidStackSizeRenderer;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.style.FluidBlitter;
import appeng.util.Platform;

public class InitStackRenderHandlers {
    private InitStackRenderHandlers() {
    }

    public static void init() {
        var itemSSRenderer = new StackSizeRenderer();
        AEStackRendering.register(AEKeyType.items(), AEItemKey.class, new IAEStackRenderHandler<>() {
            @Override
            public void drawRepresentation(Minecraft minecraft, PoseStack poseStack, int x, int y, int zIndex,
                    AEItemKey stack) {
                ItemStack displayStack = stack.toStack();
                // The item renderer uses this global stack, so we have to apply the current transform to it.
                var globalStack = RenderSystem.getModelViewStack();
                globalStack.pushPose();
                globalStack.mulPoseMatrix(poseStack.last().pose());
                ItemRenderer itemRenderer = minecraft.getItemRenderer();
                var oldBlitOffset = itemRenderer.blitOffset;
                itemRenderer.blitOffset = zIndex;
                itemRenderer.renderGuiItem(displayStack, x, y);
                itemRenderer.renderGuiItemDecorations(minecraft.font, displayStack, x, y, "");
                itemRenderer.blitOffset = oldBlitOffset;
                globalStack.popPose();
                // Ensure the global state is correctly reset.
                RenderSystem.applyModelViewMatrix();
            }

            @Override
            public Component getDisplayName(AEItemKey stack) {
                return stack.toStack().getHoverName();
            }

            @Override
            public List<Component> getTooltip(AEItemKey stack) {
                return stack.toStack().getTooltipLines(null,
                        Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED
                                : TooltipFlag.Default.NORMAL);
            }

            @Override
            public String formatAmount(long amount, AmountFormat format) {
                return switch (format) {
                    case FULL -> String.valueOf(amount);
                    case PREVIEW_REGULAR -> itemSSRenderer.getToBeRenderedStackSize(amount, false);
                    case PREVIEW_LARGE_FONT -> itemSSRenderer.getToBeRenderedStackSize(amount, true);
                };
            }
        });
        var fluidSSRenderer = new FluidStackSizeRenderer();
        AEStackRendering.register(AEKeyType.fluids(), AEFluidKey.class, new IAEStackRenderHandler<>() {
            @Override
            public void drawRepresentation(Minecraft minecraft, PoseStack poseStack, int x, int y, int zIndex,
                    AEFluidKey stack) {
                FluidBlitter.create(stack)
                        .dest(x, y, 16, 16)
                        .blit(poseStack, 100 + zIndex);
            }

            @Override
            public Component getDisplayName(AEFluidKey stack) {
                return stack.getDisplayName();
            }

            @Override
            public String formatAmount(long amount, AmountFormat format) {
                return switch (format) {
                    case FULL -> Platform.formatFluidAmount(amount);
                    case PREVIEW_REGULAR -> fluidSSRenderer.getToBeRenderedStackSize(amount, false);
                    case PREVIEW_LARGE_FONT -> fluidSSRenderer.getToBeRenderedStackSize(amount, true);
                };
            }
        });
    }
}
