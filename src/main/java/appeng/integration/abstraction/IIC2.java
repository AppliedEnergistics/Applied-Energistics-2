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

package appeng.integration.abstraction;


import appeng.integration.IIntegrationModule;
import appeng.integration.modules.ic2.IC2PowerSinkStub;
import appeng.tile.powersink.IExternalPowerSink;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;


public interface IIC2 extends IIntegrationModule {

    default void maceratorRecipe(ItemStack in, ItemStack out) {
    }

    /**
     * Create an IC2 power sink for the given external sink.
     */
    default IC2PowerSink createPowerSink(TileEntity tileEntity, IExternalPowerSink externalSink) {
        return IC2PowerSinkStub.INSTANCE;
    }

    class Stub extends IIntegrationModule.Stub implements IIC2 {

    }
}
