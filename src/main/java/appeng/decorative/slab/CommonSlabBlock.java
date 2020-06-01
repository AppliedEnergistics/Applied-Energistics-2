
package appeng.decorative.slab;


import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public abstract class CommonSlabBlock extends SlabBlock
{

	private CommonSlabBlock( Block block )
	{
		super( block.getMaterial( block.getDefaultState() ) );
		this.setHardness( block.getBlockHardness( block.getDefaultState(), null, null ) );
		this.setResistance( block.getExplosionResistance( null ) * 5.0F / 3.0F );

		BlockState BlockState = this.blockState.getBaseState();

		if( !this.isDouble() )
		{
			BlockState = BlockState.with( HALF, BlockSlab.EnumBlockHalf.BOTTOM );
		}

		this.setDefaultState( BlockState.with( VARIANT, Variant.DEFAULT ) );
		this.setCreativeTab( CreativeTabs.BUILDING_BLOCKS );
		this.useNeighborBrightness = true;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public BlockState getStateFromMeta( int meta )
	{
		BlockState BlockState = this.getDefaultState().with( VARIANT, Variant.DEFAULT );

		if( !this.isDouble() )
		{
			BlockState = BlockState.with( HALF, ( meta & 8 ) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP );
		}

		return BlockState;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState( BlockState state )
	{
		int i = 0;

		if( !this.isDouble() && state.getValue( HALF ) == BlockSlab.EnumBlockHalf.TOP )
		{
			i |= 8;
		}

		return i;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return this.isDouble() ? new BlockStateContainer( this, VARIANT ) : new BlockStateContainer( this, HALF, VARIANT );
	}

	@Override
	@Nullable
	public Item getItemDropped( BlockState state, Random rand, int fortune )
	{
		return Item.getItemFromBlock( this );
	}

	@Override
	public ItemStack getItem( World worldIn, BlockPos pos, BlockState state )
	{
		return new ItemStack( this, 1, 0 );
	}

	@Override
	public String getTranslationKey( int meta )
	{
		return this.getTranslationKey();
	}

	@Override
	public IProperty<?> getVariantProperty()
	{
		return VARIANT;
	}

	@Override
	public Comparable<?> getTypeForItem( ItemStack stack )
	{
		return Variant.DEFAULT;
	}

	public static class Double extends CommonSlabBlock
	{

		private final Block halfSlabBlock;

		public Double( Block halfSlabBlock, Block block )
		{
			super( block );
			this.halfSlabBlock = halfSlabBlock;
		}

		@Override
		public boolean isDouble()
		{
			return true;
		}

		@Override
		@Nullable
		public Item getItemDropped( BlockState state, Random rand, int fortune )
		{
			return Item.getItemFromBlock( this.halfSlabBlock );
		}

		@Override
		public ItemStack getItem( World worldIn, BlockPos pos, BlockState state )
		{
			return new ItemStack( this.halfSlabBlock, 1, 0 );
		}

	}

	public static class Half extends CommonSlabBlock
	{

		public Half( Block block )
		{
			super( block );
		}

		@Override
		public boolean isDouble()
		{
			return false;
		}
	}

	public enum Variant implements IStringSerializable
	{
		DEFAULT;

		@Override
		public String getName()
		{
			return "default";
		}
	}
}
