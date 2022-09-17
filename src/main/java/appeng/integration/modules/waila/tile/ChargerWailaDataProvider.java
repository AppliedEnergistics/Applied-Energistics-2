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


import appeng.core.localization.WailaText;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import appeng.tile.misc.TileCharger;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;


/**
 * Charger provider for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class ChargerWailaDataProvider extends BaseWailaDataProvider {
    /**
     * Displays the holding item and its tooltip
     *
     * @param itemStack      stack of charger
     * @param currentToolTip unmodified tooltip
     * @param accessor       wrapper information
     * @param config         config option
     * @return modified tooltip
     */
    @Override
    public List<String> getWailaBody(@Nonnull final ItemStack itemStack, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileCharger) {
            final TileCharger charger = (TileCharger) te;
            final IItemHandler chargerInventory = charger.getInternalInventory();
            final ItemStack chargingItem = chargerInventory.getStackInSlot(0);

            if (!chargingItem.isEmpty()) {
                final String currentInventory = chargingItem.getDisplayName();
                final EntityPlayer player = accessor.getPlayer();

                currentToolTip.add(WailaText.Contains + ": " + currentInventory);
                ITooltipFlag.TooltipFlags tooltipFlag = Minecraft
                        .getMinecraft().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
                chargingItem.getItem().addInformation(chargingItem, player.world, currentToolTip, tooltipFlag);
            }
        }

        return currentToolTip;
    }
}
