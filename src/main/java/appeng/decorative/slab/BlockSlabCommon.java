
package appeng.decorative.slab;


import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public abstract class BlockSlabCommon extends BlockSlab
{

	static final PropertyEnum<BlockSlabCommon.Variant> VARIANT = PropertyEnum.create( "variant", Variant.class );

	private BlockSlabCommon( Block block )
	{
		super( block.getMaterial( block.getDefaultState() ) );
		this.setHardness( block.getBlockHardness( block.getDefaultState(), null, null ) );
		this.setResistance( block.getExplosionResistance( null ) * 5.0F / 3.0F );

		BlockState BlockState = this.blockState.getBaseState();

		if( !this.isDouble() )
		{
			BlockState = BlockState.withProperty( HALF, BlockSlab.EnumBlockHalf.BOTTOM );
		}

		this.setDefaultState( BlockState.withProperty( VARIANT, Variant.DEFAULT ) );
		this.setCreativeTab( CreativeTabs.BUILDING_BLOCKS );
		this.useNeighborBrightness = true;
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
	public String getUnlocalizedName( int meta )
	{
		return this.getUnlocalizedName();
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

	public static class Double extends BlockSlabCommon
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

	public static class Half extends BlockSlabCommon
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
