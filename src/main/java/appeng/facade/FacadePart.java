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
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEPartLocation;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.ModelGenerator;
import appeng.client.render.RenderBlocksWorkaround;
import appeng.client.texture.IAESprite;
import appeng.core.AELog;
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
	@SideOnly( Side.CLIENT )
	public void renderStatic( final BlockPos pos, final IPartRenderHelper instance2, final ModelGenerator renderer, final IFacadeContainer fc, final AxisAlignedBB busBounds, final boolean renderStilt )
	{
		if( this.facade != null )
		{
			final BusRenderHelper instance = (BusRenderHelper) instance2;

			try
			{
				final ItemStack randomItem = this.getTexture();

				RenderBlocksWorkaround rbw = null;
				if( renderer instanceof RenderBlocksWorkaround )
				{
					rbw = (RenderBlocksWorkaround) renderer;
				}

				if( renderStilt && busBounds == null )
				{
					if( rbw != null )
					{
						// rbw.isFacade = false;
						// rbw.calculations = true;
					}

					IAESprite myIcon = null;
					if( this.notAEFacade() && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
					{
						final IBuildCraftTransport bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
						myIcon = bc.getCobbleStructurePipeTexture();
					}

					if( myIcon == null )
					{
						myIcon = renderer.getIcon( this.facade );
					}

					instance.setTexture( myIcon );

					if( this.notAEFacade() )
					{
						instance.setBounds( 6, 6, 10, 10, 10, 15 );
					}
					else
					{
						instance.setBounds( 7, 7, 10, 9, 9, 15 );
					}

					instance.renderBlock( pos, renderer );
					instance.setTexture( null );
				}

				if( randomItem != null )
				{
					if( randomItem.getItem() instanceof ItemBlock )
					{
						final ItemBlock ib = (ItemBlock) randomItem.getItem();
						final Block blk = Block.getBlockFromItem( ib );

						if( AEApi.instance().partHelper().getCableRenderMode().transparentFacades )
						{
							if( rbw != null )
							{
								rbw.setOpacity( 0.3f );
							}
							instance.renderForPass( 1 );
						}
						else
						{
							if( blk.canRenderInLayer( EnumWorldBlockLayer.TRANSLUCENT ) )
							{
								instance.renderForPass( 1 );
							}
						}

						try
						{
							final int color = ib.getColorFromItemStack( randomItem, 0 );
						}
						catch( final Throwable ignored )
						{
						}

						renderer.setUvRotateBottom( renderer.setUvRotateEast( renderer.setUvRotateNorth( renderer.setUvRotateSouth( renderer.setUvRotateTop( renderer.setUvRotateWest( 0 ) ) ) ) ) );
						instance.setBounds( 0, 0, 16 - this.thickness, 16, 16, 16 );
						instance.prepareBounds( renderer );

						/*
						 * if( rbw != null )
						 * {
						 * rbw.setFacade( true );
						 * <<<<<<< HEAD
						 * rbw.calculations = true;
						 * rbw.faces = EnumSet.noneOf( AEPartLocation.class );
						 * =======
						 * rbw.setCalculations( true );
						 * rbw.setFaces( EnumSet.noneOf( ForgeDirection.class ) );
						 * >>>>>>> 500fc47... Reduces visibility of internal fields/methods
						 * if( this.prevLight != null && rbw.similarLighting( blk, rbw.blockAccess, x, y, z,
						 * this.prevLight ) )
						 * {
						 * rbw.populate( this.prevLight );
						 * }
						 * else
						 * {
						 * instance.setRenderColor( color );
						 * rbw.renderStandardBlock( instance.getBlock(), x, y, z );
						 * instance.setRenderColor( 0xffffff );
						 * this.prevLight = rbw.getLightingCache();
						 * }
						 * rbw.setCalculations( false );
						 * rbw.setFaces( this.calculateFaceOpenFaces( rbw.blockAccess, fc, x, y, z, this.side ) );
						 * ( (RenderBlocksWorkaround) renderer ).setTexture( blk.getIcon( AEPartLocation.DOWN.ordinal(),
						 * ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( AEPartLocation.UP.ordinal(),
						 * ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( AEPartLocation.NORTH.ordinal(),
						 * ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( AEPartLocation.SOUTH.ordinal(),
						 * ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( AEPartLocation.WEST.ordinal(),
						 * ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( AEPartLocation.EAST.ordinal(),
						 * ib.getMetadata( randomItem.getItemDamage() ) ) );
						 * }
						 * else
						 * {
						 */
						final IAESprite[] icon_down = renderer.getIcon( blk.getDefaultState() );

						instance.setTexture( icon_down[EnumFacing.DOWN.ordinal()], icon_down[EnumFacing.UP.ordinal()], icon_down[EnumFacing.NORTH.ordinal()], icon_down[EnumFacing.SOUTH.ordinal()], icon_down[EnumFacing.WEST.ordinal()], icon_down[EnumFacing.EAST.ordinal()] );
						// }

						if( busBounds == null )
						{
							if( this.side == AEPartLocation.UP || this.side == AEPartLocation.DOWN )
							{
								instance.renderBlockCurrentBounds( pos, renderer );
							}
							else if( this.side == AEPartLocation.NORTH || this.side == AEPartLocation.SOUTH )
							{
								if( fc.getFacade( AEPartLocation.UP ) != null )
								{
									renderer.setRenderMaxY( renderer.getRenderMaxY() - this.thickness / 16.0 );
								}

								if( fc.getFacade( AEPartLocation.DOWN ) != null )
								{
									renderer.setRenderMinY( renderer.getRenderMinY() + this.thickness / 16.0 );
								}

								instance.renderBlockCurrentBounds( pos, renderer );
							}
							else
							{
								if( fc.getFacade( AEPartLocation.UP ) != null )
								{
									renderer.setRenderMaxY( renderer.getRenderMaxY() - this.thickness / 16.0 );
								}

								if( fc.getFacade( AEPartLocation.DOWN ) != null )
								{
									renderer.setRenderMinY( renderer.getRenderMinY() + this.thickness / 16.0 );
								}

								if( fc.getFacade( AEPartLocation.SOUTH ) != null )
								{
									renderer.setRenderMaxZ( renderer.getRenderMaxZ() - this.thickness / 16.0 );
								}

								if( fc.getFacade( AEPartLocation.NORTH ) != null )
								{
									renderer.setRenderMinZ( renderer.getRenderMinZ() + this.thickness / 16.0 );
								}

								instance.renderBlockCurrentBounds( pos, renderer );
							}
						}
						else
						{
							if( this.side == AEPartLocation.UP || this.side == AEPartLocation.DOWN )
							{
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, 0.0, 0.0, busBounds.maxZ, 1.0, 1.0, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, 0.0, 0.0, 0.0, 1.0, 1.0, busBounds.minZ );
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, 0.0, 0.0, busBounds.minZ, busBounds.minX, 1.0, busBounds.maxZ );
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, busBounds.maxX, 0.0, busBounds.minZ, 1.0, 1.0, busBounds.maxZ );
							}
							else if( this.side == AEPartLocation.NORTH || this.side == AEPartLocation.SOUTH )
							{
								if( fc.getFacade( AEPartLocation.UP ) != null )
								{
									renderer.setRenderMaxY( renderer.getRenderMaxY() - this.thickness / 16.0 );
								}

								if( fc.getFacade( AEPartLocation.DOWN ) != null )
								{
									renderer.setRenderMinY( renderer.getRenderMinY() + this.thickness / 16.0 );
								}

								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, busBounds.maxX, 0.0, 0.0, 1.0, 1.0, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, 0.0, 0.0, 0.0, busBounds.minX, 1.0, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, busBounds.minX, 0.0, 0.0, busBounds.maxX, busBounds.minY, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, busBounds.minX, busBounds.maxY, 0.0, busBounds.maxX, 1.0, 1.0 );
							}
							else
							{
								if( fc.getFacade( AEPartLocation.UP ) != null )
								{
									renderer.setRenderMaxY( renderer.getRenderMaxY() - this.thickness / 16.0 );
								}

								if( fc.getFacade( AEPartLocation.DOWN ) != null )
								{
									renderer.setRenderMinY( renderer.getRenderMinY() + this.thickness / 16.0 );
								}

								if( fc.getFacade( AEPartLocation.SOUTH ) != null )
								{
									renderer.setRenderMaxZ( renderer.getRenderMaxZ() - this.thickness / 16.0 );
								}

								if( fc.getFacade( AEPartLocation.NORTH ) != null )
								{
									renderer.setRenderMinZ( renderer.getRenderMinZ() + this.thickness / 16.0 );
								}

								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, 0.0, 0.0, busBounds.maxZ, 1.0, 1.0, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, 0.0, 0.0, 0.0, 1.0, 1.0, busBounds.minZ );
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, 0.0, 0.0, busBounds.minZ, 1.0, busBounds.minY, busBounds.maxZ );
								this.renderSegmentBlockCurrentBounds( instance, pos, renderer, 0.0, busBounds.maxY, busBounds.minZ, 1.0, 1.0, busBounds.maxZ );
							}
						}

						if( rbw != null )
						{
							rbw.setOpacity( 1.0f );
							rbw.setFaces( EnumSet.allOf( EnumFacing.class ) );
						}

						instance.renderForPass( 0 );
						instance.setTexture( null );
						renderer.setColorOpaque_F( 1, 1, 1 );
					}
				}
			}
			catch( final Throwable t )
			{
				AELog.error( t );
			}
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( final IPartRenderHelper instance, final ModelGenerator renderer )
	{
		if( this.facade != null )
		{
			final IFacadeItem fi = (IFacadeItem) this.facade.getItem();

			try
			{
				final ItemStack randomItem = fi.getTextureItem( this.facade );

				instance.setTexture( renderer.getIcon( this.facade ) );
				instance.setBounds( 7, 7, 4, 9, 9, 14 );
				instance.renderInventoryBox( renderer );
				instance.setTexture( null );

				if( randomItem != null )
				{
					if( randomItem.getItem() instanceof ItemBlock )
					{
						final ItemBlock ib = (ItemBlock) randomItem.getItem();
						final Block blk = Block.getBlockFromItem( ib );

						try
						{
							final int color = ib.getColorFromItemStack( randomItem, 0 );
							instance.setInvColor( color );
						}
						catch( final Throwable error )
						{
							instance.setInvColor( 0xffffff );
						}

						renderer.setBrightness( 15 << 20 | 15 << 4 );
						renderer.setColorOpaque_F( 1, 1, 1 );
						instance.setTexture( renderer.getIcon( blk.getDefaultState() )[this.side.ordinal()] );

						instance.setBounds( 0, 0, 14, 16, 16, 16 );
						instance.renderInventoryBox( renderer );

						instance.setTexture( null );
					}
				}
			}
			catch( final Throwable ignored )
			{

			}
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

		return !blk.isOpaqueCube();
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

	@SideOnly( Side.CLIENT )
	private void renderSegmentBlockCurrentBounds( final IPartRenderHelper instance, final BlockPos pos, final ModelGenerator renderer, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ )
	{
		final double oldMinX = renderer.getRenderMinX();
		final double oldMinY = renderer.getRenderMinY();
		final double oldMinZ = renderer.getRenderMinZ();
		final double oldMaxX = renderer.getRenderMaxX();
		final double oldMaxY = renderer.getRenderMaxY();
		final double oldMaxZ = renderer.getRenderMaxZ();

		renderer.setRenderMinX( Math.max( renderer.getRenderMinX(), minX ) );
		renderer.setRenderMinY( Math.max( renderer.getRenderMinY(), minY ) );
		renderer.setRenderMinZ( Math.max( renderer.getRenderMinZ(), minZ ) );
		renderer.setRenderMaxX( Math.min( renderer.getRenderMaxX(), maxX ) );
		renderer.setRenderMaxY( Math.min( renderer.getRenderMaxY(), maxY ) );
		renderer.setRenderMaxZ( Math.min( renderer.getRenderMaxZ(), maxZ ) );

		// don't draw it if its not at least a pixel wide...
		if( renderer.getRenderMaxX() - renderer.getRenderMinX() >= 1.0 / 16.0 && renderer.getRenderMaxY() - renderer.getRenderMinY() >= 1.0 / 16.0 && renderer.getRenderMaxZ() - renderer.getRenderMinZ() >= 1.0 / 16.0 )
		{
			instance.renderBlockCurrentBounds( pos, renderer );
		}

		renderer.setRenderMinX( oldMinX );
		renderer.setRenderMinY( oldMinY );
		renderer.setRenderMinZ( oldMinZ );
		renderer.setRenderMaxX( oldMaxX );
		renderer.setRenderMaxY( oldMaxY );
		renderer.setRenderMaxZ( oldMaxZ );
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
