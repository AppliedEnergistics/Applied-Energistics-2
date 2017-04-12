package appeng.core.features;


import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.ITileDefinition;
import appeng.api.util.AEItemDefinition;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;


/**
 * @deprecated
 */
@Deprecated
public final class DefinitionConverter
{
	public AEItemDefinition of( final ITileDefinition definition )
	{
		return new AETile( definition );
	}

	public AEItemDefinition of( final IBlockDefinition definition )
	{
		return new AEBlock( definition );
	}

	public AEItemDefinition of( final IItemDefinition definition )
	{
		return new AEItem( definition );
	}

	public AEItemDefinition of( final IComparableDefinition definition )
	{
		return new AEComparable( definition );
	}

	private static class AEComparable implements AEItemDefinition
	{
		private final IComparableDefinition definition;

		public AEComparable( final IComparableDefinition definition )
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
		public ItemStack stack( final int stackSize )
		{
			return null;
		}

		@Override
		public boolean sameAsStack( final ItemStack comparableItem )
		{
			return this.definition.isSameAs( comparableItem );
		}

		@Override
		public boolean sameAsBlock( final IBlockAccess world, final int x, final int y, final int z )
		{
			return false;
		}
	}


	private static class AEItem extends AEComparable
	{
		private final IItemDefinition definition;

		public AEItem( final IItemDefinition definition )
		{
			super( definition );

			this.definition = definition;
		}

		@Nullable
		@Override
		public ItemStack stack( final int stackSize )
		{
			return this.definition.maybeStack( stackSize ).orNull();
		}

		@Nullable
		@Override
		public Item item()
		{
			return this.definition.maybeItem().orNull();
		}
	}


	private static class AEBlock extends AEItem
	{
		private final IBlockDefinition definition;

		public AEBlock( final IBlockDefinition definition )
		{
			super( definition );

			this.definition = definition;
		}

		@Nullable
		@Override
		public Block block()
		{
			return this.definition.maybeBlock().orNull();
		}

		@Override
		public boolean sameAsBlock( final IBlockAccess world, final int x, final int y, final int z )
		{
			return this.definition.isSameAs( world, x, y, z );
		}
	}


	private static class AETile extends AEBlock
	{
		private final ITileDefinition definition;

		public AETile( final ITileDefinition definition )
		{
			super( definition );

			this.definition = definition;
		}

		@Nullable
		@Override
		public Class<? extends TileEntity> entity()
		{
			return this.definition.maybeEntity().orNull();
		}
	}
}
