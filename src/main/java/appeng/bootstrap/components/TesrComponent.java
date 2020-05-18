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


import java.util.function.Function;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import appeng.tile.AEBaseTile;


/**
 * Registers a TESR for a given tile entity class.
 *
 * @param <T>
 */
public class TesrComponent<T extends AEBaseTile> implements IPreInitComponent
{

	private final TileEntityType<T> tileEntityClass;

	private final Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<? super T>> ter;

	public TesrComponent( TileEntityType<T> tileEntityClass, Function<? super TileEntityRendererDispatcher, ? extends TileEntityRenderer<? super T>> ter )
	{
		this.tileEntityClass = tileEntityClass;
		this.ter = ter;
	}

	@Override
	// public void modelReg( Dist dist )
	public void preInitialize( Dist dist )
	{
		ClientRegistry.bindTileEntityRenderer( this.tileEntityClass, this.ter );
	}
}
