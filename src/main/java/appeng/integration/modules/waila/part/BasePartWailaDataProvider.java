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


import appeng.api.parts.IPart;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;


/**
 * Default implementation of {@link appeng.integration.modules.waila.part.IPartWailaDataProvider}
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public abstract class BasePartWailaDataProvider implements IPartWailaDataProvider {
    @Override
    public ItemStack getWailaStack(final IPart part, final IWailaConfigHandler config, final ItemStack partStack) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<String> getWailaHead(final IPart part, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        return currentToolTip;
    }

    @Override
    public List<String> getWailaBody(final IPart part, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        return currentToolTip;
    }

    @Override
    public List<String> getWailaTail(final IPart part, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, IPart part, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        return tag;
    }
}
