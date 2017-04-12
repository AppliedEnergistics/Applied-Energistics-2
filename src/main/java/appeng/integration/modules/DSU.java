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


import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.IDSU;
import appeng.integration.modules.helpers.MFRDSUHandler;
import appeng.integration.modules.helpers.MinefactoryReloadedDeepStorageUnit;
import net.minecraft.tileentity.TileEntity;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;


public class DSU implements IDSU, IIntegrationModule
{
	@Reflected
	public static DSU instance;

	@Reflected
	public DSU()
	{
		IntegrationHelper.testClassExistence( this, powercrystals.minefactoryreloaded.api.IDeepStorageUnit.class );
	}

	@Override
	public IMEInventory getDSU( final TileEntity te )
	{
		return new MinefactoryReloadedDeepStorageUnit( te );
	}

	@Override
	public boolean isDSU( final TileEntity te )
	{
		return te instanceof IDeepStorageUnit;
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new MFRDSUHandler() );
	}
}
