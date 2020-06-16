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

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import mcp.mobius.waila.api.*;

import appeng.entity.EntityGrowingCrystal;
import appeng.items.misc.ItemCrystalSeed;
import appeng.tile.AEBaseTile;

@WailaPlugin
public class WailaModule implements IWailaPlugin {

    public void register(final IRegistrar registrar) {
        final PartWailaDataProvider partHost = new PartWailaDataProvider();

        registrar.registerStackProvider(partHost, AEBaseTile.class);
        registrar.registerComponentProvider(partHost, TooltipPosition.BODY, AEBaseTile.class);
        registrar.registerBlockDataProvider(partHost, AEBaseTile.class);

        final TileWailaDataProvider tile = new TileWailaDataProvider();
        registrar.registerComponentProvider(tile, TooltipPosition.BODY, AEBaseTile.class);
        registrar.registerBlockDataProvider(tile, AEBaseTile.class);
    }

}
