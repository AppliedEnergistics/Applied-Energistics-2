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

import org.joml.Quaternionf;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import appeng.core.particles.EnergyParticleData;

public class EnergyFx extends SingleQuadParticle {

    private final int startBlkX;
    private final int startBlkY;
    private final int startBlkZ;

    public EnergyFx(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        this.gravity = 0;
        this.bCol = 1;
        this.gCol = 1;
        this.rCol = 1;
        this.alpha = 1.4f;
        this.quadSize = 3.5f;

        this.startBlkX = Mth.floor(this.x);
        this.startBlkY = Mth.floor(this.y);
        this.startBlkZ = Mth.floor(this.z);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return 0.1f * Mth.lerp(scaleFactor, this.quadSize, this.quadSize * 0.89f);
    }

    @Override
    protected void extractRotatedQuad(
            QuadParticleRenderState reusedState, Quaternionf orientation, float x, float y, float z,
            float partialTick) {
        var blockX = Math.floor(Mth.lerp(partialTick, xo, this.x));
        var blockY = Math.floor(Mth.lerp(partialTick, yo, this.y));
        var blockZ = Math.floor(Mth.lerp(partialTick, zo, this.z));
        if (blockX != this.startBlkX || blockY != this.startBlkY || blockZ != this.startBlkZ) {
            return;
        }

        var alpha = Mth.lerp(partialTick, this.alpha, this.alpha * 0.89f);

        reusedState.add(
                this.getLayer(),
                x,
                y,
                z,
                orientation.x,
                orientation.y,
                orientation.z,
                orientation.w,
                this.getQuadSize(partialTick),
                this.getU0(),
                this.getU1(),
                this.getV0(),
                this.getV1(),
                ARGB.colorFromFloat(alpha, this.rCol, this.gCol, this.bCol),
                this.getLightColor(partialTick));
    }

    @Override
    public void tick() {
        super.tick();
        this.onGround = false;

        this.quadSize *= 0.89f;
        this.alpha *= 0.89f;
    }

    public void setMotionX(float motionX) {
        this.xd = motionX;
    }

    public void setMotionY(float motionY) {
        this.yd = motionY;
    }

    public void setMotionZ(float motionZ) {
        this.zd = motionZ;
    }

    public static class Factory implements ParticleProvider<EnergyParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(EnergyParticleData data, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            EnergyFx result = new EnergyFx(level, x, y, z, spriteSet.get(random));
            result.setMotionX((float) xSpeed);
            result.setMotionY((float) ySpeed);
            result.setMotionZ((float) zSpeed);
            if (data.forItem()) {
                result.x += -0.2 * data.direction().getStepX();
                result.y += -0.2 * data.direction().getStepY();
                result.z += -0.2 * data.direction().getStepZ();
                result.quadSize *= 0.8f;
            }
            return result;
        }
    }

}
