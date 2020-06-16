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

package appeng.bootstrap.components;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;

import appeng.bootstrap.IModelRegistry;

/**
 * Registers a custom state mapper for a given block.
 */
public class StateMapperComponent implements IModelRegistrationComponent {

// FIXME	private final Block block;

// FIXME	private final IStateMapper stateMapper;
// FIXME
// FIXME	public StateMapperComponent( Block block, IStateMapper stateMapper )
// FIXME	{
// FIXME		this.block = block;
// FIXME		this.stateMapper = stateMapper;
// FIXME	}

    @Override
    public void modelRegistration(Dist dist, IModelRegistry registry) {
        // FIXME registry.setCustomStateMapper( this.block, this.stateMapper );
        // FIXME if( this.stateMapper instanceof IResourceManagerReloadListener )
        // FIXME {
        // FIXME ( (IReloadableResourceManager)
        // Minecraft.getInstance().getResourceManager() )
        // FIXME .registerReloadListener( (IResourceManagerReloadListener)
        // this.stateMapper );
        // FIXME }
    }
}
