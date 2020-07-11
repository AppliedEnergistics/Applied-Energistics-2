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

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnergyFx extends SpriteTexturedParticle {

    private final int startBlkX;
    private final int startBlkY;
    private final int startBlkZ;

    public EnergyFx(final World par1World, final double par2, final double par4, final double par6,
            final IAnimatedSprite sprite) {
        super(par1World, par2, par4, par6);
        this.particleGravity = 0;
        this.particleBlue = 1;
        this.particleGreen = 1;
        this.particleRed = 1;
        this.particleAlpha = 1.4f;
        this.particleScale = 3.5f;
        this.selectSpriteRandomly(sprite);

        this.startBlkX = MathHelper.floor(this.posX);
        this.startBlkY = MathHelper.floor(this.posY);
        this.startBlkZ = MathHelper.floor(this.posZ);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getScale(float scaleFactor) {
        return 0.1f * this.particleScale;
    }

    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks);
        float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks);
        float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks);

        final int blkX = MathHelper.floor(x);
        final int blkY = MathHelper.floor(y);
        final int blkZ = MathHelper.floor(z);

        if (blkX == this.startBlkX && blkY == this.startBlkY && blkZ == this.startBlkZ) {
            super.renderParticle(buffer, renderInfo, partialTicks);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.onGround = false;

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

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<EnergyParticleData> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle makeParticle(EnergyParticleData data, World worldIn, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            EnergyFx result = new EnergyFx(worldIn, x, y, z, spriteSet);
            result.setMotionX((float) xSpeed);
            result.setMotionY((float) ySpeed);
            result.setMotionZ((float) zSpeed);
            if (data.forItem) {
                result.posX += -0.2 * data.direction.xOffset;
                result.posY += -0.2 * data.direction.yOffset;
                result.posZ += -0.2 * data.direction.zOffset;
                result.particleScale *= 0.8f;
            }
            return result;
        }
    }

}
