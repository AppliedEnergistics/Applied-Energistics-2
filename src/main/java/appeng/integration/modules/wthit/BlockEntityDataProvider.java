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

package appeng.integration.modules.wthit;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;

import appeng.integration.modules.wthit.tile.ChargerDataProvider;
import appeng.integration.modules.wthit.tile.CraftingMonitorDataProvider;
import appeng.integration.modules.wthit.tile.DebugDataProvider;
import appeng.integration.modules.wthit.tile.GridNodeStateDataProvider;
import appeng.integration.modules.wthit.tile.PowerStorageDataProvider;

/**
 * Delegation provider for tiles through {@link mcp.mobius.waila.api.IBlockComponentProvider}
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class BlockEntityDataProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {
    /**
     * Contains all providers
     */
    private final List<BaseDataProvider> providers;

    /**
     * Initializes the provider list with all wanted providers
     */
    public BlockEntityDataProvider() {
        this.providers = List.of(
                new ChargerDataProvider(),
                new PowerStorageDataProvider(),
                new GridNodeStateDataProvider(),
                new CraftingMonitorDataProvider(),
                new DebugDataProvider());
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        for (var provider : providers) {
            provider.appendBody(tooltip, accessor, config);
        }
    }

    @Override
    public void appendServerData(CompoundTag data, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
        for (var provider : providers) {
            provider.appendServerData(data, accessor.getPlayer(), accessor.getWorld(), accessor.getTarget());
        }
    }

}
