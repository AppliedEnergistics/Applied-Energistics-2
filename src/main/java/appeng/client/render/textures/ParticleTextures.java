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

package appeng.client.render.textures;


import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;


public class ParticleTextures {
    public static TextureAtlasSprite BlockEnergyParticle;
    public static TextureAtlasSprite BlockMatterCannonParticle;

    public static void registerSprite(TextureStitchEvent.Pre event) {
        BlockEnergyParticle = event.getMap().registerSprite(new ResourceLocation("appliedenergistics2:particles/energy"));
        BlockMatterCannonParticle = event.getMap().registerSprite(new ResourceLocation("appliedenergistics2:particles/matter_cannon"));
    }
}
