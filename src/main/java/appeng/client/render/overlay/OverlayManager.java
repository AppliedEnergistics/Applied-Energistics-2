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

package appeng.client.render.overlay;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.phys.Vec3;

import appeng.api.util.DimensionalBlockPos;

/**
 * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
 */
public class OverlayManager {

    private final static OverlayManager INSTANCE = new OverlayManager();

    private final Map<DimensionalBlockPos, OverlayRenderer> overlayHandlers = new HashMap<>();

    public static OverlayManager getInstance() {
        return INSTANCE;
    }

    private OverlayManager() {
    }

    public void renderWorldLastEvent(WorldRenderContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        BufferSource buffer = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = context.matrixStack();

        poseStack.pushPose();

        Vec3 projectedView = minecraft.gameRenderer.getMainCamera().getPosition();
        poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        for (OverlayRenderer handler : overlayHandlers.entrySet().stream()
                .filter(e -> e.getKey().getLevel() == minecraft.level).map(Entry::getValue)
                .collect(Collectors.toList())) {
            handler.render(poseStack, buffer);
        }

        poseStack.popPose();
    }

    public OverlayRenderer showArea(IOverlayDataSource source) {
        Objects.requireNonNull(source);

        OverlayRenderer handler = new OverlayRenderer(source);
        overlayHandlers.put(source.getOverlaySourceLocation(), handler);
        return handler;
    }

    public boolean isShowing(IOverlayDataSource source) {
        return overlayHandlers.containsKey(source.getOverlaySourceLocation());
    }

    public void removeHandlers(IOverlayDataSource source) {
        overlayHandlers.remove(source.getOverlaySourceLocation());
    }
}
