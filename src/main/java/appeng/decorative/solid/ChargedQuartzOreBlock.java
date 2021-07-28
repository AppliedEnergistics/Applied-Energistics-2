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

package appeng.decorative.solid;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;

public class ChargedQuartzOreBlock extends QuartzOreBlock {
    public ChargedQuartzOreBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(final BlockState state, final Level w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        double xOff = r.nextFloat();
        double yOff = r.nextFloat();
        double zOff = r.nextFloat();

        switch (r.nextInt(6)) {
            case 0:
                xOff = -0.01;
                break;
            case 1:
                yOff = -0.01;
                break;
            case 2:
                xOff = -0.01;
                break;
            case 3:
                zOff = -0.01;
                break;
            case 4:
                xOff = 1.01;
                break;
            case 5:
                yOff = 1.01;
                break;
            case 6:
                zOff = 1.01;
                break;
        }

        if (AppEngClient.instance().shouldAddParticles(r)) {
            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.CHARGED_ORE, pos.getX() + xOff,
                    pos.getY() + yOff, pos.getZ() + zOff, 0.0f, 0.0f, 0.0f);
        }
    }
}
