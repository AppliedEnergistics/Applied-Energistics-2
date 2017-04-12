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

package appeng.core.features;


import appeng.api.definitions.ITileDefinition;
import appeng.block.AEBaseTileBlock;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.minecraft.tileentity.TileEntity;


public final class TileDefinition extends BlockDefinition implements ITileDefinition
{
	private static final TileEntityTransformer TILEENTITY_TRANSFORMER = new TileEntityTransformer();
	private final Optional<AEBaseTileBlock> block;

	public TileDefinition( final AEBaseTileBlock block, final ActivityState state )
	{
		super( block, state );

		Preconditions.checkNotNull( block );
		Preconditions.checkNotNull( state );
		Preconditions.checkNotNull( block.getTileEntityClass() );

		if( state == ActivityState.Enabled )
		{
			this.block = Optional.of( block );
		}
		else
		{
			this.block = Optional.absent();
		}
	}

	@Override
	public Optional<? extends Class<? extends TileEntity>> maybeEntity()
	{
		return this.block.transform( TILEENTITY_TRANSFORMER );
	}

	private static class TileEntityTransformer implements Function<AEBaseTileBlock, Class<? extends TileEntity>>
	{
		@Override
		public Class<? extends TileEntity> apply( final AEBaseTileBlock input )
		{
			final Class<? extends TileEntity> entity = input.getTileEntityClass();

			return entity;
		}
	}
}
