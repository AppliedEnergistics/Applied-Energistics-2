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


import appeng.bootstrap.IModelRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.fml.relauncher.Side;


/**
 * Registers a custom state mapper for a given block.
 */
public class StateMapperComponent implements IModelRegistrationComponent {

    private final Block block;

    private final IStateMapper stateMapper;

    public StateMapperComponent(Block block, IStateMapper stateMapper) {
        this.block = block;
        this.stateMapper = stateMapper;
    }

    @Override
    public void modelRegistration(Side side, IModelRegistry registry) {
        registry.setCustomStateMapper(this.block, this.stateMapper);
        if (this.stateMapper instanceof IResourceManagerReloadListener) {
            ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .registerReloadListener((IResourceManagerReloadListener) this.stateMapper);
        }
    }
}
