/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.helper;

import java.util.EnumSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.security.IActionHost;
import appeng.me.helpers.IGridProxyable;

public interface IFluidInterfaceHost extends IActionHost, IGridProxyable, IUpgradeableHost {
    DualityFluidInterface getDualityFluidInterface();

    EnumSet<Direction> getTargets();

    TileEntity getTileEntity();

    @Override
    void saveChanges();
}
