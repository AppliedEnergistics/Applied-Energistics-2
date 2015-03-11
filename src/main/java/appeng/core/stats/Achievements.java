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
	Compass( -2, -4, AEApi.instance().definitions().blocks().skyCompass(), AchievementType.Craft ),

	// done
	Presses( -2, -2, AEApi.instance().definitions().materials().logicProcessorPress(), AchievementType.Custom ),

	// done
	SpatialIO( -4, -4, AEApi.instance().definitions().blocks().spatialIOPort(), AchievementType.Craft ),

	// done
	SpatialIOExplorer( -4, -2, AEApi.instance().definitions().items().spatialCell128(), AchievementType.Custom ),

	// done
	StorageCell( -6, -4, AEApi.instance().definitions().items().cell64k(), AchievementType.CraftItem ),

	// done
	IOPort( -6, -2, AEApi.instance().definitions().blocks().iOPort(), AchievementType.Craft ),

	// done
	CraftingTerminal( -8, -4, AEApi.instance().definitions().parts().craftingTerminal(), AchievementType.Craft ),

	// done
	PatternTerminal( -8, -2, AEApi.instance().definitions().parts().patternTerminal(), AchievementType.Craft ),

	// done
	ChargedQuartz( 0, -4, AEApi.instance().definitions().materials().certusQuartzCrystalCharged(), AchievementType.Pickup ),

	// done
	Fluix( 0, -2, AEApi.instance().definitions().materials().fluixCrystal(), AchievementType.Pickup ),

	// done
	Charger( 0, 0, AEApi.instance().definitions().blocks().charger(), AchievementType.Craft ),

	// done
	CrystalGrowthAccelerator( -2, 0, AEApi.instance().definitions().blocks().quartzGrowthAccelerator(), AchievementType.Craft ),

	// done
	GlassCable( 2, 0, AEApi.instance().definitions().parts().cableGlass(), AchievementType.Craft ),

	// done
	Networking1( 4, -6, AEApi.instance().definitions().parts().cableCovered(), AchievementType.Custom ),

	// done
	Controller( 4, -4, AEApi.instance().definitions().blocks().controller(), AchievementType.Craft ),

	// done
	Networking2( 4, 0, AEApi.instance().definitions().parts().cableSmart(), AchievementType.Custom ),

	// done
	Networking3( 4, 2, AEApi.instance().definitions().parts().cableDense(), AchievementType.Custom ),

	// done
	P2P( 2, -2, AEApi.instance().definitions().parts().p2PTunnelME(), AchievementType.Craft ),

	// done
	Recursive( 6, -2, AEApi.instance().definitions().blocks().iface(), AchievementType.Craft ),

	// done
	CraftingCPU( 6, 0, AEApi.instance().definitions().blocks().craftingStorage64k(), AchievementType.CraftItem ),

	// done
	Facade( 6, 2, AEApi.instance().definitions().items().facade(), AchievementType.CraftItem ),

	// done
	NetworkTool( 8, 0, AEApi.instance().definitions().items().networkTool(), AchievementType.Craft ),

	// done
	PortableCell( 8, 2, AEApi.instance().definitions().items().portableCell(), AchievementType.Craft ),

	// done
	StorageBus( 10, 0, AEApi.instance().definitions().parts().storageBus(), AchievementType.Craft ),

	// done
	QNB( 10, 2, AEApi.instance().definitions().blocks().quantumLink(), AchievementType.Craft );

	public final ItemStack stack;
	public final AchievementType type;
	private final int x, y;

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

	private Achievements( int x, int y, AEColoredItemDefinition which, AchievementType type )
	{
		this.stack = (which != null) ? which.stack( AEColor.Transparent, 1 ) : null;
		this.type = type;
		this.x = x;
		this.y = y;
	}

	private Achievements( int x, int y, AEItemDefinition which, AchievementType type )
	{
		this.stack = (which != null) ? which.stack( 1 ) : null;
		this.type = type;
		this.x = x;
		this.y = y;
	}

	private Achievements( int x, int y, ItemStack which, AchievementType type )
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
