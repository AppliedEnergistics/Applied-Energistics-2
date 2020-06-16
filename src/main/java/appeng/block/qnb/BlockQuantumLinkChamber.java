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

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.client.EffectType;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ContainerQNB;
import appeng.core.AppEng;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.Platform;

public class BlockQuantumLinkChamber extends BlockQuantumBase {

    private static final VoxelShape SHAPE;

    static {
        final double onePixel = 2.0 / 16.0;
        SHAPE = VoxelShapes.create(
                new AxisAlignedBB(onePixel, onePixel, onePixel, 1.0 - onePixel, 1.0 - onePixel, 1.0 - onePixel));
    }

    public BlockQuantumLinkChamber() {
        super(defaultProps(AEGlassMaterial.INSTANCE));
    }

    @Override
    public void animateTick(final BlockState state, final World w, final BlockPos pos, final Random rand) {
        final TileQuantumBridge bridge = this.getTileEntity(w, pos);
        if (bridge != null) {
            if (bridge.hasQES()) {
                if (AppEng.proxy.shouldAddParticles(rand)) {
                    AppEng.proxy.spawnEffect(EffectType.Energy, w, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            null);
                }
            }
        }
    }

    @Override
    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        if (p.isCrouching()) {
            return ActionResultType.PASS;
        }

        final TileQuantumBridge tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                ContainerOpener.openContainer(ContainerQNB.TYPE, p, ContainerLocator.forTileEntity(tg));
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

}
