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


import appeng.api.AEApi;
import appeng.api.parts.*;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.RenderBlocksWorkaround;
import appeng.core.AELog;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBuildCraftTransport;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.EnumSet;


public class FacadePart implements IFacadePart, IBoxProvider
{

	private final ItemStack facade;
	private final ForgeDirection side;
	private int thickness = 2;

	@SideOnly( Side.CLIENT )
	private ISimplifiedBundle prevLight;

	public FacadePart( final ItemStack facade, final ForgeDirection side )
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
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper instance2, final RenderBlocks renderer, final IFacadeContainer fc, final AxisAlignedBB busBounds, final boolean renderStilt )
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
						rbw.setFacade( false );
						rbw.setCalculations( true );
					}

					IIcon myIcon = null;
					if( this.notAEFacade() && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
					{
						final IBuildCraftTransport bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
						myIcon = bc.getCobbleStructurePipeTexture();
					}

					if( myIcon == null )
					{
						myIcon = this.facade.getIconIndex();
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

					instance.renderBlock( x, y, z, renderer );
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
							if( blk.canRenderInPass( 1 ) )
							{
								instance.renderForPass( 1 );
							}
						}

						int color = 0xffffff;

						try
						{
							color = ib.getColorFromItemStack( randomItem, 0 );
						}
						catch( final Throwable ignored )
						{
						}

						renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
						instance.setBounds( 0, 0, 16 - this.thickness, 16, 16, 16 );
						instance.prepareBounds( renderer );

						if( rbw != null )
						{
							rbw.setFacade( true );

							rbw.setCalculations( true );
							rbw.setFaces( EnumSet.noneOf( ForgeDirection.class ) );

							if( this.prevLight != null && rbw.similarLighting( blk, rbw.blockAccess, x, y, z, this.prevLight ) )
							{
								rbw.populate( this.prevLight );
							}
							else
							{
								instance.setRenderColor( color );
								rbw.renderStandardBlock( instance.getBlock(), x, y, z );
								instance.setRenderColor( 0xffffff );
								this.prevLight = rbw.getLightingCache();
							}

							rbw.setCalculations( false );
							rbw.setFaces( this.calculateFaceOpenFaces( rbw.blockAccess, fc, x, y, z, this.side ) );

							( (RenderBlocksWorkaround) renderer ).setTexture( blk.getIcon( ForgeDirection.DOWN.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.UP.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.NORTH.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.SOUTH.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.WEST.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.EAST.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ) );
						}
						else
						{
							instance.setTexture( blk.getIcon( ForgeDirection.DOWN.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.UP.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.NORTH.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.SOUTH.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.WEST.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ), blk.getIcon( ForgeDirection.EAST.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ) );
						}

						if( busBounds == null )
						{
							if( this.side == ForgeDirection.UP || this.side == ForgeDirection.DOWN )
							{
								instance.renderBlockCurrentBounds( x, y, z, renderer );
							}
							else if( this.side == ForgeDirection.NORTH || this.side == ForgeDirection.SOUTH )
							{
								if( fc.getFacade( ForgeDirection.UP ) != null )
								{
									renderer.renderMaxY -= this.thickness / 16.0;
								}

								if( fc.getFacade( ForgeDirection.DOWN ) != null )
								{
									renderer.renderMinY += this.thickness / 16.0;
								}

								instance.renderBlockCurrentBounds( x, y, z, renderer );
							}
							else
							{
								if( fc.getFacade( ForgeDirection.UP ) != null )
								{
									renderer.renderMaxY -= this.thickness / 16.0;
								}

								if( fc.getFacade( ForgeDirection.DOWN ) != null )
								{
									renderer.renderMinY += this.thickness / 16.0;
								}

								if( fc.getFacade( ForgeDirection.SOUTH ) != null )
								{
									renderer.renderMaxZ -= this.thickness / 16.0;
								}

								if( fc.getFacade( ForgeDirection.NORTH ) != null )
								{
									renderer.renderMinZ += this.thickness / 16.0;
								}

								instance.renderBlockCurrentBounds( x, y, z, renderer );
							}
						}
						else
						{
							if( this.side == ForgeDirection.UP || this.side == ForgeDirection.DOWN )
							{
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, busBounds.maxZ, 1.0, 1.0, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, 0.0, 1.0, 1.0, busBounds.minZ );
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, busBounds.minZ, busBounds.minX, 1.0, busBounds.maxZ );
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, busBounds.maxX, 0.0, busBounds.minZ, 1.0, 1.0, busBounds.maxZ );
							}
							else if( this.side == ForgeDirection.NORTH || this.side == ForgeDirection.SOUTH )
							{
								if( fc.getFacade( ForgeDirection.UP ) != null )
								{
									renderer.renderMaxY -= this.thickness / 16.0;
								}

								if( fc.getFacade( ForgeDirection.DOWN ) != null )
								{
									renderer.renderMinY += this.thickness / 16.0;
								}

								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, busBounds.maxX, 0.0, 0.0, 1.0, 1.0, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, 0.0, busBounds.minX, 1.0, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, busBounds.minX, 0.0, 0.0, busBounds.maxX, busBounds.minY, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, busBounds.minX, busBounds.maxY, 0.0, busBounds.maxX, 1.0, 1.0 );
							}
							else
							{
								if( fc.getFacade( ForgeDirection.UP ) != null )
								{
									renderer.renderMaxY -= this.thickness / 16.0;
								}

								if( fc.getFacade( ForgeDirection.DOWN ) != null )
								{
									renderer.renderMinY += this.thickness / 16.0;
								}

								if( fc.getFacade( ForgeDirection.SOUTH ) != null )
								{
									renderer.renderMaxZ -= this.thickness / 16.0;
								}

								if( fc.getFacade( ForgeDirection.NORTH ) != null )
								{
									renderer.renderMinZ += this.thickness / 16.0;
								}

								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, busBounds.maxZ, 1.0, 1.0, 1.0 );
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, 0.0, 1.0, 1.0, busBounds.minZ );
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, 0.0, busBounds.minZ, 1.0, busBounds.minY, busBounds.maxZ );
								this.renderSegmentBlockCurrentBounds( instance, x, y, z, renderer, 0.0, busBounds.maxY, busBounds.minZ, 1.0, 1.0, busBounds.maxZ );
							}
						}

						if( rbw != null )
						{
							rbw.setOpacity( 1.0f );
							rbw.setFaces( EnumSet.allOf( ForgeDirection.class ) );
						}

						instance.renderForPass( 0 );
						instance.setTexture( null );
						Tessellator.instance.setColorOpaque_F( 1, 1, 1 );
					}
				}
			}
			catch( final Throwable t )
			{
				AELog.debug( t );
			}
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( final IPartRenderHelper instance, final RenderBlocks renderer )
	{
		if( this.facade != null )
		{
			final IFacadeItem fi = (IFacadeItem) this.facade.getItem();

			try
			{
				if( fi != null )
				{
					final ItemStack randomItem = fi.getTextureItem( this.facade );

					instance.setTexture( this.facade.getIconIndex() );
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
								GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0F );
								instance.setInvColor( color );
							}
							catch( final Throwable error )
							{
								GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0F );
								instance.setInvColor( 0xffffff );
							}

							Tessellator.instance.setBrightness( 15 << 20 | 15 << 4 );
							Tessellator.instance.setColorOpaque_F( 1, 1, 1 );
							instance.setTexture( blk.getIcon( this.side.ordinal(), ib.getMetadata( randomItem.getItemDamage() ) ) );

							instance.setBounds( 0, 0, 14, 16, 16, 16 );
							instance.renderInventoryBox( renderer );

							instance.setTexture( null );
						}
					}
				}
			}
			catch( final Exception ignored )
			{

			}
		}
	}

	@Override
	public ForgeDirection getSide()
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

	private EnumSet<ForgeDirection> calculateFaceOpenFaces( final IBlockAccess blockAccess, final IFacadeContainer fc, final int x, final int y, final int z, final ForgeDirection side )
	{
		final EnumSet<ForgeDirection> out = EnumSet.of( side, side.getOpposite() );
		final IFacadePart facade = fc.getFacade( side );

		for( final ForgeDirection it : ForgeDirection.VALID_DIRECTIONS )
		{
			if( !out.contains( it ) && this.hasAlphaDiff( blockAccess.getTileEntity( x + it.offsetX, y + it.offsetY, z + it.offsetZ ), side, facade ) )
			{
				out.add( it );
			}
		}

		if( out.contains( ForgeDirection.UP ) && ( side.offsetX != 0 || side.offsetZ != 0 ) )
		{
			final IFacadePart fp = fc.getFacade( ForgeDirection.UP );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( ForgeDirection.UP );
			}
		}

		if( out.contains( ForgeDirection.DOWN ) && ( side.offsetX != 0 || side.offsetZ != 0 ) )
		{
			final IFacadePart fp = fc.getFacade( ForgeDirection.DOWN );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( ForgeDirection.DOWN );
			}
		}

		if( out.contains( ForgeDirection.SOUTH ) && ( side.offsetX != 0 ) )
		{
			final IFacadePart fp = fc.getFacade( ForgeDirection.SOUTH );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( ForgeDirection.SOUTH );
			}
		}

		if( out.contains( ForgeDirection.NORTH ) && ( side.offsetX != 0 ) )
		{
			final IFacadePart fp = fc.getFacade( ForgeDirection.NORTH );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( ForgeDirection.NORTH );
			}
		}

		/*
		 * if ( out.contains( ForgeDirection.EAST ) && (side.offsetZ != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.EAST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.EAST ); }
		 * if ( out.contains( ForgeDirection.WEST ) && (side.offsetZ != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.WEST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.WEST ); }
		 * if ( out.contains( ForgeDirection.NORTH ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.NORTH ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.NORTH ); }
		 * if ( out.contains( ForgeDirection.SOUTH ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.SOUTH ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.SOUTH ); }
		 * if ( out.contains( ForgeDirection.EAST ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.EAST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.EAST ); }
		 * if ( out.contains( ForgeDirection.WEST ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.WEST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.WEST ); }
		 */
		return out;
	}

	@SideOnly( Side.CLIENT )
	private void renderSegmentBlockCurrentBounds( final IPartRenderHelper instance, final int x, final int y, final int z, final RenderBlocks renderer, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ )
	{
		final double oldMinX = renderer.renderMinX;
		final double oldMinY = renderer.renderMinY;
		final double oldMinZ = renderer.renderMinZ;
		final double oldMaxX = renderer.renderMaxX;
		final double oldMaxY = renderer.renderMaxY;
		final double oldMaxZ = renderer.renderMaxZ;

		renderer.renderMinX = Math.max( renderer.renderMinX, minX );
		renderer.renderMinY = Math.max( renderer.renderMinY, minY );
		renderer.renderMinZ = Math.max( renderer.renderMinZ, minZ );
		renderer.renderMaxX = Math.min( renderer.renderMaxX, maxX );
		renderer.renderMaxY = Math.min( renderer.renderMaxY, maxY );
		renderer.renderMaxZ = Math.min( renderer.renderMaxZ, maxZ );

		// don't draw it if its not at least a pixel wide...
		if( renderer.renderMaxX - renderer.renderMinX >= 1.0 / 16.0 && renderer.renderMaxY - renderer.renderMinY >= 1.0 / 16.0 && renderer.renderMaxZ - renderer.renderMinZ >= 1.0 / 16.0 )
		{
			instance.renderBlockCurrentBounds( x, y, z, renderer );
		}

		renderer.renderMinX = oldMinX;
		renderer.renderMinY = oldMinY;
		renderer.renderMinZ = oldMinZ;
		renderer.renderMaxX = oldMaxX;
		renderer.renderMaxY = oldMaxY;
		renderer.renderMaxZ = oldMaxZ;
	}

	private boolean hasAlphaDiff( final TileEntity tileEntity, final ForgeDirection side, final IFacadePart facade )
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
