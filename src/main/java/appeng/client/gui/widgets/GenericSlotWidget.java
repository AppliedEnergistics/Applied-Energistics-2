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

package appeng.client.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

import appeng.api.client.AEStackRendering;
import appeng.api.client.AmountFormat;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.helpers.iface.GenericStackInv;
import appeng.util.Platform;

/**
 * Read-only display of a generic IAEStack
 */
public class GenericSlotWidget extends CustomSlotWidget {
    private final AbstractContainerScreen<?> screen;
    private final GenericStackInv inv;
    private final int slot;

    public GenericSlotWidget(AbstractContainerScreen<?> screen, GenericStackInv inv, int slot) {
        super(slot);
        this.screen = screen;
        this.inv = inv;
        this.slot = slot;
    }

    @Override
    public boolean canClick(Player player) {
        return false;
    }

    @Override
    public void drawContent(PoseStack poseStack, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        var stack = inv.getStack(slot);
        if (stack != null) {
            AEStackRendering.drawRepresentation(Minecraft.getInstance(), poseStack, getTooltipAreaX(),
                    getTooltipAreaY(), stack);

            // The font renderer uses this global stack, so we have to apply the current transform to it.
            var globalStack = RenderSystem.getModelViewStack();
            globalStack.pushPose();
            globalStack.mulPoseMatrix(poseStack.last().pose());
            RenderSystem.applyModelViewMatrix();

            var text = AEStackRendering.formatAmount(stack, AmountFormat.PREVIEW_LARGE_FONT);
            StackSizeRenderer.renderSizeLabel(mc.font, getTooltipAreaX(), getTooltipAreaY(), text,
                    true);

            globalStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

    }

    @Nonnull
    @Override
    public List<Component> getTooltipMessage() {
        var stack = inv.getStack(slot);
        if (stack != null) {
            var list = new ArrayList<Component>();
            list.add(AEStackRendering.getDisplayName(stack));
            list.add(new TextComponent(Platform.formatModName(AEStackRendering.getModid(stack))));
            return list;
        }
        return List.of();
    }
}
