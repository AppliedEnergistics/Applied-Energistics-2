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

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import appeng.block.AEBaseBlockItem;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.util.InteractionUtil;

/**
 * Item that allows uncrafting CPU parts by disassembling them back into the crafting unit and the extra item.
 */
public class CraftingBlockItem extends AEBaseBlockItem {
    public CraftingBlockItem(Block id, Item.Properties props) {
        super(id, props);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player,
            @NotNull InteractionHand hand) {
        return InteractionUtil.isInAlternateUseMode(player) && disassemble(player.getItemInHand(hand), level, player)
                ? InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide())
                : super.use(level, player, hand);
    }

    private boolean disassemble(ItemStack stack, Level level, Player player) {
        if (!AEConfig.instance().isDisassemblyCraftingEnabled()) {
            return false;
        }

        var recipe = level.getRecipeManager().byKey(getRecipeId());

        if (recipe.isEmpty()) {
            AELog.debug("Cannot disassemble crafting block because its crafting recipe doesn't exist: %s",
                    getRecipeId());
            return false;
        }

        if (level.isClientSide()) {
            return true;
        }

        var inventory = player.getInventory();

        if (inventory.getSelected() != stack) {
            return false;
        }

        inventory.setItem(inventory.selected, ItemStack.EMPTY);

        for (var ingredient : recipe.get().getIngredients()) {
            var ingredientStack = new ItemStack(ingredient.getItems()[0].getItem(), inventory.getSelected().getCount());
            inventory.placeItemBackInInventory(ingredientStack);
        }

        return true;
    }

    protected ResourceLocation getRecipeId() {
        return AppEng.makeId("network/crafting/" + BuiltInRegistries.ITEM.getKey(this).getPath());
    }
}
