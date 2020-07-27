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
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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

    private static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public QuartzGrowthAcceleratorBlock() {
        super(defaultProps(Material.STONE).sounds(BlockSoundGroup.METAL));
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState,
            QuartzGrowthAcceleratorBlockEntity te) {
        return currentState.with(POWERED, te.isPowered());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void randomDisplayTick(final BlockState state, final World w, final BlockPos pos, final Random r) {
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

            rx += up.getOffsetX() * d0;
            ry += up.getOffsetY() * d0;
            rz += up.getOffsetZ() * d0;

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
                    pt = new BlockPos(x + west.getOffsetX(), y + west.getOffsetY(), z + west.getOffsetZ());

                    break;
                case 1:
                    dx = d1;
                    dz += 0.6;
                    pt = new BlockPos(x + forward.getOffsetX(), y + forward.getOffsetY(), z + forward.getOffsetZ());

                    break;
                case 2:
                    dx = d1;
                    dz = -0.6;
                    pt = new BlockPos(x - forward.getOffsetX(), y - forward.getOffsetY(), z - forward.getOffsetZ());

                    break;
                case 3:
                    dx = -0.6;
                    dz = d1;
                    pt = new BlockPos(x - west.getOffsetX(), y - west.getOffsetY(), z - west.getOffsetZ());

                    break;
            }

            if (!w.getBlockState(pt).isAir()) {
                return;
            }

            rx += dx * west.getOffsetX();
            ry += dx * west.getOffsetY();
            rz += dx * west.getOffsetZ();

            rx += dz * forward.getOffsetX();
            ry += dz * forward.getOffsetY();
            rz += dz * forward.getOffsetZ();

            MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.LIGHTNING, rx, ry, rz, 0.0D, 0.0D,
                    0.0D);
        }
    }

}
