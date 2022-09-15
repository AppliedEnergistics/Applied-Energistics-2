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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.Upgrades;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.PlayerMessages;
import appeng.items.AEBaseItem;
import appeng.util.InteractionUtil;

public class UpgradeCardItem extends AEBaseItem {

    public UpgradeCardItem(Item.Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);

        var supportedBy = Upgrades.getTooltipLinesForCard(this);
        if (!supportedBy.isEmpty()) {
            lines.add(ButtonToolTips.SupportedBy.text());
            lines.addAll(supportedBy);
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
                final SelectedPart sp = ((IPartHost) te).selectPartWorld(context.getClickLocation());
                if (sp.part instanceof IUpgradeableObject) {
                    upgrades = ((IUpgradeableObject) sp.part).getUpgrades();
                }
            } else if (te instanceof IUpgradeableObject) {
                upgrades = ((IUpgradeableObject) te).getUpgrades();
            }

            if (upgrades != null && upgrades.size() > 0) {
                var heldStack = player.getItemInHand(hand);

                boolean isFull = true;
                for (int i = 0; i < upgrades.size(); i++) {
                    if (upgrades.getStackInSlot(i).isEmpty()) {
                        isFull = false;
                        break;
                    }
                }
                if (isFull) {
                    player.sendSystemMessage(PlayerMessages.MaxUpgradesInstalled.text());
                    return InteractionResult.FAIL;
                }

                var maxInstalled = upgrades.getMaxInstalled(heldStack.getItem());
                var installed = upgrades.getInstalledUpgrades(heldStack.getItem());
                if (maxInstalled <= 0) {
                    player.sendSystemMessage(PlayerMessages.UnsupportedUpgrade.text());
                    return InteractionResult.FAIL;
                } else if (installed >= maxInstalled) {
                    player.sendSystemMessage(PlayerMessages.MaxUpgradesOfTypeInstalled.text());
                    return InteractionResult.FAIL;
                }

                if (player.getCommandSenderWorld().isClientSide()) {
                    return InteractionResult.PASS;
                }

                player.setItemInHand(hand, upgrades.addItems(heldStack));
                return InteractionResult.sidedSuccess(player.getCommandSenderWorld().isClientSide());
            }
        }

        return super.onItemUseFirst(stack, context);
    }
}
