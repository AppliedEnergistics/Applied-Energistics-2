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

package appeng.integration.modules.waila;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;

import appeng.integration.modules.waila.tile.ChargerWailaDataProvider;
import appeng.integration.modules.waila.tile.CraftingMonitorWailaDataProvider;
import appeng.integration.modules.waila.tile.PowerStateWailaDataProvider;
import appeng.integration.modules.waila.tile.PowerStorageWailaDataProvider;

/**
 * Delegation provider for tiles through {@link mcp.mobius.waila.api.IComponentProvider}
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class TileWailaDataProvider implements IComponentProvider, IServerDataProvider<TileEntity> {
    /**
     * Contains all providers
     */
    private final List<BaseWailaDataProvider> providers;

    /**
     * Initializes the provider list with all wanted providers
     */
    public TileWailaDataProvider() {
        final BaseWailaDataProvider charger = new ChargerWailaDataProvider();
        final BaseWailaDataProvider energyCell = new PowerStorageWailaDataProvider();
        final BaseWailaDataProvider craftingBlock = new PowerStateWailaDataProvider();
        final BaseWailaDataProvider craftingMonitor = new CraftingMonitorWailaDataProvider();

        this.providers = Lists.newArrayList(charger, energyCell, craftingBlock, craftingMonitor);
    }

    @Override
    public ItemStack getStack(final IDataAccessor accessor, final IPluginConfig config) {
        return ItemStack.EMPTY;
    }

    @Override
    public void appendHead(List<ITextComponent> currentToolTip, final IDataAccessor accessor,
            final IPluginConfig config) {
        for (final BaseWailaDataProvider provider : this.providers) {
            provider.appendHead(currentToolTip, accessor, config);
        }
    }

    @Override
    public void appendBody(List<ITextComponent> currentToolTip, final IDataAccessor accessor,
            final IPluginConfig config) {
        for (final BaseWailaDataProvider provider : this.providers) {
            provider.appendBody(currentToolTip, accessor, config);
        }
    }

    @Override
    public void appendTail(List<ITextComponent> currentToolTip, final IDataAccessor accessor,
            final IPluginConfig config) {
        for (final BaseWailaDataProvider provider : this.providers) {
            provider.appendTail(currentToolTip, accessor, config);
        }
    }

    @Override
    public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {

        for (final BaseWailaDataProvider provider : this.providers) {
            provider.appendServerData(tag, player, world, te);
        }
    }
}
