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

package appeng.entity;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class RenderFloatingItem extends RenderEntityItem {

    public RenderFloatingItem(final RenderManager manager) {
        super(manager, Minecraft.getMinecraft().getRenderItem());
        this.shadowOpaque = 0.0F;
    }

    @Override
    public void doRender(final EntityItem entityItem, final double x, final double y, final double z, final float yaw, final float partialTick) {
        if (entityItem instanceof EntityFloatingItem) {
            final EntityFloatingItem efi = (EntityFloatingItem) entityItem;
            if (efi.getProgress() > 0.0) {
                GlStateManager.pushMatrix();

                if (!(efi.getItem().getItem() instanceof ItemBlock)) {
                    GlStateManager.translate(0, -0.3f, 0);
                } else {
                    GlStateManager.translate(0, -0.2f, 0);
                }

                super.doRender(efi, x, y, z, yaw, 0);
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public boolean shouldBob() {
        return false;
    }
}
