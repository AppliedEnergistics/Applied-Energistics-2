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
import java.util.Set;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
 */
public class OverlayManager {

    private final static OverlayManager INSTANCE = new OverlayManager();

    private final Map<BlockPos, OverlayRenderer> overlayHandlers = new HashMap<>();

    public static OverlayManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        MatrixStack matrixStack = event.getMatrixStack();

        matrixStack.push();

        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        for (OverlayRenderer handler : overlayHandlers.values()) {
            handler.render(matrixStack, buffer);
        }

        matrixStack.pop();
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
    }

    public OverlayRenderer showArea(Set<ChunkPos> area, int color, TileEntity areaShower, boolean depth) {
        if (areaShower == null) {
            return null;
        }
        removeHandlers(areaShower);
        OverlayRenderer handler = new OverlayRenderer(area, color, depth);
        overlayHandlers.put(
                new BlockPos(areaShower.getPos().getX(), areaShower.getPos().getY(), areaShower.getPos().getZ()),
                handler);
        return handler;
    }

    public OverlayRenderer showArea(Set<ChunkPos> area, int color, TileEntity areaShower) {
        return showArea(area, color, areaShower, true);
    }

    public boolean isShowing(TileEntity te) {
        return overlayHandlers.containsKey(new BlockPos(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
    }

    public void removeHandlers(TileEntity te) {
        overlayHandlers.remove(new BlockPos(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()));
    }
}
