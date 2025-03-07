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

package appeng.block.crafting;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import appeng.block.AEBaseBlockItem;
import appeng.core.definitions.AEBlocks;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.util.InteractionUtil;

/**
 * Item that allows uncrafting CPU parts by disassembling them back into the crafting unit and the extra item.
 */
public class CraftingBlockItem extends AEBaseBlockItem {
    public CraftingBlockItem(Block id, Properties props) {
        super(id, props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            ItemStack stack = player.getItemInHand(hand);

            if (level instanceof ServerLevel serverLevel) {
                var removedUpgrade = CraftingUnitTransformRecipe.getRemovedUpgrade(serverLevel, getBlock());
                if (removedUpgrade.isEmpty()) {
                    return super.use(level, player, hand);
                }


                int itemCount = stack.getCount();
                player.setItemInHand(hand, ItemStack.EMPTY);

                var inv = player.getInventory();
                inv.placeItemBackInInventory(removedUpgrade.copyWithCount(removedUpgrade.getCount() * itemCount));
                // This is hard-coded, as this is always a base block.
                inv.placeItemBackInInventory(AEBlocks.CRAFTING_UNIT.stack(itemCount));
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.use(level, player, hand);
    }
}
