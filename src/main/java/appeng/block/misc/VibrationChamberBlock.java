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
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.VibrationChamberContainer;
import appeng.core.AEConfig;
import appeng.tile.misc.VibrationChamberBlockEntity;
import appeng.util.Platform;

public final class VibrationChamberBlock extends AEBaseTileBlock<VibrationChamberBlockEntity> {

    // Indicates that the vibration chamber is currently working
    private static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    public VibrationChamberBlock() {
        super(defaultProps(Material.METAL).strength(4.2F));
        this.setDefaultState(this.getDefaultState().with(ACTIVE, false));
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, VibrationChamberBlockEntity te) {
        return currentState.with(ACTIVE, te.isOn);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ACTIVE);
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (player.isInSneakingPose()) {
            return ActionResult.PASS;
        }

        if (Platform.isServer()) {
            final VibrationChamberBlockEntity tc = this.getBlockEntity(w, pos);
            if (tc != null && !player.isInSneakingPose()) {
                ContainerOpener.openContainer(VibrationChamberContainer.TYPE, player,
                        ContainerLocator.forTileEntitySide(tc, hit.getSide()));
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void randomDisplayTick(final BlockState state, final World w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        final VibrationChamberBlockEntity tile = this.getBlockEntity(w, pos);
        if (tile != null && tile.isOn) {
            double f1 = pos.getX() + 0.5F;
            double f2 = pos.getY() + 0.5F;
            double f3 = pos.getZ() + 0.5F;

            final Direction forward = tile.getForward();
            final Direction up = tile.getUp();

            final int west_x = forward.getOffsetY() * up.getOffsetZ() - forward.getOffsetZ() * up.getOffsetY();
            final int west_y = forward.getOffsetZ() * up.getOffsetX() - forward.getOffsetX() * up.getOffsetZ();
            final int west_z = forward.getOffsetX() * up.getOffsetY() - forward.getOffsetY() * up.getOffsetX();

            f1 += forward.getOffsetX() * 0.6;
            f2 += forward.getOffsetY() * 0.6;
            f3 += forward.getOffsetZ() * 0.6;

            final double ox = r.nextDouble();
            final double oy = r.nextDouble() * 0.2f;

            f1 += up.getOffsetX() * (-0.3 + oy);
            f2 += up.getOffsetY() * (-0.3 + oy);
            f3 += up.getOffsetZ() * (-0.3 + oy);

            f1 += west_x * (0.3 * ox - 0.15);
            f2 += west_y * (0.3 * ox - 0.15);
            f3 += west_z * (0.3 * ox - 0.15);

            w.addParticle(ParticleTypes.SMOKE, f1, f2, f3, 0.0D, 0.0D, 0.0D);
            w.addParticle(ParticleTypes.FLAME, f1, f2, f3, 0.0D, 0.0D, 0.0D);
        }
    }
}
