package appeng.decorative.slab;


import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
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

		IBlockState iblockstate = this.blockState.getBaseState();

		if( !this.isDouble() )
		{
			iblockstate = iblockstate.withProperty( HALF, BlockSlab.EnumBlockHalf.BOTTOM );
		}

		this.setDefaultState( iblockstate.withProperty( VARIANT, Variant.DEFAULT ) );
		this.setCreativeTab( CreativeTabs.BUILDING_BLOCKS );
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta( int meta )
	{
		IBlockState iblockstate = this.getDefaultState().withProperty( VARIANT, Variant.DEFAULT );

		if( !this.isDouble() )
		{
			iblockstate = iblockstate.withProperty( HALF, ( meta & 8 ) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP );
		}

		return iblockstate;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState( IBlockState state )
	{
		int i = 0;

		if( !this.isDouble() && state.getValue( HALF ) == BlockSlab.EnumBlockHalf.TOP )
		{
			i |= 8;
		}

		return i;
	}

	protected BlockStateContainer createBlockState()
	{
		return this.isDouble() ? new BlockStateContainer( this, VARIANT ) : new BlockStateContainer( this, HALF, VARIANT );
	}

	@Nullable
	public Item getItemDropped( IBlockState state, Random rand, int fortune )
	{
		return Item.getItemFromBlock( this );
	}

	public ItemStack getItem( World worldIn, BlockPos pos, IBlockState state )
	{
		return new ItemStack( this, 1, 0 );
	}

	@Override
	public String getUnlocalizedName( int meta )
	{
		return getUnlocalizedName();
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

		public boolean isDouble()
		{
			return true;
		}

		@Nullable
		public Item getItemDropped( IBlockState state, Random rand, int fortune )
		{
			return Item.getItemFromBlock( halfSlabBlock );
		}

		public ItemStack getItem( World worldIn, BlockPos pos, IBlockState state )
		{
			return new ItemStack( halfSlabBlock, 1, 0 );
		}

	}

	public static class Half extends BlockSlabCommon
	{

		public Half( Block block )
		{
			super( block );
		}

		public boolean isDouble()
		{
			return false;
		}
	}

	public enum Variant implements IStringSerializable
	{
		DEFAULT;

		public String getName()
		{
			return "default";
		}
	}
}
