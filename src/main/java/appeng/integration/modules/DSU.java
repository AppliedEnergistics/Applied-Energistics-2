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

package appeng.integration.modules;


import net.minecraft.tileentity.TileEntity;

import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IDSU;
import appeng.integration.modules.helpers.MFRDSUHandler;
import appeng.integration.modules.helpers.MinefactoryReloadedDeepStorageUnit;


public class DSU extends BaseModule implements IDSU
{
	public static DSU instance;

	@Override
	public IMEInventory getDSU( TileEntity te )
	{
		return new MinefactoryReloadedDeepStorageUnit( te );
	}

	@Override
	public boolean isDSU( TileEntity te )
	{
		return te instanceof IDeepStorageUnit;
	}

	@Override
	public void preInit()
	{

	}

	@Override
	public void init()
	{
		this.testClassExistence( IDeepStorageUnit.class );
	}

	@Override
	public void postInit()
	{
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new MFRDSUHandler() );
	}
}
