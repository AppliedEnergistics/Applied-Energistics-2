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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

// Derived from Vanilla's BreakingParticle, but allows
// a texture to be specified directly rather than via an itemstack
@Environment(EnvType.CLIENT)
public class CableBusBreakingParticle extends TextureSheetParticle {

    private final float uCoord;
    private final float vCoord;

    public CableBusBreakingParticle(ClientLevel level, double x, double y, double z, double speedX, double speedY,
            double speedZ, TextureAtlasSprite sprite) {
        super(level, x, y, z, speedX, speedY, speedZ);
        this.setSprite(sprite);
        this.gravity = 1.0F;
        this.quadSize /= 2.0F;
        this.uCoord = this.random.nextFloat() * 3.0F;
        this.vCoord = this.random.nextFloat() * 3.0F;
    }

    public CableBusBreakingParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        this(level, x, y, z, 0, 0, 0, sprite);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uCoord + 1.0F) / 4.0F * 16.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uCoord / 4.0F * 16.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vCoord / 4.0F * 16.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vCoord + 1.0F) / 4.0F * 16.0F);
    }

}
