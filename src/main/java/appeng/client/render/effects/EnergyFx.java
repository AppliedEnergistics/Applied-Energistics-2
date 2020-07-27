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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class EnergyFx extends SpriteBillboardParticle {

    private final int startBlkX;
    private final int startBlkY;
    private final int startBlkZ;

    public EnergyFx(ClientWorld world, final double par2, final double par4, final double par6,
            final SpriteProvider sprite) {
        super(world, par2, par4, par6);
        this.gravityStrength = 0;
        this.colorBlue = 1;
        this.colorGreen = 1;
        this.colorRed = 1;
        this.colorAlpha = 1.4f;
        this.scale = 3.5f;
        this.setSprite(sprite);

        this.startBlkX = MathHelper.floor(this.x);
        this.startBlkY = MathHelper.floor(this.y);
        this.startBlkZ = MathHelper.floor(this.z);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getSize(float tickDelta) {
        return 0.1f * this.scale;
    }

    @Override
    public void buildGeometry(VertexConsumer buffer, Camera camera, float partialTicks) {
        float x = (float) (this.prevPosX + (this.x - this.prevPosX) * partialTicks);
        float y = (float) (this.prevPosY + (this.y - this.prevPosY) * partialTicks);
        float z = (float) (this.prevPosZ + (this.z - this.prevPosZ) * partialTicks);

        final int blkX = MathHelper.floor(x);
        final int blkY = MathHelper.floor(y);
        final int blkZ = MathHelper.floor(z);

        if (blkX == this.startBlkX && blkY == this.startBlkY && blkZ == this.startBlkZ) {
            super.buildGeometry(buffer, camera, partialTicks);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.onGround = false;

        this.scale *= 0.89f;
        this.colorAlpha *= 0.89f;
    }

    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public void setVelocityZ(float velocityZ) {
        this.velocityZ = velocityZ;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<EnergyParticleData> {
        private final SpriteProvider spriteSet;

        public Factory(SpriteProvider spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(EnergyParticleData effect, ClientWorld world, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            EnergyFx result = new EnergyFx(world, x, y, z, spriteSet);
            result.setVelocityX((float) xSpeed);
            result.setVelocityY((float) ySpeed);
            result.setVelocityZ((float) zSpeed);
            if (effect.forItem) {
                result.x += -0.2 * effect.direction.xOffset;
                result.y += -0.2 * effect.direction.yOffset;
                result.z += -0.2 * effect.direction.zOffset;
                result.scale *= 0.8f;
            }
            return result;
        }
    }

}
