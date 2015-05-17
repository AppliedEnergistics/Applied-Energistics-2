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

package appeng.worldgen.meteorite;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;


public final class MeteoriteBlockPutter
{
	public final boolean put( IMeteoriteWorld w, int i, int j, int k, Block blk )
	{
		Block original = w.getBlock( i, j, k );

		if( original == Blocks.bedrock || original == blk )
		{
			return false;
		}

		w.setBlock( i, j, k, blk );
		return true;
	}

	public final void put( IMeteoriteWorld w, int i, int j, int k, Block blk, int meta )
	{
		if( w.getBlock( i, j, k ) == Blocks.bedrock )
		{
			return;
		}

		w.setBlock( i, j, k, blk, meta, 3 );
	}
}
