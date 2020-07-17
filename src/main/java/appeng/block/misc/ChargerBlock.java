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
import java.util.function.Function;

import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.util.math.Box;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import appeng.api.util.AEAxisAlignedBB;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.renderable.ItemRenderable;
import appeng.client.render.tesr.ModularTESR;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.tile.misc.ChargerBlockEntity;
import appeng.util.Platform;

public class ChargerBlock extends AEBaseTileBlock<ChargerBlockEntity> {

    public ChargerBlock() {
        super(defaultProps(Material.METAL).solidBlock((state, world, pos) -> false));
    }

    @Override
    public int getOpacity(BlockState state, BlockView worldIn, BlockPos pos) {
        return 2; // FIXME Double check this (esp. value range)
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (player.isInSneakingPose()) {
            return ActionResult.PASS;
        }

        if (Platform.isServer()) {
            final ChargerBlockEntity tc = this.getBlockEntity(w, pos);
            if (tc != null) {
                tc.activate(player);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(final BlockState state, final World w, final BlockPos pos, final Random r) {
        if (!AEConfig.instance().isEnableEffects()) {
            return;
        }

        if (r.nextFloat() < 0.98) {
            return;
        }

        final ChargerBlockEntity tile = this.getBlockEntity(w, pos);
        if (tile != null) {
            if (Api.instance().definitions().materials().certusQuartzCrystalCharged()
                    .isSameAs(tile.getInternalInventory().getInvStack(0))) {
                final double xOff = 0.0;
                final double yOff = 0.0;
                final double zOff = 0.0;

                for (int bolts = 0; bolts < 3; bolts++) {
                    if (AppEng.instance().shouldAddParticles(r)) {
                        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.LIGHTNING, xOff + 0.5 + pos.getX(),
                                yOff + 0.5 + pos.getY(), zOff + 0.5 + pos.getZ(), 0.0, 0.0, 0.0);
                    }
                }
            }
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView w, BlockPos pos, ShapeContext context) {

        final ChargerBlockEntity tile = this.getBlockEntity(w, pos);
        if (tile != null) {
            final double twoPixels = 2.0 / 16.0;
            final Direction up = tile.getUp();
            final Direction forward = tile.getForward();
            final AEAxisAlignedBB bb = new AEAxisAlignedBB(twoPixels, twoPixels, twoPixels, 1.0 - twoPixels,
                    1.0 - twoPixels, 1.0 - twoPixels);

            if (up.getOffsetX() != 0) {
                bb.minX = 0;
                bb.maxX = 1;
            }
            if (up.getOffsetY() != 0) {
                bb.minY = 0;
                bb.maxY = 1;
            }
            if (up.getOffsetZ() != 0) {
                bb.minZ = 0;
                bb.maxZ = 1;
            }

            switch (forward) {
                case DOWN:
                    bb.maxY = 1;
                    break;
                case UP:
                    bb.minY = 0;
                    break;
                case NORTH:
                    bb.maxZ = 1;
                    break;
                case SOUTH:
                    bb.minZ = 0;
                    break;
                case EAST:
                    bb.minX = 0;
                    break;
                case WEST:
                    bb.maxX = 1;
                    break;
                default:
                    break;
            }

            return VoxelShapes.cuboid(bb.getBoundingBox());
        }
        return VoxelShapes.cuboid(new Box(0.0, 0, 0.0, 1.0, 1.0, 1.0));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos,
            ShapeContext context) {
        return VoxelShapes.cuboid(new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    }

    @Environment(EnvType.CLIENT)
    public static Function<BlockEntityRenderDispatcher, BlockEntityRenderer<ChargerBlockEntity>> createTesr() {
        return dispatcher -> new ModularTESR<>(dispatcher, new ItemRenderable<>(ChargerBlock::getRenderedItem));
    }

    @Environment(EnvType.CLIENT)
    private static Pair<ItemStack, Transformation> getRenderedItem(ChargerBlockEntity tile) {
        Transformation transform = new Transformation(new Vector3f(), new Vector3f(0.5f, 0.375f, 0.5f), new Vector3f(1f, 1f, 1f));
        return new ImmutablePair<>(tile.getInternalInventory().getInvStack(0), transform);
    }

}
