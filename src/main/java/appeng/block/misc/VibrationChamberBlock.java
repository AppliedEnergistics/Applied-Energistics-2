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

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
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

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.core.AEConfig;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.VibrationChamberMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;

public final class VibrationChamberBlock extends AEBaseEntityBlock<VibrationChamberBlockEntity> {

    // Indicates that the vibration chamber is currently working
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public VibrationChamberBlock() {
        super(defaultProps(Material.METAL).strength(4.2F));
        this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, VibrationChamberBlockEntity be) {
        return currentState.setValue(ACTIVE, be.isOn);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }

    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player player,
            InteractionHand hand,
            @Nullable ItemStack heldItem, BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            final VibrationChamberBlockEntity tc = this.getBlockEntity(level, pos);
            if (tc != null) {
                hit.getDirection();
                MenuOpener.open(VibrationChamberMenu.TYPE, player,
                        MenuLocators.forBlockEntity(tc));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        var tc = this.getBlockEntity(level, pos);
        if (tc != null && tc.isOn) {
            double f1 = pos.getX() + 0.5F;
            double f2 = pos.getY() + 0.5F;
            double f3 = pos.getZ() + 0.5F;

            var front = tc.getFront();
            var top = tc.getTop();

            // Cross-Product of forward/up directional vector
            final int west_x = front.getStepY() * top.getStepZ() - front.getStepZ() * top.getStepY();
            final int west_y = front.getStepZ() * top.getStepX() - front.getStepX() * top.getStepZ();
            final int west_z = front.getStepX() * top.getStepY() - front.getStepY() * top.getStepX();

            f1 += front.getStepX() * 0.6;
            f2 += front.getStepY() * 0.6;
            f3 += front.getStepZ() * 0.6;

            final double ox = r.nextDouble();
            final double oy = r.nextDouble() * 0.2f;

            f1 += top.getStepX() * (-0.3 + oy);
            f2 += top.getStepY() * (-0.3 + oy);
            f3 += top.getStepZ() * (-0.3 + oy);

            f1 += west_x * (0.3 * ox - 0.15);
            f2 += west_y * (0.3 * ox - 0.15);
            f3 += west_z * (0.3 * ox - 0.15);

            level.addParticle(ParticleTypes.SMOKE, f1, f2, f3, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, f1, f2, f3, 0.0D, 0.0D, 0.0D);
        }
    }
}
