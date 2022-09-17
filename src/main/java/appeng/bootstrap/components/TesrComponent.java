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


import appeng.tile.AEBaseTile;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;


/**
 * Registers a TESR for a given tile entity class.
 *
 * @param <T>
 */
// public class TesrComponent<T extends AEBaseTile> implements ModelRegComponent
public class TesrComponent<T extends AEBaseTile> implements IPreInitComponent {

    private final Class<T> tileEntityClass;

    private final TileEntitySpecialRenderer<? super T> tesr;

    public TesrComponent(Class<T> tileEntityClass, TileEntitySpecialRenderer<? super T> tesr) {
        this.tileEntityClass = tileEntityClass;
        this.tesr = tesr;
    }

    @Override
    // public void modelReg( Side side )
    public void preInitialize(Side side) {
        ClientRegistry.bindTileEntitySpecialRenderer(this.tileEntityClass, this.tesr);
    }
}
