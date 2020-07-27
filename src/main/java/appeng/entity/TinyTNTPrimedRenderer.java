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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.TntMinecartEntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TinyTNTPrimedRenderer extends EntityRenderer<TinyTNTPrimedEntity> {

    public TinyTNTPrimedRenderer(final EntityRenderDispatcher manager) {
        super(manager);
        this.shadowRadius = 0.25F;
    }

    @Override
    public void render(TinyTNTPrimedEntity tnt, float entityYaw, float partialTicks, MatrixStack mStack,
            VertexConsumerProvider buffers, int packedLight) {
        mStack.push();
        mStack.translate(0, 0.25F, 0);
        float f2;

        if (tnt.getFuse() - partialTicks + 1.0F < 10.0F) {
            f2 = 1.0F - (tnt.getFuse() - partialTicks + 1.0F) / 10.0F;

            if (f2 < 0.0F) {
                f2 = 0.0F;
            }

            if (f2 > 1.0F) {
                f2 = 1.0F;
            }

            f2 *= f2;
            f2 *= f2;
            final float f3 = 1.0F + f2 * 0.3F;
            mStack.scale(f3, f3, f3);
        }

        mStack.scale(0.5f, 0.5f, 0.5f);
        mStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90.0F));
        mStack.translate(-0.5D, -0.5D, 0.5D);
        mStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0F));
        TntMinecartEntityRenderer.method_23190(Blocks.TNT.getDefaultState(), mStack, buffers, packedLight,
                tnt.getFuse() / 5 % 2 == 0);
        mStack.pop();
        super.render(tnt, entityYaw, partialTicks, mStack, buffers, packedLight);
    }

    @Override
    public Identifier getTexture(final TinyTNTPrimedEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEX;
    }
}
