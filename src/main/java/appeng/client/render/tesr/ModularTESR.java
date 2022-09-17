/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.render.tesr;


import appeng.client.render.FacingToRotation;
import appeng.client.render.renderable.Renderable;
import appeng.tile.AEBaseTile;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class ModularTESR<T extends AEBaseTile> extends TileEntitySpecialRenderer<T> {

    private final Renderable[] renderables;

    public ModularTESR(Renderable... renderables) {
        this.renderables = renderables;
    }

    @Override
    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float p_render_10_) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5, 0.5, 0.5);
        FacingToRotation.get(te.getForward(), te.getUp()).glRotateCurrentMat();
        GlStateManager.translate(-0.5, -0.5, -0.5);
        for (Renderable renderable : this.renderables) {
            renderable.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
        }
        GlStateManager.popMatrix();
    }

    @Override
    public void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, float p_render_10_, BufferBuilder buffer) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5, 0.5, 0.5);
        FacingToRotation.get(te.getForward(), te.getUp()).glRotateCurrentMat();
        GlStateManager.translate(-0.5, -0.5, -0.5);
        for (Renderable renderable : this.renderables) {
            renderable.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);
        }
        GlStateManager.popMatrix();
    }

}