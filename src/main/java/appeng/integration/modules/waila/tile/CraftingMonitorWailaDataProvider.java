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


import appeng.api.storage.data.IAEItemStack;
import appeng.core.localization.WailaText;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import appeng.tile.crafting.TileCraftingMonitorTile;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.List;


/**
 * Crafting-monitor provider for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class CraftingMonitorWailaDataProvider extends BaseWailaDataProvider {
    /**
     * Displays the item currently crafted by the CPU cluster
     *
     * @param itemStack      stack of crafting monitor
     * @param currentToolTip unmodified tooltip
     * @param accessor       information wrapper
     * @param config         config option
     * @return modified tooltip
     */
    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileCraftingMonitorTile) {
            final TileCraftingMonitorTile monitor = (TileCraftingMonitorTile) te;
            final IAEItemStack displayStack = monitor.getJobProgress();

            if (displayStack != null) {
                final String currentCrafting = displayStack.asItemStackRepresentation().getDisplayName();

                currentToolTip.add(WailaText.Crafting.getLocal() + ": " + currentCrafting);
            }
        }

        return currentToolTip;
    }
}
