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

import java.util.Random;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LightningFX extends SpriteTexturedParticle {

    private static final Random RANDOM_GENERATOR = new Random();
    private static final int STEPS = 5;
    private static final int BRIGHTNESS = 13 << 4;

    private final double[][] precomputedSteps;
    private final double[] vertices = new double[3];
    private final double[] verticesWithUV = new double[3];
    private boolean hasData = false;

    private LightningFX(final ClientWorld w, final double x, final double y, final double z, final double r,
            final double g, final double b) {
        this(w, x, y, z, r, g, b, 6);
        this.regen();
    }

    protected LightningFX(final ClientWorld w, final double x, final double y, final double z, final double r,
            final double g, final double b, final int maxAge) {
        super(w, x, y, z, r, g, b);
        this.precomputedSteps = new double[LightningFX.STEPS][3];
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.maxAge = maxAge;
    }

    protected void regen() {
        double lastDirectionX = (RANDOM_GENERATOR.nextDouble() - 0.5) * 0.9;
        double lastDirectionY = (RANDOM_GENERATOR.nextDouble() - 0.5) * 0.9;
        double lastDirectionZ = (RANDOM_GENERATOR.nextDouble() - 0.5) * 0.9;
        for (int s = 0; s < LightningFX.STEPS; s++) {
            this.precomputedSteps[s][0] = lastDirectionX = (lastDirectionX
                    + (RANDOM_GENERATOR.nextDouble() - 0.5) * 0.9) / 2.0;
            this.precomputedSteps[s][1] = lastDirectionY = (lastDirectionY
                    + (RANDOM_GENERATOR.nextDouble() - 0.5) * 0.9) / 2.0;
            this.precomputedSteps[s][2] = lastDirectionZ = (lastDirectionZ
                    + (RANDOM_GENERATOR.nextDouble() - 0.5) * 0.9) / 2.0;
        }
    }

    protected int getSteps() {
        return LightningFX.STEPS;
    }

    @Override
    public IParticleRenderType getRenderType() {
        // TODO: FIXME
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.age++ >= this.maxAge) {
            this.setExpired();
        }

        this.motionY -= 0.04D * this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
    }

    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        Vector3d Vector3d = renderInfo.getProjectedView();
        float centerX = (float) (MathHelper.lerp(partialTicks, this.prevPosX, this.posX) - Vector3d.getX());
        float centerY = (float) (MathHelper.lerp(partialTicks, this.prevPosY, this.posY) - Vector3d.getY());
        float centerZ = (float) (MathHelper.lerp(partialTicks, this.prevPosZ, this.posZ) - Vector3d.getZ());

        final float j = 1.0f;
        float red = this.particleRed * j * 0.9f;
        float green = this.particleGreen * j * 0.95f;
        float blue = this.particleBlue * j;
        final float alpha = this.particleAlpha;

        if (this.age == 3) {
            this.regen();
        }

        float u = this.getMinU() + (this.getMaxU() - this.getMinU()) / 2;
        float v = this.getMinV() + (this.getMaxV() - this.getMinV()) / 2;

        double scale = 0.02;// 0.02F * this.particleScale;

        final double[] a = new double[3];
        final double[] b = new double[3];

        double ox = 0;
        double oy = 0;
        double oz = 0;

        final PlayerEntity p = Minecraft.getInstance().player;

        // FIXME: Billboard rotation is not applied to the particle yet,
        // FIXME The old version apparently did this by hand using rX,rZ -> replicate
        // using the quaternion

        for (int layer = 0; layer < 2; layer++) {
            if (layer == 0) {
                scale = 0.04;
//				FIXME offX *= 0.001;
//				FIXME offY *= 0.001;
//				FIXME offZ *= 0.001;
                red = this.particleRed * j * 0.4f;
                green = this.particleGreen * j * 0.25f;
                blue = this.particleBlue * j * 0.45f;
            } else {
//				FIXME offX = 0;
//				FIXME offY = 0;
//				FIXME offZ = 0;
                scale = 0.02;
                red = this.particleRed * j * 0.9f;
                green = this.particleGreen * j * 0.65f;
                blue = this.particleBlue * j * 0.85f;
            }

            for (int cycle = 0; cycle < 3; cycle++) {
                this.clear();

                // FIXME removed interpPos here, check if this is correct
                double x = centerX; // FIXME - offX;
                double y = centerY; // FIXME - offY;
                double z = centerZ; // FIXME - offZ;

                for (int s = 0; s < LightningFX.STEPS; s++) {
                    final double xN = x + this.precomputedSteps[s][0];
                    final double yN = y + this.precomputedSteps[s][1];
                    final double zN = z + this.precomputedSteps[s][2];

                    final double xD = xN - x;
                    final double yD = yN - y;
                    final double zD = zN - z;

                    if (cycle == 0) {
                        ox = (yD * 0) - (1 * zD);
                        oy = (zD * 0) - (0 * xD);
                        oz = (xD * 1) - (0 * yD);
                    }
                    if (cycle == 1) {
                        ox = (yD * 1) - (0 * zD);
                        oy = (zD * 0) - (1 * xD);
                        oz = (xD * 0) - (0 * yD);
                    }
                    if (cycle == 2) {
                        ox = (yD * 0) - (0 * zD);
                        oy = (zD * 1) - (0 * xD);
                        oz = (xD * 0) - (1 * yD);
                    }

                    final double ss = Math.sqrt(ox * ox + oy * oy + oz * oz)
                            / ((((double) LightningFX.STEPS - (double) s) / LightningFX.STEPS) * scale);
                    ox /= ss;
                    oy /= ss;
                    oz /= ss;

                    a[0] = x + ox;
                    a[1] = y + oy;
                    a[2] = z + oz;

                    b[0] = x;
                    b[1] = y;
                    b[2] = z;

                    this.draw(red, green, blue, buffer, a, b, u, v);

                    x = xN;
                    y = yN;
                    z = zN;
                }
            }
        }
    }

    private void clear() {
        this.hasData = false;
    }

    private void draw(float red, float green, float blue, final IVertexBuilder tess, final double[] a, final double[] b,
            final float u, final float v) {
        if (this.hasData) {
            tess.pos(a[0], a[1], a[2]).tex(u, v).color(red, green, blue, this.particleAlpha)
                    .lightmap(BRIGHTNESS, BRIGHTNESS).endVertex();
            tess.pos(this.vertices[0], this.vertices[1], this.vertices[2]).tex(u, v)
                    .color(red, green, blue, this.particleAlpha).lightmap(BRIGHTNESS, BRIGHTNESS).endVertex();
            tess.pos(this.verticesWithUV[0], this.verticesWithUV[1], this.verticesWithUV[2]).tex(u, v)
                    .color(red, green, blue, this.particleAlpha).lightmap(BRIGHTNESS, BRIGHTNESS).endVertex();
            tess.pos(b[0], b[1], b[2]).tex(u, v).color(red, green, blue, this.particleAlpha)
                    .lightmap(BRIGHTNESS, BRIGHTNESS).endVertex();
        }
        this.hasData = true;
        for (int x = 0; x < 3; x++) {
            this.vertices[x] = a[x];
            this.verticesWithUV[x] = b[x];
        }
    }

    protected double[][] getPrecomputedSteps() {
        return this.precomputedSteps;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            LightningFX lightningFX = new LightningFX(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            lightningFX.selectSpriteRandomly(this.spriteSet);
            return lightningFX;
        }
    }

}
