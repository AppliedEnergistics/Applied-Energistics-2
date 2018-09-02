/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.integration.abstraction;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Optional;

import team.chisel.ctm.api.IFacade;


/**
 * Neat abstraction class for All the IFacade interfaces.
 *
 * @author covers1624
 */
@Optional.Interface( iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api" )
public interface IAEFacade extends IFacade
{

	IBlockState getFacadeState( IBlockAccess world, BlockPos pos, EnumFacing side );

	@Nonnull
	@Override
	@Optional.Method( modid = "ctm-api" )
	default IBlockState getFacade( @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side, @Nonnull BlockPos connection )
	{
		return getFacadeState( world, pos, side );
	}

	@Nonnull
	@Override
	@Optional.Method( modid = "ctm-api" )
	default IBlockState getFacade( @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side )
	{
		return getFacadeState( world, pos, side );
	}
}
