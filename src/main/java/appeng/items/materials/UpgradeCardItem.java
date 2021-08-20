/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.items.materials;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;

public class UpgradeCardItem extends AEBaseItem implements IUpgradeModule, AEToolItem {
    private final Upgrades cardType;

    public UpgradeCardItem(Item.Properties properties, Upgrades cardType) {
        super(properties);
        this.cardType = cardType;
    }

    @Override
    public Upgrades getType(final ItemStack itemstack) {
        return cardType;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);

        final Upgrades u = this.getType(stack);
        if (u != null) {
            lines.addAll(u.getTooltipLines());
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        if (player != null && InteractionUtil.isInAlternateUseMode(player)) {
            final BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
            IUpgradeInventory upgrades = null;

            if (te instanceof IPartHost) {
                final SelectedPart sp = ((IPartHost) te).selectPart(context.getClickLocation());
                if (sp.part instanceof IUpgradeableObject) {
                    upgrades = ((IUpgradeableObject) sp.part).getUpgrades();
                }
            } else if (te instanceof IUpgradeableObject) {
                upgrades = ((IUpgradeableObject) te).getUpgrades();
            }

            ItemStack heldStack = player.getItemInHand(hand);
            var u = IUpgradeModule.getTypeFromStack(heldStack);
            if (upgrades != null && u != null) {
                if (player.getCommandSenderWorld().isClientSide()) {
                    return InteractionResult.PASS;
                }

                player.setItemInHand(hand, upgrades.addItems(heldStack));
                return InteractionResult.sidedSuccess(player.getCommandSenderWorld().isClientSide());
            }
        }

        return InteractionResult.PASS;
    }
}
