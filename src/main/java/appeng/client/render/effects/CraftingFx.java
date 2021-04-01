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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

@Environment(EnvType.CLIENT)
public class CraftingFx extends SpriteTexturedParticle {

    // Offset relative to center of block, is the starting point of the particle
    // movement
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public CraftingFx(ClientWorld world, final double x, final double y, final double z, final IAnimatedSprite sprite) {
        super(world, x, y, z);

        // Pick a random normal, offset it by 0.35 and use that as the particle origin
        Vector3f off = new Vector3f(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
        off.normalize();
        off.mul(0.35f);
        offsetX = off.getX();
        offsetY = off.getY();
        offsetZ = off.getZ();

        this.particleGravity = 0;
        this.particleBlue = 1;
        this.particleGreen = 0.9f;
        this.particleRed = 1;
        this.selectSpriteRandomly(sprite);
        this.maxAge /= 1.2;
        this.canCollide = false; // we're INSIDE the block anyway
    }

    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo camera, float partialTicks) {

        float f = (this.age + partialTicks) / this.maxAge;

        float offX = (float) posX + MathHelper.lerp(f, offsetX, 0);
        float offY = (float) posY + MathHelper.lerp(f, offsetY, 0);
        float offZ = (float) posZ + MathHelper.lerp(f, offsetZ, 0);
        float alpha = MathHelper.lerp(easeOutCirc(f), 1.3f, 0.1f);
        float scale = MathHelper.lerp(easeOutCirc(f), 0.13f, 0.0f);

        // I believe this particle is same as breaking particle, but should not exit the
        // original block it was
        // spawned in (which is encased in glass)
        Vector3d vec3d = camera.getProjectedView();
        offX -= vec3d.x;
        offY -= vec3d.y;
        offZ -= vec3d.z;

        Vector3f[] avector3f = new Vector3f[] { new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F) };

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.transform(camera.getRotation());
            vector3f.mul(scale);
            vector3f.add(offX, offY, offZ);
        }

        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();
        int j = 15728880; // full brightness
        buffer.pos(avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ()).tex(maxU, maxV)
                .color(this.particleRed, this.particleGreen, this.particleBlue, alpha).lightmap(j).endVertex();
        buffer.pos(avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ()).tex(maxU, minV)
                .color(this.particleRed, this.particleGreen, this.particleBlue, alpha).lightmap(j).endVertex();
        buffer.pos(avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ()).tex(minU, minV)
                .color(this.particleRed, this.particleGreen, this.particleBlue, alpha).lightmap(j).endVertex();
        buffer.pos(avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ()).tex(minU, maxV)
                .color(this.particleRed, this.particleGreen, this.particleBlue, alpha).lightmap(j).endVertex();
    }

    // https://easings.net/#easeOutCirc
    private static float easeOutCirc(float x) {
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite p_i50477_1_) {
            this.spriteSet = p_i50477_1_;
        }

        @Override
        public Particle createParticle(BasicParticleType effect, ClientWorld world, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new CraftingFx(world, x, y, z, spriteSet);
        }
    }

}
