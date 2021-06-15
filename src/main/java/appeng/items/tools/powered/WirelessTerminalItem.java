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

package appeng.items.tools.powered;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.api.definitions.ApiItems;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.ConfigManager;

public class WirelessTerminalItem extends AEBasePoweredItem implements IWirelessTermHandler {

    private static final String TAG_ENCRYPTION_KEY = "encryptionKey";

    public WirelessTerminalItem(Item.Properties props) {
        super(AEConfig.instance().getWirelessTerminalBattery(), props);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final PlayerEntity player, final Hand hand) {
        Api.instance().registries().wireless().openWirelessTerminalGui(player.getHeldItem(hand), w, player, hand);
        return new ActionResult<>(ActionResultType.func_233537_a_(w.isRemote()), player.getHeldItem(hand));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);

        if (getEncryptionKey(stack).isEmpty()) {
            lines.add(GuiText.Unlinked.text());
        } else {
            lines.add(GuiText.Linked.text());
        }
    }

    @Override
    public boolean canHandle(final ItemStack is) {
        return ApiItems.wirelessTerminal().isSameAs(is);
    }

    @Override
    public boolean usePower(final PlayerEntity player, final double amount, final ItemStack is) {
        return this.extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
    }

    @Override
    public boolean hasPower(final PlayerEntity player, final double amt, final ItemStack is) {
        return this.getAECurrentPower(is) >= amt;
    }

    @Override
    public IConfigManager getConfigManager(final ItemStack target) {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final CompoundNBT data = target.getOrCreateTag();
            manager.writeToNBT(data);
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(target.getOrCreateTag().copy());
        return out;
    }

    @Override
    public String getEncryptionKey(final ItemStack item) {
        final CompoundNBT tag = item.getTag();
        if (tag != null) {
            return tag.getString(TAG_ENCRYPTION_KEY);
        } else {
            return "";
        }
    }

    @Override
    public void setEncryptionKey(final ItemStack item, final String encKey, final String name) {
        final CompoundNBT tag = item.getOrCreateTag();
        tag.putString(TAG_ENCRYPTION_KEY, encKey);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
