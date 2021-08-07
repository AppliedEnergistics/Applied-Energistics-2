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

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.QuartzKnifeContainer;
import appeng.items.AEBaseItem;
import appeng.items.contents.QuartzKnifeObj;
import appeng.util.Platform;

public class QuartzCuttingKnifeItem extends AEBaseItem implements IGuiItem {
    private final QuartzToolType type;
    private final Random random = new Random();

    public QuartzCuttingKnifeItem(Item.Properties props, final QuartzToolType type) {
        super(props);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (!level.isClientSide() && player != null) {
            ContainerOpener.openContainer(QuartzKnifeContainer.TYPE, context.getPlayer(),
                    ContainerLocator.forItemUseContext(context));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player p, final InteractionHand hand) {
        if (!level.isClientSide()) {
            ContainerOpener.openContainer(QuartzKnifeContainer.TYPE, p, ContainerLocator.forHand(p, hand));
        }
        p.swing(hand);
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                p.getItemInHand(hand));
    }

    @Override
    public boolean isValidRepairItem(final ItemStack a, final ItemStack b) {
        return Platform.canRepair(this.type, a, b);
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack) {
        ItemStack damagedStack = itemStack.copy();
        if (damagedStack.hurt(1, random, null)) {
            return ItemStack.EMPTY;
        } else {
            return damagedStack;
        }
    }

    @Override
    public boolean hasContainerItem(final ItemStack stack) {
        return true;
    }

    @Override
    public IGuiItemObject getGuiObject(final ItemStack is, int playerInventorySlot, final Level level,
            final BlockPos pos) {
        return new QuartzKnifeObj(is);
    }
}
