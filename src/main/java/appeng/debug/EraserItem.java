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

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import appeng.core.AELog;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;
import net.minecraft.world.item.Item.Properties;

public class EraserItem extends AEBaseItem {

    private static final int BOX_SIZE = 48;
    private static final int BLOCK_ERASE_LIMIT = BOX_SIZE * BOX_SIZE * BOX_SIZE;
    final static Set<Block> COMMON_BLOCKS = new HashSet<>();

    public EraserItem(net.minecraft.world.item.Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }

        final Player player = context.getPlayer();
        final Level world = context.getLevel();
        final net.minecraft.core.BlockPos pos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.PASS;
        }

        final net.minecraft.world.level.block.Block state = world.getBlockState(pos).getBlock();
        final boolean bulk = InteractionUtil.isInAlternateUseMode(player);
        final Queue<net.minecraft.core.BlockPos> next = new ArrayDeque<>();
        final Set<BlockPos> closed = new HashSet<>();
        final Set<Block> commonBlocks = this.getCommonBlocks();

        next.add(pos);
        int blocks = 0;

        while (blocks < BLOCK_ERASE_LIMIT && next.peek() != null) {
            final net.minecraft.core.BlockPos wc = next.poll();
            final Block c_state = world.getBlockState(wc).getBlock();
            final boolean contains = state == c_state || bulk && commonBlocks.contains(c_state);

            closed.add(wc);

            if (contains) {
                blocks++;
                world.setBlock(wc, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                world.destroyBlock(wc, false);

                if (isInsideBox(wc, pos)) {
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                if (0 == x && 0 == y && 0 == z) {
                                    continue;
                                }
                                final net.minecraft.core.BlockPos nextPos = wc.offset(x, y, z);
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

        return InteractionResult.sidedSuccess(world.isClientSide());
    }

    private boolean isInsideBox(net.minecraft.core.BlockPos pos, BlockPos origin) {
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
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.STONE);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.DIRT);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.GRASS_BLOCK);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.COBBLESTONE);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.ANDESITE);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.GRANITE);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.DIORITE);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.GRAVEL);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.SANDSTONE);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.NETHERRACK);
            COMMON_BLOCKS.add(net.minecraft.world.level.block.Blocks.WATER);
            COMMON_BLOCKS.add(Blocks.LAVA);

            COMMON_BLOCKS.addAll(BlockTags.LEAVES.getValues());
            COMMON_BLOCKS.addAll(BlockTags.SAND.getValues());
            COMMON_BLOCKS.addAll(BlockTags.LOGS.getValues());
        }

        return COMMON_BLOCKS;
    }
}
