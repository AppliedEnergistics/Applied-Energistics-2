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

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderFloatingItem extends ItemRenderer {

    public RenderFloatingItem(final EntityRendererManager manager) {
        super(manager, Minecraft.getInstance().getItemRenderer());
        this.shadowOpaque = 0.0F;
    }

    @Override
    public void render(ItemEntity entityIn, float entityYaw, float partialTicks, MatrixStack mStack,
            IRenderTypeBuffer buffers, int packedLightIn) {
        if (entityIn instanceof EntityFloatingItem) {
            final EntityFloatingItem efi = (EntityFloatingItem) entityIn;
            if (efi.getProgress() > 0.0) {
                mStack.push();

                if (!(efi.getItem().getItem() instanceof BlockItem)) {
                    mStack.translate(0, -0.3f, 0);
                } else {
                    mStack.translate(0, -0.2f, 0);
                }

                super.render(entityIn, entityYaw, 0, mStack, buffers, packedLightIn);
                mStack.pop();
            }
        }
    }

    @Override
    public boolean shouldBob() {
        return false;
    }
}
