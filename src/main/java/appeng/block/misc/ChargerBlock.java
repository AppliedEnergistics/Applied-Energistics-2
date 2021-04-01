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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import appeng.api.util.AEAxisAlignedBB;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.tile.misc.ChargerTileEntity;
import appeng.util.Platform;

public class ChargerBlock extends AEBaseTileBlock<ChargerTileEntity> {

    public ChargerBlock() {
        super(defaultProps(Material.IRON).notSolid());
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 2; // FIXME Double check this (esp. value range)
    }

    @Override
    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        if (player.isCrouching()) {
            return ActionResultType.field_5811;
        }

        if (Platform.isServer()) {
            final ChargerTileEntity tc = this.getTileEntity(w, pos);
            if (tc != null) {
                tc.activate(player);
            }
        }

        return ActionResultType.field_5812;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void animateTick(final BlockState state, final World w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        if (r.nextFloat() < 0.98) {
            return;
        }

        final ChargerTileEntity tile = this.getTileEntity(w, pos);
        if (tile != null) {
            if (Api.instance().definitions().materials().certusQuartzCrystalCharged()
                    .isSameAs(tile.getInternalInventory().getInvStack(0))) {
                final double xOff = 0.0;
                final double yOff = 0.0;
                final double zOff = 0.0;

                for (int bolts = 0; bolts < 3; bolts++) {
                    if (AppEng.instance().shouldAddParticles(r)) {
                        Minecraft.getInstance().particles.addParticle(ParticleTypes.LIGHTNING,
                                xOff + 0.5 + pos.getX(), yOff + 0.5 + pos.getY(), zOff + 0.5 + pos.getZ(), 0.0, 0.0,
                                0.0);
                    }
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader w, BlockPos pos, ISelectionContext context) {

        final ChargerTileEntity tile = this.getTileEntity(w, pos);
        if (tile != null) {
            final double twoPixels = 2.0 / 16.0;
            final Direction up = tile.getUp();
            final Direction forward = tile.getForward();
            final AEAxisAlignedBB bb = new AEAxisAlignedBB(twoPixels, twoPixels, twoPixels, 1.0 - twoPixels,
                    1.0 - twoPixels, 1.0 - twoPixels);

            if (up.getXOffset() != 0) {
                bb.minX = 0;
                bb.maxX = 1;
            }
            if (up.getYOffset() != 0) {
                bb.minY = 0;
                bb.maxY = 1;
            }
            if (up.getZOffset() != 0) {
                bb.minZ = 0;
                bb.maxZ = 1;
            }

            switch (forward) {
                case field_11033:
                    bb.maxY = 1;
                    break;
                case field_11036:
                    bb.minY = 0;
                    break;
                case field_11043:
                    bb.maxZ = 1;
                    break;
                case field_11035:
                    bb.minZ = 0;
                    break;
                case field_11034:
                    bb.minX = 0;
                    break;
                case field_11039:
                    bb.maxX = 1;
                    break;
                default:
                    break;
            }

            return VoxelShapes.create(bb.getBoundingBox());
        }
        return VoxelShapes.create(new AxisAlignedBB(0.0, 0, 0.0, 1.0, 1.0, 1.0));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.create(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    }

}
