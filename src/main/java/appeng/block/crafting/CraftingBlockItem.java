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

import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.recipes.game.CraftingUnitUpgradeRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import appeng.block.AEBaseBlockItem;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.util.InteractionUtil;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Item that allows uncrafting CPU parts by disassembling them back into the crafting unit and the extra item.
 */
public class CraftingBlockItem extends AEBaseBlockItem {
    /**
     * This can be retrieved when disassembling the crafting unit.
     */
    @Deprecated(forRemoval = true, since = "1.21.1")
    protected final Supplier<ItemLike> disassemblyExtra;

    @Deprecated(forRemoval = true, since = "1.21.1")
    public CraftingBlockItem(Block id, Properties props, Supplier<ItemLike> disassemblyExtra) {
        super(id, props);
        this.disassemblyExtra = disassemblyExtra;
    }

    public CraftingBlockItem(Block id, Properties props) {
        super(id, props);
        this.disassemblyExtra = null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (AEConfig.instance().isDisassemblyCraftingEnabled() && InteractionUtil.isInAlternateUseMode(player)) {
            ItemStack stack = player.getItemInHand(hand);
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

            if (itemId == BuiltInRegistries.ITEM.getDefaultKey()) {
                AELog.debug("Cannot disassemble crafting block because its item is unregistered?");
                return super.use(level, player, hand);
            }

            var recipe = CraftingUnitUpgradeRecipe.getDisassemblyRecipe(level, AppEng.makeId("upgrade/" + itemId.getPath()), itemId);
            if (recipe == null) return super.use(level, player, hand);

            int itemCount = stack.getCount();
            player.setItemInHand(hand, ItemStack.EMPTY);

            var inv = player.getInventory();
            if (recipe.useLootTable()) {
                // Because this is a loot-table, and there might be chance-dependent conditions,
                // we need to roll the loot table for each item.
                LootParams params = new LootParams.Builder((ServerLevel) level).create(LootContextParamSets.EMPTY);
                for (int i = 0; i < itemCount; i++) {
                    recipe.getDisassemblyLoot(level, params).forEach(inv::placeItemBackInInventory);
                }
            } else {
                recipe.getDisassemblyItems().forEach(item -> inv.placeItemBackInInventory(item.copyWithCount(item.getCount() * itemCount)));
            }

            // This is hard-coded, as this is always a base block.
            inv.placeItemBackInInventory(AEBlocks.CRAFTING_UNIT.stack(itemCount));

            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    private void disassemble(ItemStack stack, Player player) {
    }
}
