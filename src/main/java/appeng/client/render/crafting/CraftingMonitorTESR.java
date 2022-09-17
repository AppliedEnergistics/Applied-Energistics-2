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

package appeng.client.render.crafting;


import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.TesrRenderHelper;
import appeng.tile.crafting.TileCraftingMonitorTile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * Renders the item currently being crafted
 */
@SideOnly(Side.CLIENT)
public class CraftingMonitorTESR extends TileEntitySpecialRenderer<TileCraftingMonitorTile> {

    @Override
    public void render(TileCraftingMonitorTile te, double x, double y, double z, float partialTicks, int destroyStage, float p_render_10_) {
        if (te == null) {
            return;
        }

        EnumFacing facing = te.getForward();

        IAEItemStack jobProgress = te.getJobProgress();
        if (jobProgress != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

            TesrRenderHelper.moveToFace(facing);
            TesrRenderHelper.rotateToFace(facing, (byte) 0);
            TesrRenderHelper.renderItem2dWithAmount(jobProgress, 0.7f, 0.1f);

            GlStateManager.popMatrix();
        }
    }
}
