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

package appeng.client.render.effects;


import appeng.api.util.AEPartLocation;
import appeng.client.render.textures.ParticleTextures;
import net.minecraft.client.particle.ParticleBreaking;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class EnergyFx extends ParticleBreaking {

    private final TextureAtlasSprite particleTextureIndex;

    private final int startBlkX;
    private final int startBlkY;
    private final int startBlkZ;

    public EnergyFx(final World par1World, final double par2, final double par4, final double par6, final Item par8Item) {
        super(par1World, par2, par4, par6, par8Item);
        this.particleGravity = 0;
        this.particleBlue = 1;
        this.particleGreen = 1;
        this.particleRed = 1;
        this.particleAlpha = 1.4f;
        this.particleScale = 3.5f;
        this.particleTextureIndex = ParticleTextures.BlockEnergyParticle;

        this.startBlkX = MathHelper.floor(this.posX);
        this.startBlkY = MathHelper.floor(this.posY);
        this.startBlkZ = MathHelper.floor(this.posZ);
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void renderParticle(final BufferBuilder par1Tessellator, final Entity p_180434_2_, final float partialTicks, final float par3, final float par4, final float par5, final float par6, final float par7) {
        final float f6 = this.particleTextureIndex.getMinU();
        final float f7 = this.particleTextureIndex.getMaxU();
        final float f8 = this.particleTextureIndex.getMinV();
        final float f9 = this.particleTextureIndex.getMaxV();
        final float f10 = 0.1F * this.particleScale;

        final float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        final float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        final float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);

        final int blkX = MathHelper.floor(this.posX);
        final int blkY = MathHelper.floor(this.posY);
        final int blkZ = MathHelper.floor(this.posZ);

        if (blkX == this.startBlkX && blkY == this.startBlkY && blkZ == this.startBlkZ) {
            int i = this.getBrightnessForRender(partialTicks);
            int j = i >> 16 & 65535;
            int k = i & 65535;

            final float f14 = 1.0F;
            par1Tessellator.pos(f11 - par3 * f10 - par6 * f10, f12 - par4 * f10, f13 - par5 * f10 - par7 * f10)
                    .tex(f7, f9)
                    .color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha)
                    .lightmap(j, k)
                    .endVertex();
            par1Tessellator.pos(f11 - par3 * f10 + par6 * f10, f12 + par4 * f10, f13 - par5 * f10 + par7 * f10)
                    .tex(f7, f8)
                    .color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha)
                    .lightmap(j, k)
                    .endVertex();
            par1Tessellator.pos(f11 + par3 * f10 + par6 * f10, f12 + par4 * f10, f13 + par5 * f10 + par7 * f10)
                    .tex(f6, f8)
                    .color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha)
                    .lightmap(j, k)
                    .endVertex();
            par1Tessellator.pos(f11 + par3 * f10 - par6 * f10, f12 - par4 * f10, f13 + par5 * f10 - par7 * f10)
                    .tex(f6, f9)
                    .color(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha)
                    .lightmap(j, k)
                    .endVertex();
        }
    }

    public void fromItem(final AEPartLocation d) {
        this.posX += 0.2 * d.xOffset;
        this.posY += 0.2 * d.yOffset;
        this.posZ += 0.2 * d.zOffset;
        this.particleScale *= 0.8f;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        this.motionY -= 0.04D * this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        this.particleScale *= 0.89f;
        this.particleAlpha *= 0.89f;
    }

    public void setMotionX(float motionX) {
        this.motionX = motionX;
    }

    public void setMotionY(float motionY) {
        this.motionY = motionY;
    }

    public void setMotionZ(float motionZ) {
        this.motionZ = motionZ;
    }
}
