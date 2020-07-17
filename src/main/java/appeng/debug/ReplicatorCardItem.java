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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote()) {
            final CompoundNBT tag = playerIn.getHeldItem(handIn).getOrCreateTag();
            final int replications;

            if (tag.contains("r")) {
                replications = (tag.getInt("r") + 1) % 4;
            } else {
                replications = 0;
            }

            tag.putInt("r", replications);

            playerIn.sendMessage(new StringTextComponent((replications + 1) + "³ Replications"));
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        if (context.getWorld().isClient()) {
            // Needed, otherwise client will trigger onItemRightClick also on server...
            return ActionResult.SUCCESS;
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
                final CompoundTag tag = player.getStackInHand(hand).getOrCreateTag();
                tag.putInt("x", x);
                tag.putInt("y", y);
                tag.putInt("z", z);
                tag.putInt("side", side.ordinal());
                tag.putString("w", world.getRegistryKey().getValue().toString());
                tag.putInt("r", 0);

                this.outputMsg(player, "Set replicator source");
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
                final int replications = ish.getInt("r") + 1;

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

                                // TODO: Why??? Places it one block up each time...
                                // x += currentSideOff.getOffsetX();
                                // y += currentSideOff.getOffsetY();
                                // z += currentSideOff.getOffsetZ();

                                final int sc_size_x = max.x - min.x;
                                final int sc_size_y = max.y - min.y;
                                final int sc_size_z = max.z - min.z;

                                final int min_x = min.x;
                                final int min_y = min.y;
                                final int min_z = min.z;

                                // Invert to maintain correct sign for west/east
                                final int x_rot = (int) -Math.signum(MathHelper.wrapDegrees(player.rotationYaw));
                                // Rotate by 90 degree, so north/south are negative/positive
                                final int z_rot = (int) Math.signum(MathHelper.wrapDegrees(player.rotationYaw + 90));

                                // Loops for replication in each direction
                                for (int r_x = 0; r_x < replications; r_x++) {
                                    for (int r_y = 0; r_y < replications; r_y++) {
                                        for (int r_z = 0; r_z < replications; r_z++) {

                                            // Offset x/z by the rotation index.
                                            // For sake of simplicity always grow upwards.
                                            final int rel_x = min.x - src_x + x + (r_x * sc_size_x * x_rot);
                                            final int rel_y = min.y - src_y + y + (r_y * sc_size_y);
                                            final int rel_z = min.z - src_z + z + (r_z * sc_size_z * z_rot);

                                            // Copy a single SC instance completely
                                            for (int i = 1; i < sc_size_x; i++) {
                                                for (int j = 1; j < sc_size_y; j++) {
                                                    for (int k = 1; k < sc_size_z; k++) {
                                                        final BlockPos p = new BlockPos(min_x + i, min_y + j,
                                                                min_z + k);
                                                        final BlockPos d = new BlockPos(i + rel_x, j + rel_y,
                                                                k + rel_z);

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
