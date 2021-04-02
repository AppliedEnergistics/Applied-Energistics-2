/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.client.render.cablebus;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;

// Derived from Vanilla's BreakingParticle, but allows
// a texture to be specified directly rather than via an itemstack
@Environment(EnvType.CLIENT)
public class CableBusBreakingParticle extends SpriteTexturedParticle {

    private final float uCoord;
    private final float vCoord;

    public CableBusBreakingParticle(ClientWorld world, double x, double y, double z, double speedX, double speedY,
            double speedZ, TextureAtlasSprite sprite) {
        super(world, x, y, z, speedX, speedY, speedZ);
        this.setSprite(sprite);
        this.particleGravity = 1.0F;
        this.particleScale /= 2.0F;
        this.uCoord = this.rand.nextFloat() * 3.0F;
        this.vCoord = this.rand.nextFloat() * 3.0F;
    }

    public CableBusBreakingParticle(ClientWorld world, double x, double y, double z, TextureAtlasSprite sprite) {
        this(world, x, y, z, 0, 0, 0, sprite);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getMinU() {
        return this.sprite.getInterpolatedU((this.uCoord + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxU() {
        return this.sprite.getInterpolatedU(this.uCoord / 4.0F * 16.0F);
    }

    @Override
    protected float getMinV() {
        return this.sprite.getInterpolatedV(this.vCoord / 4.0F * 16.0F);
    }

    @Override
    protected float getMaxV() {
        return this.sprite.getInterpolatedV((this.vCoord + 1.0F) / 4.0F * 16.0F);
    }

}
