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

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import appeng.block.AEBaseBlockItem;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.util.InteractionUtil;
import appeng.util.Lazy;

/**
 * Item that allows uncrafting CPU parts by disassembling them back into the crafting unit and the extra item.
 */
public class CraftingBlockItem extends AEBaseBlockItem {
    /**
     * This can be retrieved when disassembling the crafting unit.
     */
    protected final Lazy<ItemLike> disassemblyExtra;

    public CraftingBlockItem(Block id, Item.Properties props, Lazy<ItemLike> disassemblyExtra) {
        super(id, props);
        this.disassemblyExtra = disassemblyExtra;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (AEConfig.instance().isDisassemblyCraftingEnabled() && InteractionUtil.isInAlternateUseMode(player)) {
            this.disassemble(player.getItemInHand(hand), player);
            return InteractionResultHolder.sidedSuccess(player.getMainHandItem(), level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    private void disassemble(ItemStack stack, Player player) {
        int itemCount = stack.getCount();
        stack.setCount(0);

        player.getInventory().placeItemBackInInventory(new ItemStack(AEBlocks.CRAFTING_UNIT, itemCount));
        player.getInventory().placeItemBackInInventory(new ItemStack(disassemblyExtra.get(), itemCount));
    }
}
