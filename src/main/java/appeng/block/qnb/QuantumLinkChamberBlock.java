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

package appeng.block.qnb;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import appeng.client.EffectType;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.QNBContainer;
import appeng.core.AppEng;
import appeng.helpers.AEMaterials;
import appeng.tile.qnb.QuantumBridgeBlockEntity;
import appeng.util.Platform;

public class QuantumLinkChamberBlock extends QuantumBaseBlock {

    private static final VoxelShape SHAPE;

    static {
        final double onePixel = 2.0 / 16.0;
        SHAPE = VoxelShapes.cuboid(
                new Box(onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel));
    }

    public QuantumLinkChamberBlock() {
        super(defaultProps(AEMaterials.GLASS));
    }

    @Override
    public void randomDisplayTick(final BlockState state, final World w, final BlockPos pos, final Random rand) {
        final QuantumBridgeBlockEntity bridge = this.getBlockEntity(w, pos);
        if (bridge != null) {
            if (bridge.hasQES()) {
                if (AppEng.instance().shouldAddParticles(rand)) {
                    AppEng.instance().spawnEffect(EffectType.Energy, w, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            null);
                }
            }
        }
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (p.isInSneakingPose()) {
            return ActionResult.PASS;
        }

        final QuantumBridgeBlockEntity tg = this.getBlockEntity(w, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                ContainerOpener.openContainer(QNBContainer.TYPE, p, ContainerLocator.forTileEntity(tg));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

}
