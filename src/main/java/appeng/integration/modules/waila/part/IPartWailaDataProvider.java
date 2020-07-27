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

package appeng.integration.modules.waila.part;

import java.util.List;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;

import appeng.api.parts.IPart;

/**
 * An abstraction layer of the {@link IPartWailaDataProvider} for {@link IPart}.
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public interface IPartWailaDataProvider {
    ItemStack getStack(IPart part, IPluginConfig config, ItemStack partStack);

    void appendHead(IPart part, List<Text> tooltip, IDataAccessor accessor, IPluginConfig config);

    void appendBody(IPart part, List<Text> tooltip, IDataAccessor accessor, IPluginConfig config);

    void appendTail(IPart part, List<Text> tooltip, IDataAccessor accessor, IPluginConfig config);

    void appendServerData(ServerPlayerEntity player, IPart part, BlockEntity te, CompoundTag tag, World world,
            BlockPos pos);
}
