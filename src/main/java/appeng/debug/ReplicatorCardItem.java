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

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.spatial.ISpatialService;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;

public class ReplicatorCardItem extends AEBaseItem {

    public ReplicatorCardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player playerIn, InteractionHand handIn) {
        if (!level.isClientSide()) {
            final CompoundTag tag = playerIn.getItemInHand(handIn).getOrCreateTag();
            final int replications;

            if (tag.contains("r")) {
                replications = (tag.getInt("r") + 1) % 4;
            } else {
                replications = 0;
            }

            tag.putInt("r", replications);

            playerIn.sendMessage(new TextComponent(replications + 1 + "³ Replications"), Util.NIL_UUID);
        }

        return super.use(level, playerIn, handIn);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            // Needed, otherwise client will trigger onItemRightClick also on server...
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        InteractionHand hand = context.getHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (InteractionUtil.isInAlternateUseMode(player)) {
            var gridHost = GridHelper.getNodeHost(level, pos);

            if (gridHost != null) {
                final CompoundTag tag = player.getItemInHand(hand).getOrCreateTag();
                tag.putInt("x", x);
                tag.putInt("y", y);
                tag.putInt("z", z);
                tag.putInt("side", side.ordinal());
                tag.putString("w", level.dimension().location().toString());
                tag.putInt("r", 0);

                this.outputMsg(player, "Set replicator source");
            } else {
                this.outputMsg(player, "This does not host a grid node");
            }
        } else {
            final CompoundTag ish = player.getItemInHand(hand).getTag();
            if (ish != null) {
                final int src_x = ish.getInt("x");
                final int src_y = ish.getInt("y");
                final int src_z = ish.getInt("z");
                final int src_side = ish.getInt("side");
                final String worldId = ish.getString("w");
                final Level src_w = level.getServer()
                        .getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(worldId)));
                final int replications = ish.getInt("r") + 1;

                var gh = GridHelper.getNodeHost(src_w, new BlockPos(src_x, src_y, src_z));

                if (gh != null) {
                    final Direction sideOff = Direction.values()[src_side];
                    final Direction currentSideOff = side;
                    final IGridNode n = gh.getGridNode(sideOff);

                    if (n != null) {
                        final IGrid g = n.getGrid();

                        final ISpatialService sc = g.getSpatialService();

                        if (sc.isValidRegion()) {
                            var min = sc.getMin();
                            var max = sc.getMax();

                            // TODO: Why??? Places it one block up each time...
                            // x += currentSideOff.getXOffset();
                            // y += currentSideOff.getYOffset();
                            // z += currentSideOff.getZOffset();

                            final int sc_size_x = max.getX() - min.getX();
                            final int sc_size_y = max.getY() - min.getY();
                            final int sc_size_z = max.getZ() - min.getZ();

                            final int min_x = min.getX();
                            final int min_y = min.getY();
                            final int min_z = min.getZ();

                            // Invert to maintain correct sign for west/east
                            final int x_rot = (int) -Math.signum(Mth.wrapDegrees(player.getYRot()));
                            // Rotate by 90 degree, so north/south are negative/positive
                            final int z_rot = (int) Math.signum(Mth.wrapDegrees(player.getYRot() + 90));

                            // Loops for replication in each direction
                            for (int r_x = 0; r_x < replications; r_x++) {
                                for (int r_y = 0; r_y < replications; r_y++) {
                                    for (int r_z = 0; r_z < replications; r_z++) {

                                        // Offset x/z by the rotation index.
                                        // For sake of simplicity always grow upwards.
                                        final int rel_x = min.getX() - src_x + x + r_x * sc_size_x * x_rot;
                                        final int rel_y = min.getY() - src_y + y + r_y * sc_size_y;
                                        final int rel_z = min.getZ() - src_z + z + r_z * sc_size_z * z_rot;

                                        // Copy a single SC instance completely
                                        for (int i = 1; i < sc_size_x; i++) {
                                            for (int j = 1; j < sc_size_y; j++) {
                                                for (int k = 1; k < sc_size_z; k++) {
                                                    final BlockPos p = new BlockPos(min_x + i, min_y + j,
                                                            min_z + k);
                                                    final BlockPos d = new BlockPos(i + rel_x, j + rel_y,
                                                            k + rel_z);

                                                    final BlockState state = src_w.getBlockState(p);
                                                    final BlockState prev = level.getBlockState(d);

                                                    level.setBlockAndUpdate(d, state);
                                                    if (state.hasBlockEntity()) {
                                                        final BlockEntity ote = src_w.getBlockEntity(p);
                                                        var data = ote.saveWithId();
                                                        var newBe = BlockEntity.loadStatic(d, state, data);
                                                        if (newBe != null) {
                                                            level.setBlockEntity(newBe);
                                                        }
                                                    }
                                                    level.sendBlockUpdated(d, prev, state, 3);
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
                        this.outputMsg(player, "No grid node?");
                    }
                } else {
                    this.outputMsg(player, "Src is no longer a grid block?");
                }
            } else {
                this.outputMsg(player, "No Source Defined");
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void outputMsg(final Entity player, final String string) {
        player.sendMessage(new TextComponent(string), Util.NIL_UUID);
    }
}
