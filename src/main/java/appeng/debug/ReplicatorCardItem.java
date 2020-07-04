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

package appeng.debug;

import appeng.hooks.AEToolItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.items.AEBaseItem;

public class ReplicatorCardItem extends AEBaseItem implements AEToolItem {

    public ReplicatorCardItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        if (context.getWorld().isClient()) {
            return ActionResult.PASS;
        }

        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Direction side = context.getSide();
        Hand hand = context.getHand();

        if (player == null) {
            return ActionResult.PASS;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (player.isInSneakingPose()) {
            if (world.getBlockEntity(pos) instanceof IGridHost) {
                final CompoundTag tag = new CompoundTag();
                tag.putInt("x", x);
                tag.putInt("y", y);
                tag.putInt("z", z);
                tag.putInt("side", side.ordinal());
                tag.putString("w", world.getRegistryKey().getValue().toString());
                player.getStackInHand(hand).setTag(tag);
            } else {
                this.outputMsg(player, "This is not a Grid Tile.");
            }
        } else {
            final CompoundTag ish = player.getStackInHand(hand).getTag();
            if (ish != null) {
                final int src_x = ish.getInt("x");
                final int src_y = ish.getInt("y");
                final int src_z = ish.getInt("z");
                final int src_side = ish.getInt("side");
                final String worldId = ish.getString("w");
                final World src_w = world.getServer().getWorld(RegistryKey.of(Registry.DIMENSION, new Identifier(worldId)));

                final BlockEntity te = src_w.getBlockEntity(new BlockPos(src_x, src_y, src_z));
                if (te instanceof IGridHost) {
                    final IGridHost gh = (IGridHost) te;
                    final Direction sideOff = Direction.values()[src_side];
                    final Direction currentSideOff = side;
                    final IGridNode n = gh.getGridNode(AEPartLocation.fromFacing(sideOff));
                    if (n != null) {
                        final IGrid g = n.getGrid();
                        if (g != null) {
                            final ISpatialCache sc = g.getCache(ISpatialCache.class);
                            if (sc.isValidRegion()) {
                                final DimensionalCoord min = sc.getMin();
                                final DimensionalCoord max = sc.getMax();

                                x += currentSideOff.getOffsetX();
                                y += currentSideOff.getOffsetY();
                                z += currentSideOff.getOffsetZ();

                                final int min_x = min.x;
                                final int min_y = min.y;
                                final int min_z = min.z;

                                final int rel_x = min.x - src_x + x;
                                final int rel_y = min.y - src_y + y;
                                final int rel_z = min.z - src_z + z;

                                final int scale_x = max.x - min.x;
                                final int scale_y = max.y - min.y;
                                final int scale_z = max.z - min.z;

                                for (int i = 1; i < scale_x; i++) {
                                    for (int j = 1; j < scale_y; j++) {
                                        for (int k = 1; k < scale_z; k++) {
                                            final BlockPos p = new BlockPos(min_x + i, min_y + j, min_z + k);
                                            final BlockPos d = new BlockPos(i + rel_x, j + rel_y, k + rel_z);
                                            final BlockState state = src_w.getBlockState(p);
                                            final Block blk = state.getBlock();
                                            final BlockState prev = world.getBlockState(d);

                                            world.setBlockState(d, state);
                                            if (blk instanceof BlockEntityProvider) {
                                                BlockEntityProvider blkEntityProvider = (BlockEntityProvider)blk;
                                                final BlockEntity ote = src_w.getBlockEntity(p);
                                                final BlockEntity nte = blkEntityProvider.createBlockEntity(world);
                                                final CompoundTag data = new CompoundTag();
                                                ote.toTag(data);
                                                nte.fromTag(state, data.copy());
                                                world.setBlockEntity(d, nte);
                                            }
                                            world.updateListeners(d, prev, state, 3);
                                        }
                                    }
                                }
                            } else {
                                this.outputMsg(player, "requires valid spatial pylon setup.");
                            }
                        } else {
                            this.outputMsg(player, "no grid?");
                        }
                    } else {
                        this.outputMsg(player, "No grid node?");
                    }
                } else {
                    this.outputMsg(player, "Src is no longer a grid block?");
                }
            } else {
                this.outputMsg(player, "No Source Defined");
            }
        }
        return ActionResult.SUCCESS;
    }

    private void outputMsg(final Entity player, final String string) {
        player.sendSystemMessage(new LiteralText(string), Util.NIL_UUID);
    }
}
