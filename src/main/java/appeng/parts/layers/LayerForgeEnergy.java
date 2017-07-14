/*
 *
 *  * This file is part of Applied Energistics 2.
 *  * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
 *  *
 *  * Applied Energistics 2 is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * Applied Energistics 2 is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 *
 */

package appeng.parts.layers;

import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author GuntherDW
 */
public class LayerForgeEnergy extends LayerBase implements ICapabilityProvider, IEnergyStorage
{

    private final EnumFacing facing;

    public LayerForgeEnergy()
    {
        this.facing = null;
    }

    public LayerForgeEnergy( EnumFacing facing )
    {
        this.facing = facing;
    }

    @Override
    public int receiveEnergy( int maxReceive, boolean simulate )
    {
        if( facing != null)
        {
            IPart part = getPart( facing );
            if( part instanceof IEnergyStorage )
            {
                return ( (IEnergyStorage) part ).receiveEnergy( maxReceive, simulate );
            }
        }

        return 0;
    }

    @Override
    public int extractEnergy( int maxExtract, boolean simulate )
    {
        if( facing != null)
        {
            IPart part = getPart( facing );
            if( part instanceof IEnergyStorage )
            {
                return ( (IEnergyStorage) part ).extractEnergy( maxExtract, simulate );
            }
        }

        return 0;
    }

    @Override
    public int getEnergyStored()
    {
        if( facing != null)
        {
            IPart part = getPart( facing );
            if( part instanceof IEnergyStorage )
            {
                return ( (IEnergyStorage) part ).getEnergyStored();
            }
        }

        return 0;
    }

    @Override
    public int getMaxEnergyStored()
    {
        if( facing != null)
        {
            IPart part = getPart( facing );
            if( part instanceof IEnergyStorage )
            {
                return ( (IEnergyStorage) part ).getMaxEnergyStored();
            }
        }

        return 0;
    }

    @Override
    public boolean canExtract()
    {
        if( facing != null)
        {
            IPart part = getPart( facing );
            if( part instanceof IEnergyStorage )
            {
                return ( (IEnergyStorage) part ).canExtract();
            }
        }

        return false;
    }

    @Override
    public boolean canReceive()
    {
        if( facing != null)
        {
            IPart part = getPart( facing );
            if( part instanceof IEnergyStorage )
            {
                return ( (IEnergyStorage) part ).canReceive();
            }
        }

        return false;
    }
}
