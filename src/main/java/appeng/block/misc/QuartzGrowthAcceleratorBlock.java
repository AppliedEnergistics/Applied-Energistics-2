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

package appeng.block.misc;

import java.util.Random;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.tile.misc.QuartzGrowthAcceleratorBlockEntity;
import appeng.util.Platform;

public class QuartzGrowthAcceleratorBlock extends AEBaseTileBlock<QuartzGrowthAcceleratorBlockEntity>
        implements IOrientableBlock {

    private static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public QuartzGrowthAcceleratorBlock() {
        super(defaultProps(Material.ROCK).sound(SoundType.METAL));
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState,
            QuartzGrowthAcceleratorBlockEntity te) {
        return currentState.with(POWERED, te.isPowered());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(POWERED);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void animateTick(final BlockState state, final World w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        final QuartzGrowthAcceleratorBlockEntity cga = this.getBlockEntity(w, pos);

        if (cga != null && cga.isPowered() && AppEng.instance().shouldAddParticles(r)) {
            final double d0 = r.nextFloat() - 0.5F;
            final double d1 = r.nextFloat() - 0.5F;

            final Direction up = cga.getUp();
            final Direction forward = cga.getForward();
            final Direction west = Platform.crossProduct(forward, up);

            double rx = 0.5 + pos.getX();
            double ry = 0.5 + pos.getY();
            double rz = 0.5 + pos.getZ();

            rx += up.getXOffset() * d0;
            ry += up.getYOffset() * d0;
            rz += up.getZOffset() * d0;

            final int x = pos.getX();
            final int y = pos.getY();
            final int z = pos.getZ();

            double dz = 0;
            double dx = 0;
            BlockPos pt = null;

            switch (r.nextInt(4)) {
                case 0:
                    dx = 0.6;
                    dz = d1;
                    pt = new BlockPos(x + west.getXOffset(), y + west.getYOffset(), z + west.getZOffset());

                    break;
                case 1:
                    dx = d1;
                    dz += 0.6;
                    pt = new BlockPos(x + forward.getXOffset(), y + forward.getYOffset(), z + forward.getZOffset());

                    break;
                case 2:
                    dx = d1;
                    dz = -0.6;
                    pt = new BlockPos(x - forward.getXOffset(), y - forward.getYOffset(), z - forward.getZOffset());

                    break;
                case 3:
                    dx = -0.6;
                    dz = d1;
                    pt = new BlockPos(x - west.getXOffset(), y - west.getYOffset(), z - west.getZOffset());

                    break;
            }

            if (!w.getBlockState(pt).isAir()) {
                return;
            }

            rx += dx * west.getXOffset();
            ry += dx * west.getYOffset();
            rz += dx * west.getZOffset();

            rx += dz * forward.getXOffset();
            ry += dz * forward.getYOffset();
            rz += dz * forward.getZOffset();

            Minecraft.getInstance().particles.addParticle(ParticleTypes.LIGHTNING, rx, ry, rz, 0.0D, 0.0D,
                    0.0D);
        }
    }

}
