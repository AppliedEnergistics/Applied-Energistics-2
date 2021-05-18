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

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.core.AELog;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;

public class EraserItem extends AEBaseItem {

    private static final int BOX_SIZE = 48;
    private static final int BLOCK_ERASE_LIMIT = BOX_SIZE * BOX_SIZE * BOX_SIZE;
    final static Set<Block> COMMON_BLOCKS = new HashSet<>();

    public EraserItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getWorld().isRemote()) {
            return ActionResultType.PASS;
        }

        final PlayerEntity player = context.getPlayer();
        final World world = context.getWorld();
        final BlockPos pos = context.getPos();

        if (player == null) {
            return ActionResultType.PASS;
        }

        final Block state = world.getBlockState(pos).getBlock();
        final boolean bulk = InteractionUtil.isInAlternateUseMode(player);
        final Queue<BlockPos> next = new ArrayDeque<>();
        final Set<BlockPos> closed = new HashSet<>();
        final Set<Block> commonBlocks = this.getCommonBlocks();

        next.add(pos);
        int blocks = 0;

        while (blocks < BLOCK_ERASE_LIMIT && next.peek() != null) {
            final BlockPos wc = next.poll();
            final Block c_state = world.getBlockState(wc).getBlock();
            final boolean contains = state == c_state || bulk && commonBlocks.contains(c_state);

            closed.add(wc);

            if (contains) {
                blocks++;
                world.setBlockState(wc, Blocks.AIR.getDefaultState(), 2);
                world.destroyBlock(wc, false);

                if (isInsideBox(wc, pos)) {
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                if (0 == x && 0 == y && 0 == z) {
                                    continue;
                                }
                                final BlockPos nextPos = wc.add(x, y, z);
                                if (!closed.contains(nextPos)) {
                                    next.add(nextPos);
                                }
                            }
                        }
                    }
                }
            }
        }

        AELog.info("Delete " + blocks + " blocks");

        return ActionResultType.func_233537_a_(world.isRemote());
    }

    private boolean isInsideBox(BlockPos pos, BlockPos origin) {
        boolean ret = true;

        if (pos.getX() > origin.getX() + BOX_SIZE || pos.getX() < origin.getX() - BOX_SIZE) {
            ret = false;
        }
        if (pos.getY() > origin.getY() + BOX_SIZE || pos.getY() < origin.getY() - BOX_SIZE) {
            ret = false;
        }
        if (pos.getZ() > origin.getZ() + BOX_SIZE || pos.getZ() < origin.getZ() - BOX_SIZE) {
            ret = false;
        }

        return ret;
    }

    /**
     * Filling needs to be deferred as the tags might not be populated at construction time.
     *
     * @return
     */
    private Set<Block> getCommonBlocks() {
        if (COMMON_BLOCKS.isEmpty()) {
            COMMON_BLOCKS.add(Blocks.STONE);
            COMMON_BLOCKS.add(Blocks.DIRT);
            COMMON_BLOCKS.add(Blocks.GRASS_BLOCK);
            COMMON_BLOCKS.add(Blocks.COBBLESTONE);
            COMMON_BLOCKS.add(Blocks.ANDESITE);
            COMMON_BLOCKS.add(Blocks.GRANITE);
            COMMON_BLOCKS.add(Blocks.DIORITE);
            COMMON_BLOCKS.add(Blocks.GRAVEL);
            COMMON_BLOCKS.add(Blocks.SANDSTONE);
            COMMON_BLOCKS.add(Blocks.NETHERRACK);
            COMMON_BLOCKS.add(Blocks.WATER);
            COMMON_BLOCKS.add(Blocks.LAVA);

            COMMON_BLOCKS.addAll(BlockTags.LEAVES.getAllElements());
            COMMON_BLOCKS.addAll(BlockTags.SAND.getAllElements());
            COMMON_BLOCKS.addAll(BlockTags.LOGS.getAllElements());
        }

        return COMMON_BLOCKS;
    }
}
