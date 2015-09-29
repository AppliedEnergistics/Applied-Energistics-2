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

package appeng.decorative.solid;


import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.helpers.MetaRotation;


// TODO Quartz Rotation.
public class QuartzPillarBlock extends AEBaseBlock implements IOrientableBlock
{

	public QuartzPillarBlock()
	{
		super( Material.rock );
		this.setFeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

	@Override
	public int getMetaFromState(
			IBlockState state )
	{
		return 0;
	}
	
	@Override
	public IBlockState getStateFromMeta(
			int meta )
	{
		return getDefaultState();
	}
	
	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[]{ AXIS_ORIENTATION };
	}

	@Override
	public boolean usesMetadata()
	{
		return true;
	}

	@Override
	public IOrientable getOrientable( final IBlockAccess w,BlockPos pos )
	{
		return new MetaRotation( w, pos,false );
	}
	
}
