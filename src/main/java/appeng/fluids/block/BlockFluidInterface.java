
package appeng.fluids.block;


import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseTileBlock;
import appeng.core.sync.GuiBridge;
import appeng.fluids.tile.TileFluidInterface;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;


public class BlockFluidInterface extends AEBaseTileBlock
{
	private static final PropertyBool OMNIDIRECTIONAL = PropertyBool.create( "omnidirectional" );

	public BlockFluidInterface()
	{
		super( Material.IRON );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { OMNIDIRECTIONAL };
	}

	@Override
	public IBlockState getActualState( IBlockState state, IBlockAccess world, BlockPos pos )
	{
		// Determine whether the interface is omni-directional or not
		TileInterface te = this.getTileEntity( world, pos );
		boolean omniDirectional = true; // The default
		if( te != null )
		{
			omniDirectional = te.isOmniDirectional();
		}

		return super.getActualState( state, world, pos )
				.withProperty( OMNIDIRECTIONAL, omniDirectional );
	}

	@Override
	public boolean onActivated( final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		if( p.isSneaking() )
		{
			return false;
		}

		final TileEntity tg = this.getTileEntity( w, pos );
		if( tg instanceof TileFluidInterface )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_FLUID_INTERFACE );
			}
			return true;
		}
		return false;
	}

	@Override
	protected boolean hasCustomRotation()
	{
		return true;
	}

	@Override
	protected void customRotateBlock( final IOrientable rotatable, final EnumFacing axis )
	{
		if( rotatable instanceof TileInterface )
		{
			( (TileInterface) rotatable ).setSide( axis );
		}
	}
}
