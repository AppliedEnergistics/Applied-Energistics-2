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

package appeng.items.tools.quartz;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.items.AEBaseItem;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.QuartzKnifeMenu;
import appeng.menu.locator.MenuLocators;

public class QuartzCuttingKnifeItem extends AEBaseItem implements IMenuItem {
    private final RandomSource random = RandomSource.create();

    public QuartzCuttingKnifeItem(Item.Properties props, QuartzToolType type) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (!level.isClientSide() && player != null) {
            MenuOpener.open(QuartzKnifeMenu.TYPE, context.getPlayer(),
                    MenuLocators.forItemUseContext(context));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        if (!level.isClientSide()) {
            MenuOpener.open(QuartzKnifeMenu.TYPE, p, MenuLocators.forHand(p, hand));
        }
        p.swing(hand);
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                p.getItemInHand(hand));
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack damagedStack = itemStack.copy();
        if (damagedStack.hurt(1, random, null)) {
            return ItemStack.EMPTY;
        } else {
            return damagedStack;
        }
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public ItemMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new ItemMenuHost(player, inventorySlot, stack);
    }
}
