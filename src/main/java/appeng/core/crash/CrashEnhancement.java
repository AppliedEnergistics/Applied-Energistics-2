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

package appeng.core.crash;


import cpw.mods.fml.common.ICrashCallable;

import appeng.core.AEConfig;
import appeng.integration.IntegrationRegistry;


public class CrashEnhancement implements ICrashCallable
{

	private final String name;
	private final String value;

	private final String ModVersion = AEConfig.CHANNEL + ' ' + AEConfig.VERSION + " for Forge " + // WHAT?
	net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
			+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
			+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
			+ net.minecraftforge.common.ForgeVersion.buildVersion;

	public CrashEnhancement( CrashInfo Output )
	{

		if ( Output == CrashInfo.MOD_VERSION )
		{
			this.name = "AE2 Version";
			this.value = this.ModVersion;
		}
		else if ( Output == CrashInfo.INTEGRATION )
		{
			this.name = "AE2 Integration";
			this.value = IntegrationRegistry.INSTANCE.getStatus();
		}
		else
		{
			this.name = "AE2_UNKNOWN";
			this.value = "UNKNOWN_VALUE";
		}
	}

	@Override
	public String call() throws Exception
	{
		return this.value;
	}

	@Override
	public String getLabel()
	{
		return this.name;
	}

}
