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

import net.minecraft.block.Block;

import codechicken.multipart.TMultiPart;

import appeng.block.AEBaseBlock;
import appeng.block.misc.BlockQuartzTorch;
import appeng.block.networking.BlockCableBus;
import appeng.core.Api;

public enum PartRegistry
{
	QuartzTorchPart("ae2_torch", BlockQuartzTorch.class, QuartzTorchPart.class), CableBusPart("ae2_cablebus", BlockCableBus.class, CableBusPart.class);

	final private String name;
	final private Class<? extends AEBaseBlock> blk;
	final private Class<? extends TMultiPart> part;

	public String getName()
	{
		return this.name;
	}

	PartRegistry( String name, Class<? extends AEBaseBlock> blk, Class<? extends TMultiPart> part ) {
		this.name = name;
		this.blk = blk;
		this.part = part;
	}

	public TMultiPart construct(int meta)
	{
		try
		{
			if ( this == CableBusPart )
				return (TMultiPart) Api.INSTANCE.partHelper.getCombinedInstance( this.part.getName() ).newInstance();
			else
				return this.part.getConstructor( int.class ).newInstance( meta );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}
	}

	public static String getPartName(TMultiPart part)
	{
		Class c = part.getClass();
		for (PartRegistry pr : values())
		{
			if ( pr.equals( c ) )
			{
				return pr.name;
			}
		}
		throw new RuntimeException( "Invalid PartName" );
	}

	public static TMultiPart getPartByBlock(Block block, int meta)
	{
		for (PartRegistry pr : values())
		{
			if ( pr.blk.isInstance( block ) )
			{
				return pr.construct( meta );
			}
		}
		return null;
	}

	public static boolean isPart(Block block)
	{
		for (PartRegistry pr : values())
		{
			if ( pr.blk.isInstance( block ) )
			{
				return true;
			}
		}
		return false;
	}
}
