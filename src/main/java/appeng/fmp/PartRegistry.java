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

package appeng.fmp;


import appeng.block.AEBaseBlock;
import appeng.block.misc.BlockQuartzTorch;
import appeng.block.networking.BlockCableBus;
import appeng.core.Api;
import codechicken.multipart.TMultiPart;
import net.minecraft.block.Block;

import javax.annotation.Nullable;


public enum PartRegistry
{
	QuartzTorchPart( "ae2_torch", BlockQuartzTorch.class, QuartzTorchPart.class ),
	CableBusPart( "ae2_cablebus", BlockCableBus.class, CableBusPart.class );

	private final String name;
	private final Class<? extends AEBaseBlock> blk;
	private final Class<? extends TMultiPart> part;

	PartRegistry( final String name, final Class<? extends AEBaseBlock> blk, final Class<? extends TMultiPart> part )
	{
		this.name = name;
		this.blk = blk;
		this.part = part;
	}

	@Nullable
	public static TMultiPart getPartByBlock( final Block block, final int meta )
	{
		for( final PartRegistry pr : values() )
		{
			if( pr.blk.isInstance( block ) )
			{
				return pr.construct( meta );
			}
		}
		return null;
	}

	public TMultiPart construct( final int meta )
	{
		try
		{
			if( this == CableBusPart )
			{
				return (TMultiPart) Api.INSTANCE.partHelper().getCombinedInstance( this.part.getName() ).newInstance();
			}
			else
			{
				return this.part.getConstructor( int.class ).newInstance( meta );
			}
		}
		catch( final Throwable t )
		{
			throw new IllegalStateException( t );
		}
	}

	public String getName()
	{
		return this.name;
	}
}
