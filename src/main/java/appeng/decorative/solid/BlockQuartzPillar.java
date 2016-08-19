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
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.helpers.MetaRotation;


public class BlockQuartzPillar extends AEBaseBlock implements IOrientableBlock
{
	public static final PropertyEnum<EnumFacing.Axis> AXIS_ORIENTATION = PropertyEnum.create( "axis", EnumFacing.Axis.class );

	public BlockQuartzPillar()
	{
		super( Material.ROCK );
		this.setFeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

	@Override
	public int getMetaFromState( final IBlockState state )
	{
		return state.getValue( AXIS_ORIENTATION ).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta( final int meta )
	{
		// Simply use the ordinal here
		EnumFacing.Axis axis = EnumFacing.Axis.values()[meta];
		return this.getDefaultState().withProperty( AXIS_ORIENTATION, axis );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { AXIS_ORIENTATION };
	}

	@Override
	public boolean usesMetadata()
	{
		return true;
	}

	@Override
	public IOrientable getOrientable( final IBlockAccess w, final BlockPos pos )
	{
		return new MetaRotation( w, pos, null );
	}

}
