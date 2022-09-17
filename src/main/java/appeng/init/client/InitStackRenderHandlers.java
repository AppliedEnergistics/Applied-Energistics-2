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
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import appeng.api.client.AEStackRendering;
import appeng.api.client.IAEStackRenderHandler;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.client.gui.style.FluidBlitter;
import appeng.util.Platform;

public class InitStackRenderHandlers {
    private InitStackRenderHandlers() {
    }

    public static void init() {
        AEStackRendering.register(AEKeyType.items(), AEItemKey.class, new ItemKeyRenderHandler());
        AEStackRendering.register(AEKeyType.fluids(), AEFluidKey.class, new FluidKeyRenderHandler());
    }

    private static class ItemKeyRenderHandler implements IAEStackRenderHandler<AEItemKey> {
        @Override
        public void drawInGui(Minecraft minecraft, PoseStack poseStack, int x, int y, int zIndex,
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
        public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, AEItemKey what, float scale,
                int combinedLight) {
            poseStack.pushPose();
            // Push it out of the block face a bit to avoid z-fighting
            poseStack.translate(0, 0, 0.01f);
            // The Z-scaling by 0.001 causes the model to be visually "flattened"
            // This cannot replace a proper projection, but it's cheap and gives the desired effect.
            // We don't scale the normal matrix to avoid lighting issues.
            poseStack.mulPoseMatrix(Matrix4f.createScaleMatrix(scale, scale, 0.001f));
            // Rotate the normal matrix a little for nicer lighting.
            poseStack.last().normal().mul(Vector3f.XN.rotationDegrees(45f));

            Minecraft.getInstance().getItemRenderer().renderStatic(what.toStack(), ItemTransforms.TransformType.GUI,
                    combinedLight, OverlayTexture.NO_OVERLAY, poseStack, buffers, 0);

            poseStack.popPose();
        }

        @Override
        public Component getDisplayName(AEItemKey stack) {
            return stack.toStack().getHoverName();
        }

        @Override
        public List<Component> getTooltip(AEItemKey stack) {
            return stack.toStack().getTooltipLines(Minecraft.getInstance().player,
                    Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED
                            : TooltipFlag.Default.NORMAL);
        }
    }

    private static class FluidKeyRenderHandler implements IAEStackRenderHandler<AEFluidKey> {
        @Override
        public void drawInGui(Minecraft minecraft, PoseStack poseStack, int x, int y, int zIndex,
                AEFluidKey what) {
            FluidBlitter.create(what)
                    .dest(x, y, 16, 16)
                    .blit(poseStack, 100 + zIndex);
        }

        @Override
        public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, AEFluidKey what, float scale,
                int combinedLight) {
            var variant = what.toVariant();
            var color = FluidVariantRendering.getColor(variant);
            var sprite = FluidVariantRendering.getSprite(variant);

            if (sprite == null) {
                return;
            }

            poseStack.pushPose();
            // Push it out of the block face a bit to avoid z-fighting
            poseStack.translate(0, 0, 0.01f);

            var buffer = buffers.getBuffer(RenderType.solid());

            // In comparison to items, make it _slightly_ smaller because item icons
            // usually don't extend to the full size.
            scale -= 0.05f;

            // y is flipped here
            var x0 = -scale / 2;
            var y0 = scale / 2;
            var x1 = scale / 2;
            var y1 = -scale / 2;

            var transform = poseStack.last().pose();
            buffer.vertex(transform, x0, y1, 0)
                    .color(color)
                    .uv(sprite.getU0(), sprite.getV1())
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(combinedLight)
                    .normal(0, 0, 1)
                    .endVertex();
            buffer.vertex(transform, x1, y1, 0)
                    .color(color)
                    .uv(sprite.getU1(), sprite.getV1())
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(combinedLight)
                    .normal(0, 0, 1)
                    .endVertex();
            buffer.vertex(transform, x1, y0, 0)
                    .color(color)
                    .uv(sprite.getU1(), sprite.getV0())
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(combinedLight)
                    .normal(0, 0, 1)
                    .endVertex();
            buffer.vertex(transform, x0, y0, 0)
                    .color(color)
                    .uv(sprite.getU0(), sprite.getV0())
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(combinedLight)
                    .normal(0, 0, 1)
                    .endVertex();
            poseStack.popPose();
        }

        @Override
        public Component getDisplayName(AEFluidKey stack) {
            return FluidVariantAttributes.getName(stack.toVariant());
        }

        @Override
        public List<Component> getTooltip(AEFluidKey stack) {
            var tooltip = FluidVariantRendering.getTooltip(stack.toVariant());

            // Heuristic: If the last line doesn't include the modname, add it ourselves
            var modName = Platform.formatModName(stack.getModId());
            if (tooltip.isEmpty() || !tooltip.get(tooltip.size() - 1).getString().equals(modName)) {
                tooltip.add(Component.literal(modName));
            }

            return tooltip;
        }
    }
}
