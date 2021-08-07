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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.VibrationChamberContainer;
import appeng.core.AEConfig;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.util.InteractionUtil;

public final class VibrationChamberBlock extends AEBaseEntityBlock<VibrationChamberBlockEntity> {

    // Indicates that the vibration chamber is currently working
    private static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public VibrationChamberBlock() {
        super(defaultProps(Material.METAL).strength(4.2F));
        this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, VibrationChamberBlockEntity te) {
        return currentState.setValue(ACTIVE, te.isOn);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public InteractionResult onActivated(final Level w, final BlockPos pos, final Player player,
            final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        if (!w.isClientSide()) {
            final VibrationChamberBlockEntity tc = this.getTileEntity(w, pos);
            if (tc != null) {
                ContainerOpener.openContainer(VibrationChamberContainer.TYPE, player,
                        ContainerLocator.forTileEntitySide(tc, hit.getDirection()));
            }
        }

        return InteractionResult.sidedSuccess(w.isClientSide());
    }

    @Override
    public void animateTick(final BlockState state, final Level w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        final VibrationChamberBlockEntity tc = this.getTileEntity(w, pos);
        if (tc != null && tc.isOn) {
            double f1 = pos.getX() + 0.5F;
            double f2 = pos.getY() + 0.5F;
            double f3 = pos.getZ() + 0.5F;

            final Direction forward = tc.getForward();
            final Direction up = tc.getUp();

            // Cross-Product of forward/up directional vector
            final int west_x = forward.getStepY() * up.getStepZ() - forward.getStepZ() * up.getStepY();
            final int west_y = forward.getStepZ() * up.getStepX() - forward.getStepX() * up.getStepZ();
            final int west_z = forward.getStepX() * up.getStepY() - forward.getStepY() * up.getStepX();

            f1 += forward.getStepX() * 0.6;
            f2 += forward.getStepY() * 0.6;
            f3 += forward.getStepZ() * 0.6;

            final double ox = r.nextDouble();
            final double oy = r.nextDouble() * 0.2f;

            f1 += up.getStepX() * (-0.3 + oy);
            f2 += up.getStepY() * (-0.3 + oy);
            f3 += up.getStepZ() * (-0.3 + oy);

            f1 += west_x * (0.3 * ox - 0.15);
            f2 += west_y * (0.3 * ox - 0.15);
            f3 += west_z * (0.3 * ox - 0.15);

            w.addParticle(ParticleTypes.SMOKE, f1, f2, f3, 0.0D, 0.0D, 0.0D);
            w.addParticle(ParticleTypes.FLAME, f1, f2, f3, 0.0D, 0.0D, 0.0D);
        }
    }
}
