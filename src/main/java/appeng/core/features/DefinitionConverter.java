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

package appeng.core.features;


import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.ITileDefinition;
import appeng.api.util.AEItemDefinition;


/**
 * @deprecated
 */
@Deprecated
public final class DefinitionConverter
{
	public AEItemDefinition of( ITileDefinition definition )
	{
		return new AETile( definition );
	}

	public AEItemDefinition of( IBlockDefinition definition )
	{
		return new AEBlock( definition );
	}

	public AEItemDefinition of( IItemDefinition definition )
	{
		return new AEItem( definition );
	}

	public AEItemDefinition of( IComparableDefinition definition )
	{
		return new AEComparable( definition );
	}

	private static class AEComparable implements AEItemDefinition
	{
		private final IComparableDefinition definition;

		public AEComparable( IComparableDefinition definition )
		{
			this.definition = definition;
		}

		@Nullable
		@Override
		public Block block()
		{
			return null;
		}

		@Nullable
		@Override
		public Item item()
		{
			return null;
		}

		@Nullable
		@Override
		public Class<? extends TileEntity> entity()
		{
			return null;
		}

		@Nullable
		@Override
		public ItemStack stack( int stackSize )
		{
			return null;
		}

		@Override
		public final boolean sameAsStack( ItemStack comparableItem )
		{
			return this.definition.isSameAs( comparableItem );
		}

		@Override
		public boolean sameAsBlock( IBlockAccess world, int x, int y, int z )
		{
			return false;
		}
	}


	private static class AEItem extends AEComparable
	{
		private final IItemDefinition definition;

		public AEItem( IItemDefinition definition )
		{
			super( definition );

			this.definition = definition;
		}

		@Nullable
		@Override
		public final ItemStack stack( int stackSize )
		{
			return this.definition.maybeStack( stackSize ).orNull();
		}

		@Nullable
		@Override
		public final Item item()
		{
			return this.definition.maybeItem().orNull();
		}
	}


	private static class AEBlock extends AEItem
	{
		private final IBlockDefinition definition;

		public AEBlock( IBlockDefinition definition )
		{
			super( definition );

			this.definition = definition;
		}

		@Nullable
		@Override
		public final Block block()
		{
			return this.definition.maybeBlock().orNull();
		}

		@Override
		public final boolean sameAsBlock( IBlockAccess world, int x, int y, int z )
		{
			return this.definition.isSameAs( world, x, y, z );
		}
	}


	private static final class AETile extends AEBlock
	{
		private final ITileDefinition definition;

		public AETile( ITileDefinition definition )
		{
			super( definition );

			this.definition = definition;
		}

		@Nullable
		@Override
		public final Class<? extends TileEntity> entity()
		{
			return this.definition.maybeEntity().orNull();
		}
	}
}
