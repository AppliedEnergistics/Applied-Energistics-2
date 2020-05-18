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

package appeng.decorative;


import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;


public abstract class AEBaseStairBlock extends StairsBlock
{

	protected AEBaseStairBlock( final Block block, final String type )
	{
		super( block.getDefaultState() );

		Preconditions.checkNotNull( block );
		Preconditions.checkNotNull( block.getTranslationKey() );
		Preconditions.checkArgument( block.getTranslationKey().length() > 0 );

		this.setUnlocalizedName( "stair." + type );
		this.setLightOpacity( 0 );
	}

	@Override
	public String toString()
	{
		String regName = this.getRegistryName() != null ? this.getRegistryName().getResourcePath() : "unregistered";
		return this.getClass().getSimpleName() + "[" + regName + "]";
	}

}
