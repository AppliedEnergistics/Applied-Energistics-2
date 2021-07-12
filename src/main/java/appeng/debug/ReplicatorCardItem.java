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

import appeng.core.Api;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.spatial.ISpatialService;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;

public class ReplicatorCardItem extends AEBaseItem {

    public ReplicatorCardItem(Properties properties) {
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

            playerIn.sendMessage(new StringTextComponent(replications + 1 + "³ Replications"), Util.DUMMY_UUID);
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World w = context.getWorld();
        if (w.isRemote()) {
            // Needed, otherwise client will trigger onItemRightClick also on server...
            return ActionResultType.func_233537_a_(w.isRemote());
        }

        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction side = context.getFace();
        Hand hand = context.getHand();

        if (player == null) {
            return ActionResultType.PASS;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (world.getTileEntity(pos) instanceof IGridNodeHost) {
                final CompoundNBT tag = player.getHeldItem(hand).getOrCreateTag();
                tag.putInt("x", x);
                tag.putInt("y", y);
                tag.putInt("z", z);
                tag.putInt("side", side.ordinal());
                tag.putString("w", world.getDimensionKey().getLocation().toString());
                tag.putInt("r", 0);

                this.outputMsg(player, "Set replicator source");
            } else {
                this.outputMsg(player, "This is not a Grid Tile.");
            }
        } else {
            final CompoundNBT ish = player.getHeldItem(hand).getTag();
            if (ish != null) {
                final int src_x = ish.getInt("x");
                final int src_y = ish.getInt("y");
                final int src_z = ish.getInt("z");
                final int src_side = ish.getInt("side");
                final String worldId = ish.getString("w");
                final World src_w = world.getServer()
                        .getWorld(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(worldId)));
                final int replications = ish.getInt("r") + 1;

                var gh = Api.instance().grid().getNodeHost(src_w, new BlockPos(src_x, src_y, src_z));

                if (gh != null) {
                    final Direction sideOff = Direction.values()[src_side];
                    final Direction currentSideOff = side;
                    final IGridNode n = gh.getGridNode(sideOff);

                    if (n != null) {
                        final IGrid g = n.getGrid();

                        if (g != null) {
                            final ISpatialService sc = g.getService(ISpatialService.class);

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
                                final int x_rot = (int) -Math.signum(MathHelper.wrapDegrees(player.rotationYaw));
                                // Rotate by 90 degree, so north/south are negative/positive
                                final int z_rot = (int) Math.signum(MathHelper.wrapDegrees(player.rotationYaw + 90));

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
                                                        final Block blk = state.getBlock();
                                                        final BlockState prev = world.getBlockState(d);

                                                        world.setBlockState(d, state);
                                                        if (blk != null && blk.hasTileEntity(state)) {
                                                            final TileEntity ote = src_w.getTileEntity(p);
                                                            final TileEntity nte = blk.createTileEntity(state, world);
                                                            final CompoundNBT data = new CompoundNBT();
                                                            ote.write(data);
                                                            nte.read(state, data.copy());
                                                            world.setTileEntity(d, nte);
                                                        }
                                                        world.notifyBlockUpdate(d, prev, state, 3);
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
        return ActionResultType.func_233537_a_(w.isRemote());
    }

    private void outputMsg(final Entity player, final String string) {
        player.sendMessage(new StringTextComponent(string), Util.DUMMY_UUID);
    }
}
