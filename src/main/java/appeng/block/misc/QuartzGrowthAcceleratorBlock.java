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

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.QuartzGrowthAcceleratorBlockEntity;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;
import appeng.util.Platform;

public class QuartzGrowthAcceleratorBlock extends AEBaseEntityBlock<QuartzGrowthAcceleratorBlockEntity>
        implements IOrientableBlock {

    private static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public QuartzGrowthAcceleratorBlock() {
        super(defaultProps(Material.STONE).sound(SoundType.METAL));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState,
            QuartzGrowthAcceleratorBlockEntity be) {
        return currentState.setValue(POWERED, be.isPowered());
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        final QuartzGrowthAcceleratorBlockEntity cga = this.getBlockEntity(level, pos);

        if (cga != null && cga.isPowered() && AppEngClient.instance().shouldAddParticles(r)) {
            final double d0 = r.nextFloat() - 0.5F;
            final double d1 = r.nextFloat() - 0.5F;

            final Direction up = cga.getUp();
            final Direction forward = cga.getForward();
            final Direction west = Platform.crossProduct(forward, up);

            double rx = 0.5 + pos.getX();
            double ry = 0.5 + pos.getY();
            double rz = 0.5 + pos.getZ();

            rx += up.getStepX() * d0;
            ry += up.getStepY() * d0;
            rz += up.getStepZ() * d0;

            final int x = pos.getX();
            final int y = pos.getY();
            final int z = pos.getZ();

            double dz = 0;
            double dx = 0;
            BlockPos pt = null;

            switch (r.nextInt(4)) {
                case 0 -> {
                    dx = 0.6;
                    dz = d1;
                    pt = new BlockPos(x + west.getStepX(), y + west.getStepY(), z + west.getStepZ());
                }
                case 1 -> {
                    dx = d1;
                    dz += 0.6;
                    pt = new BlockPos(x + forward.getStepX(), y + forward.getStepY(), z + forward.getStepZ());
                }
                case 2 -> {
                    dx = d1;
                    dz = -0.6;
                    pt = new BlockPos(x - forward.getStepX(), y - forward.getStepY(), z - forward.getStepZ());
                }
                case 3 -> {
                    dx = -0.6;
                    dz = d1;
                    pt = new BlockPos(x - west.getStepX(), y - west.getStepY(), z - west.getStepZ());
                }
            }

            if (!level.getBlockState(pt).isAir()) {
                return;
            }

            rx += dx * west.getStepX();
            ry += dx * west.getStepY();
            rz += dx * west.getStepZ();

            rx += dz * forward.getStepX();
            ry += dz * forward.getStepY();
            rz += dz * forward.getStepZ();

            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LIGHTNING, rx, ry, rz, 0.0D, 0.0D,
                    0.0D);
        }
    }

}
