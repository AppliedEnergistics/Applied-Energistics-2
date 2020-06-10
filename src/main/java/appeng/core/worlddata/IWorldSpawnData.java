/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.worlddata;


import java.util.Collection;

import net.minecraft.nbt.CompoundNBT;


/**
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
public interface IWorldSpawnData
{
	void setGenerated( int dim, int chunkX, int chunkZ );

	boolean hasGenerated( int dim, int chunkX, int chunkZ );

	boolean addNearByMeteorites( int dim, int chunkX, int chunkZ, CompoundNBT newData );

	Collection<CompoundNBT> getNearByMeteorites( int dim, int chunkX, int chunkZ );
}
