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
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.api.util.DimensionalCoord;

/**
 * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
 */
public class OverlayManager {

    private final static OverlayManager INSTANCE = new OverlayManager();

    private final Map<DimensionalCoord, OverlayRenderer> overlayHandlers = new HashMap<>();

    public static OverlayManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        IRenderTypeBuffer.Impl buffer = minecraft.getRenderTypeBuffers().getBufferSource();
        MatrixStack matrixStack = event.getMatrixStack();

        matrixStack.push();

        Vector3d projectedView = minecraft.gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        for (OverlayRenderer handler : overlayHandlers.entrySet().stream()
                .filter(e -> e.getKey().getWorld() == minecraft.world).map(Entry::getValue)
                .collect(Collectors.toList())) {
            handler.render(matrixStack, buffer);
        }

        matrixStack.pop();
    }

    public OverlayRenderer showArea(IOverlayDataSource source) {
        Preconditions.checkNotNull(source);

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
