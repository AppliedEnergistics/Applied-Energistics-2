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

package appeng.client.gui.config;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

import appeng.core.AEConfig;
import appeng.core.AppEng;

public class AEConfigGui extends GuiConfig
{

	private static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = new ArrayList<IConfigElement>();

		for (String cat : AEConfig.instance.getCategoryNames())
		{
			if ( cat.equals( "versionchecker" ) )
				continue;

			if ( cat.equals( "settings" ) )
				continue;

			ConfigCategory cc = AEConfig.instance.getCategory( cat );

			if ( cc.isChild() )
				continue;

			ConfigElement ce = new ConfigElement( cc );
			list.add( ce );
		}

		return list;
	}

	public AEConfigGui(GuiScreen parent) {
		super( parent, getConfigElements(), AppEng.MOD_ID, false, false, GuiConfig.getAbridgedConfigPath( AEConfig.instance.getFilePath() ) );
	}

}
