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


import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinition;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.IRandomDisplayTick;
import codechicken.multipart.minecraft.McBlockPart;
import codechicken.multipart.minecraft.McSidedMetaPart;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;


public class QuartzTorchPart extends McSidedMetaPart implements IRandomDisplayTick
{

	public QuartzTorchPart()
	{
		this( ForgeDirection.DOWN.ordinal() );
	}

	public QuartzTorchPart( final int meta )
	{
		super( meta );
	}

	public static McBlockPart placement( final World world, BlockCoord pos, final int side )
	{
		pos = pos.copy().offset( side );
		if( !world.isSideSolid( pos.x, pos.y, pos.z, ForgeDirection.getOrientation( side ) ) )
		{
			return null;
		}

		return new QuartzTorchPart( side );
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	@Override
	public String getType()
	{
		return PartRegistry.QuartzTorchPart.getName();
	}

	@Override
	public Cuboid6 getBounds()
	{
		return this.getBounds( this.meta );
	}

	private Cuboid6 getBounds( final int meta )
	{
		final ForgeDirection up = ForgeDirection.getOrientation( meta );
		final double xOff = -0.3 * up.offsetX;
		final double yOff = -0.3 * up.offsetY;
		final double zOff = -0.3 * up.offsetZ;
		return new Cuboid6( xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7 );
	}

	@Override
	public int sideForMeta( final int meta )
	{
		return ForgeDirection.getOrientation( meta ).getOpposite().ordinal();
	}

	@Override
	public void randomDisplayTick( final Random r )
	{
		this.getBlock().randomDisplayTick( this.world(), this.x(), this.y(), this.z(), r );
	}

	@Override
	public Block getBlock()
	{
		for( final Block torchBlock : AEApi.instance().definitions().blocks().quartzTorch().maybeBlock().asSet() )
		{
			return torchBlock;
		}

		throw new MissingDefinition( "Tried to retrieve a quartz torch, even though it is disabled." );
	}
}