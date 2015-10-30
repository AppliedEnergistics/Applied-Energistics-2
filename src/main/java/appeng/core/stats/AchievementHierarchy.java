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


/**
 * Hierarchy of the AE2 achievements
 *
 * @author thatsIch
 * @since rv2
 */
public class AchievementHierarchy
{
	/**
	 * Setup hierarchy through assigning parents.
	 */
	void registerAchievementHierarchy()
	{
		Achievements.Presses.setParent( Achievements.Compass );

		Achievements.Fluix.setParent( Achievements.ChargedQuartz );

		Achievements.Charger.setParent( Achievements.Fluix );

		Achievements.CrystalGrowthAccelerator.setParent( Achievements.Charger );

		Achievements.GlassCable.setParent( Achievements.Charger );

		Achievements.SpatialIOExplorer.setParent( Achievements.SpatialIO );

		Achievements.IOPort.setParent( Achievements.StorageCell );

		Achievements.PatternTerminal.setParent( Achievements.CraftingTerminal );

		Achievements.Controller.setParent( Achievements.Networking1 );

		Achievements.Networking2.setParent( Achievements.Controller );

		Achievements.Networking3.setParent( Achievements.Networking2 );

		Achievements.P2P.setParent( Achievements.Controller );

		Achievements.Recursive.setParent( Achievements.Controller );
	}
}
