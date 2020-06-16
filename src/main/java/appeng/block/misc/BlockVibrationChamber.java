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

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ContainerDrive;
import appeng.container.implementations.ContainerVibrationChamber;
import appeng.core.AEConfig;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileVibrationChamber;
import appeng.util.Platform;

public final class BlockVibrationChamber extends AEBaseTileBlock<TileVibrationChamber> {

    // Indicates that the vibration chamber is currently working
    private static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public BlockVibrationChamber() {
        super(defaultProps(Material.IRON).hardnessAndResistance(4.2F));
        this.setDefaultState(this.getDefaultState().with(ACTIVE, false));
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, TileVibrationChamber te) {
        return currentState.with(ACTIVE, te.isOn);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(ACTIVE);
    }

    @Override
    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        if (player.isCrouching()) {
            return ActionResultType.PASS;
        }

        if (Platform.isServer()) {
            final TileVibrationChamber tc = this.getTileEntity(w, pos);
            if (tc != null && !player.isCrouching()) {
                ContainerOpener.openContainer(ContainerVibrationChamber.TYPE, player,
                        ContainerLocator.forTileEntitySide(tc, hit.getFace()));
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void animateTick(final BlockState state, final World w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        final AEBaseTile tile = this.getTileEntity(w, pos);
        if (tile instanceof TileVibrationChamber) {
            final TileVibrationChamber tc = (TileVibrationChamber) tile;
            if (tc.isOn) {
                double f1 = pos.getX() + 0.5F;
                double f2 = pos.getY() + 0.5F;
                double f3 = pos.getZ() + 0.5F;

                final Direction forward = tc.getForward();
                final Direction up = tc.getUp();

                final int west_x = forward.getYOffset() * up.getZOffset() - forward.getZOffset() * up.getYOffset();
                final int west_y = forward.getZOffset() * up.getXOffset() - forward.getXOffset() * up.getZOffset();
                final int west_z = forward.getXOffset() * up.getYOffset() - forward.getYOffset() * up.getXOffset();

                f1 += forward.getXOffset() * 0.6;
                f2 += forward.getYOffset() * 0.6;
                f3 += forward.getZOffset() * 0.6;

                final double ox = r.nextDouble();
                final double oy = r.nextDouble() * 0.2f;

                f1 += up.getXOffset() * (-0.3 + oy);
                f2 += up.getYOffset() * (-0.3 + oy);
                f3 += up.getZOffset() * (-0.3 + oy);

                f1 += west_x * (0.3 * ox - 0.15);
                f2 += west_y * (0.3 * ox - 0.15);
                f3 += west_z * (0.3 * ox - 0.15);

                w.addParticle(ParticleTypes.SMOKE, f1, f2, f3, 0.0D, 0.0D, 0.0D);
                w.addParticle(ParticleTypes.FLAME, f1, f2, f3, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
