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

package appeng.integration.modules.waila.tile;

import alexiil.mc.lib.attributes.item.FixedItemInvView;
import appeng.core.localization.WailaText;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import appeng.tile.misc.ChargerBlockEntity;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Charger provider for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class ChargerWailaDataProvider extends BaseWailaDataProvider {

    @Override
    public void appendBody(List<Text> tooltip, IDataAccessor accessor, IPluginConfig config) {

        final BlockEntity te = accessor.getBlockEntity();
        if (te instanceof ChargerBlockEntity) {
            final ChargerBlockEntity charger = (ChargerBlockEntity) te;
            final FixedItemInvView chargerInventory = charger.getInternalInventory();
            final ItemStack chargingItem = chargerInventory.getInvStack(0);

            if (!chargingItem.isEmpty()) {
                final Text currentInventory = chargingItem.getName();
                final PlayerEntity player = accessor.getPlayer();

                tooltip.add(WailaText.Contains.text().copy().append(": ").append(currentInventory));
                TooltipContext tooltipFlag = MinecraftClient.getInstance().options.advancedItemTooltips
                        ? TooltipContext.Default.ADVANCED
                        : TooltipContext.Default.NORMAL;
                chargingItem.getItem().appendTooltip(chargingItem, player.world, tooltip, tooltipFlag);
            }
        }

    }
}
