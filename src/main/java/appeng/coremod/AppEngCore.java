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

package appeng.coremod;


import java.util.Map;

import javax.annotation.Nullable;

import net.minecraftforge.fml.relauncher.FMLRelaunchLog;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;


@MCVersion( "1.10.2" )
public final class AppEngCore implements IFMLLoadingPlugin
{

	public AppEngCore()
	{
		FMLRelaunchLog.info( "[AppEng] Core Init" );
	}

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] { "appeng.coremod.asm.ASMIntegration" };
	}

	@Override
	public String getModContainerClass()
	{
		return "appeng.coremod.AppEngModContainer";
	}

	@Nullable
	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData( final Map<String, Object> data )
	{

	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}


}
