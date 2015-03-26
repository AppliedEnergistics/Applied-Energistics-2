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

package appeng.core.stats;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

import appeng.api.AEApi;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;


public enum Achievements
{

	// done
	Compass( -2, -4, AEApi.instance().blocks().blockSkyCompass, AchievementType.Craft ),

	// done
	Presses( -2, -2, AEApi.instance().materials().materialLogicProcessorPress, AchievementType.Custom ),

	// done
	SpatialIO( -4, -4, AEApi.instance().blocks().blockSpatialIOPort, AchievementType.Craft ),

	// done
	SpatialIOExplorer( -4, -2, AEApi.instance().items().itemSpatialCell128, AchievementType.Custom ),

	// done
	StorageCell( -6, -4, AEApi.instance().items().itemCell64k, AchievementType.CraftItem ),

	// done
	IOPort( -6, -2, AEApi.instance().blocks().blockIOPort, AchievementType.Craft ),

	// done
	CraftingTerminal( -8, -4, AEApi.instance().parts().partCraftingTerminal, AchievementType.Craft ),

	// done
	PatternTerminal( -8, -2, AEApi.instance().parts().partPatternTerminal, AchievementType.Craft ),

	// done
	ChargedQuartz( 0, -4, AEApi.instance().materials().materialCertusQuartzCrystalCharged, AchievementType.Pickup ),

	// done
	Fluix( 0, -2, AEApi.instance().materials().materialFluixCrystal, AchievementType.Pickup ),

	// done
	Charger( 0, 0, AEApi.instance().blocks().blockCharger, AchievementType.Craft ),

	// done
	CrystalGrowthAccelerator( -2, 0, AEApi.instance().blocks().blockQuartzGrowthAccelerator, AchievementType.Craft ),

	// done
	GlassCable( 2, 0, AEApi.instance().parts().partCableGlass, AchievementType.Craft ),

	// done
	Networking1( 4, -6, AEApi.instance().parts().partCableCovered, AchievementType.Custom ),

	// done
	Controller( 4, -4, AEApi.instance().blocks().blockController, AchievementType.Craft ),

	// done
	Networking2( 4, 0, AEApi.instance().parts().partCableSmart, AchievementType.Custom ),

	// done
	Networking3( 4, 2, AEApi.instance().parts().partCableDense, AchievementType.Custom ),

	// done
	P2P( 2, -2, AEApi.instance().parts().partP2PTunnelME, AchievementType.Craft ),

	// done
	Recursive( 6, -2, AEApi.instance().blocks().blockInterface, AchievementType.Craft ),

	// done
	CraftingCPU( 6, 0, AEApi.instance().blocks().blockCraftingStorage64k, AchievementType.CraftItem ),

	// done
	Facade( 6, 2, AEApi.instance().items().itemFacade, AchievementType.CraftItem ),

	// done
	NetworkTool( 8, 0, AEApi.instance().items().itemNetworkTool, AchievementType.Craft ),

	// done
	PortableCell( 8, 2, AEApi.instance().items().itemPortableCell, AchievementType.Craft ),

	// done
	StorageBus( 10, 0, AEApi.instance().parts().partStorageBus, AchievementType.Craft ),

	// done
	QNB( 10, 2, AEApi.instance().blocks().blockQuantumLink, AchievementType.Craft );

	public final ItemStack stack;
	public final AchievementType type;
	private final int x;
	private final int y;

	private Achievement parent;
	private Achievement stat;

	public void setParent( Achievements parent )
	{
		this.parent = parent.getAchievement();
	}

	public Achievement getAchievement()
	{
		if ( this.stat == null && this.stack != null )
		{
			this.stat = new Achievement( "achievement.ae2." + this.name(), "ae2." + this.name(), this.x, this.y, this.stack, this.parent );
			this.stat.registerStat();
		}

		return this.stat;
	}

	Achievements( int x, int y, AEColoredItemDefinition which, AchievementType type )
	{
		this.stack = (which != null) ? which.stack( AEColor.Transparent, 1 ) : null;
		this.type = type;
		this.x = x;
		this.y = y;
	}

	Achievements( int x, int y, AEItemDefinition which, AchievementType type )
	{
		this.stack = (which != null) ? which.stack( 1 ) : null;
		this.type = type;
		this.x = x;
		this.y = y;
	}

	Achievements( int x, int y, ItemStack which, AchievementType type )
	{
		this.stack = which;
		this.type = type;
		this.x = x;
		this.y = y;
	}

	public void addToPlayer( EntityPlayer player )
	{
		player.addStat( this.getAchievement(), 1 );
	}

}
