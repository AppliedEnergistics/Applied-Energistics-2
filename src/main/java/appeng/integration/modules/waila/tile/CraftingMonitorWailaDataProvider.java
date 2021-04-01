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

import java.util.List;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.localization.WailaText;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import appeng.tile.crafting.CraftingMonitorTileEntity;

/**
 * Crafting-monitor provider for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class CraftingMonitorWailaDataProvider extends BaseWailaDataProvider {

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        final TileEntity te = accessor.getBlockEntity();
        if (te instanceof CraftingMonitorTileEntity) {
            final CraftingMonitorTileEntity monitor = (CraftingMonitorTileEntity) te;
            final IAEItemStack displayStack = monitor.getJobProgress();

            if (displayStack != null) {
                final ITextComponent currentCrafting = displayStack.asItemStackRepresentation().getName();

                tooltip.add(WailaText.Crafting.text().copyRaw().appendString(": ").append(currentCrafting));
            }
        }
    }
}
