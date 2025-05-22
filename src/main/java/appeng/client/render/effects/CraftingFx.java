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

import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CraftingFx extends TextureSheetParticle {

    // Offset relative to center of block, is the starting point of the particle
    // movement
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public CraftingFx(ClientLevel level, double x, double y, double z,
            SpriteSet sprite) {
        super(level, x, y, z);

        // Pick a random normal, offset it by 0.35 and use that as the particle origin
        Vector3f off = new Vector3f(random.nextFloat() - 0.5f, random.nextFloat() - 0.5f, random.nextFloat() - 0.5f);
        off.normalize();
        off.mul(0.35f);
        offsetX = off.x();
        offsetY = off.y();
        offsetZ = off.z();

        this.gravity = 0;
        this.bCol = 1;
        this.gCol = 0.9f;
        this.rCol = 1;
        this.pickSprite(sprite);
        this.lifetime /= 1.2;
        this.hasPhysics = false; // we're INSIDE the block anyway
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {

        float f = (this.age + partialTicks) / this.lifetime;

        float offX = (float) x + Mth.lerp(f, offsetX, 0);
        float offY = (float) y + Mth.lerp(f, offsetY, 0);
        float offZ = (float) z + Mth.lerp(f, offsetZ, 0);
        float alpha = Mth.lerp(easeOutCirc(f), 1.3f, 0.1f);
        float scale = Mth.lerp(easeOutCirc(f), 0.13f, 0.0f);

        // I believe this particle is same as breaking particle, but should not exit the
        // original block it was
        // spawned in (which is encased in glass)
        Vec3 Vector3d = renderInfo.getPosition();
        offX -= Vector3d.x;
        offY -= Vector3d.y;
        offZ -= Vector3d.z;

        Vector3f[] avector3f = new Vector3f[] { new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F) };

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(renderInfo.rotation());
            vector3f.mul(scale);
            vector3f.add(offX, offY, offZ);
        }

        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int j = 15728880; // full brightness
        buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(maxU, maxV)
                .color(this.rCol, this.gCol, this.bCol, alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(maxU, minV)
                .color(this.rCol, this.gCol, this.bCol, alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(minU, minV)
                .color(this.rCol, this.gCol, this.bCol, alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(minU, maxV)
                .color(this.rCol, this.gCol, this.bCol, alpha).uv2(j).endVertex();
    }

    // https://easings.net/#easeOutCirc
    private static float easeOutCirc(float x) {
        return (float) Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType data, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new CraftingFx(level, x, y, z, spriteSet);
        }
    }

}
