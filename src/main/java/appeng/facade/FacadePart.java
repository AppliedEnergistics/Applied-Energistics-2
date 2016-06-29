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

package appeng.facade;


import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.AEApi;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBuildCraftTransport;
import appeng.util.Platform;


public class FacadePart implements IFacadePart, IBoxProvider
{

	private final ItemStack facade;
	private final AEPartLocation side;
	private int thickness = 2;

	public FacadePart( final ItemStack facade, final AEPartLocation side )
	{
		if( facade == null )
		{
			throw new IllegalArgumentException( "Facade Part constructed on null item." );
		}
		this.facade = facade.copy();
		this.facade.stackSize = 1;
		this.side = side;
	}

	public static boolean isFacade( final ItemStack is )
	{
		return is.getItem() instanceof IFacadeItem;
	}

	@Override
	public ItemStack getItemStack()
	{
		return this.facade;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper ch, final Entity e )
	{
		if( e instanceof EntityLivingBase )
		{
			// prevent weird snag behavior
			ch.addBox( 0.0, 0.0, 14, 16.0, 16.0, 16.0 );
		}
		else
		{
			// the box is 15.9 for transition planes to pick up collision events.
			ch.addBox( 0.0, 0.0, 14, 16.0, 16.0, 15.9 );
		}
	}

	@Override
	public AEPartLocation getSide()
	{
		return this.side;
	}

	@Override
	public AxisAlignedBB getPrimaryBox()
	{
		return Platform.getPrimaryBox( this.side, this.thickness );
	}

	@Override
	public Item getItem()
	{
		final ItemStack is = this.getTexture();
		if( is == null )
		{
			return null;
		}
		return is.getItem();
	}

	@Override
	public int getItemDamage()
	{
		final ItemStack is = this.getTexture();
		if( is == null )
		{
			return 0;
		}
		return is.getItemDamage();
	}

	@Override
	public boolean notAEFacade()
	{
		return !( this.facade.getItem() instanceof IFacadeItem );
	}

	@Override
	public void setThinFacades( final boolean useThinFacades )
	{
		this.thickness = useThinFacades ? 1 : 2;
	}

	@Override
	public boolean isTransparent()
	{
		if( AEApi.instance().partHelper().getCableRenderMode().transparentFacades )
		{
			return true;
		}

		final ItemStack is = this.getTexture();
		final Block blk = Block.getBlockFromItem( is.getItem() );

		return !blk.isOpaqueCube(blk.getDefaultState());
	}

	@Nullable
	private ItemStack getTexture()
	{
		final Item maybeFacade = this.facade.getItem();

		// AE Facade
		if( maybeFacade instanceof IFacadeItem )
		{
			final IFacadeItem facade = (IFacadeItem) maybeFacade;

			return facade.getTextureItem( this.facade );
		}
		else if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
		{
			final IBuildCraftTransport bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );

			return bc.getTextureForFacade( this.facade );
		}

		return null;
	}

	private EnumSet<AEPartLocation> calculateFaceOpenFaces( final IBlockAccess blockAccess, final IFacadeContainer fc, final BlockPos pos, final AEPartLocation side )
	{
		final EnumSet<AEPartLocation> out = EnumSet.of( side, side.getOpposite() );
		final IFacadePart facade = fc.getFacade( side );

		for( final AEPartLocation it : AEPartLocation.SIDE_LOCATIONS )
		{
			if( !out.contains( it ) && this.hasAlphaDiff( blockAccess.getTileEntity( pos.offset( it.getFacing() ) ), side, facade ) )
			{
				out.add( it );
			}
		}

		if( out.contains( AEPartLocation.UP ) && ( side.xOffset != 0 || side.zOffset != 0 ) )
		{
			final IFacadePart fp = fc.getFacade( AEPartLocation.UP );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( AEPartLocation.UP );
			}
		}

		if( out.contains( AEPartLocation.DOWN ) && ( side.xOffset != 0 || side.zOffset != 0 ) )
		{
			final IFacadePart fp = fc.getFacade( AEPartLocation.DOWN );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( AEPartLocation.DOWN );
			}
		}

		if( out.contains( AEPartLocation.SOUTH ) && ( side.xOffset != 0 ) )
		{
			final IFacadePart fp = fc.getFacade( AEPartLocation.SOUTH );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( AEPartLocation.SOUTH );
			}
		}

		if( out.contains( AEPartLocation.NORTH ) && ( side.xOffset != 0 ) )
		{
			final IFacadePart fp = fc.getFacade( AEPartLocation.NORTH );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( AEPartLocation.NORTH );
			}
		}

		/*
		 * if ( out.contains( AEPartLocation.EAST ) && (side.offsetZ != 0) ) { IFacadePart fp = fc.getFacade(
		 * AEPartLocation.EAST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * AEPartLocation.EAST ); }
		 * if ( out.contains( AEPartLocation.WEST ) && (side.offsetZ != 0) ) { IFacadePart fp = fc.getFacade(
		 * AEPartLocation.WEST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * AEPartLocation.WEST ); }
		 * if ( out.contains( AEPartLocation.NORTH ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * AEPartLocation.NORTH ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * AEPartLocation.NORTH ); }
		 * if ( out.contains( AEPartLocation.SOUTH ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * AEPartLocation.SOUTH ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * AEPartLocation.SOUTH ); }
		 * if ( out.contains( AEPartLocation.EAST ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * AEPartLocation.EAST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * AEPartLocation.EAST ); }
		 * if ( out.contains( AEPartLocation.WEST ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * AEPartLocation.WEST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * AEPartLocation.WEST ); }
		 */
		return out;
	}

	private boolean hasAlphaDiff( final TileEntity tileEntity, final AEPartLocation side, final IFacadePart facade )
	{
		if( tileEntity instanceof IPartHost )
		{
			final IPartHost ph = (IPartHost) tileEntity;
			final IFacadePart fp = ph.getFacadeContainer().getFacade( side );

			return fp == null || ( fp.isTransparent() != facade.isTransparent() );
		}

		return true;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		this.getBoxes( bch, null );
	}
}
