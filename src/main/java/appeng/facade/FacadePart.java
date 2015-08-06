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

import org.lwjgl.opengl.GL11;

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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.ISimplifiedBundle;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.RenderBlocksWorkaround;
import appeng.core.AELog;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBuildCraftTransport;
import appeng.util.Platform;


public class FacadePart implements IFacadePart, IBoxProvider
{

	public final ItemStack facade;
	public final ForgeDirection side;
	public int thickness = 2;

	@SideOnly( Side.CLIENT )
	ISimplifiedBundle prevLight;

	public FacadePart( ItemStack facade, ForgeDirection side )
	{
		if( facade == null )
		{
			throw new IllegalArgumentException( "Facade Part constructed on null item." );
		}
		this.facade = facade.copy();
		this.facade.stackSize = 1;
		this.side = side;
	}

	public static boolean isFacade( ItemStack is )
	{
		return is.getItem() instanceof IFacadeItem;
	}

	@Override
	public ItemStack getItemStack()
	{
		return this.facade;
	}

	@Override
	public void getBoxes( IPartCollisionHelper ch, Entity e )
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
	public void renderStatic( int x, int y, int z, IPartRenderHelper instance2, RenderBlocks renderer, IFacadeContainer fc, AxisAlignedBB busBounds, boolean renderStilt )
	{
		if( this.facade != null )
		{
			BusRenderHelper instance = (BusRenderHelper) instance2;

			try
			{
				ItemStack randomItem = this.getTexture();

				RenderBlocksWorkaround rbw = null;
				if( renderer instanceof RenderBlocksWorkaround )
				{
					rbw = (RenderBlocksWorkaround) renderer;
				}

				if( renderStilt && busBounds == null )
				{
					if( rbw != null )
					{
						rbw.isFacade = false;
						rbw.calculations = true;
					}

					IIcon myIcon = null;
					if( this.notAEFacade() && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
					{
						IBuildCraftTransport bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
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
						ItemBlock ib = (ItemBlock) randomItem.getItem();
						Block blk = Block.getBlockFromItem( ib );

						if( AEApi.instance().partHelper().getCableRenderMode().transparentFacades )
						{
							if( rbw != null )
							{
								rbw.opacity = 0.3f;
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
						catch( Throwable ignored )
						{
						}

						renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
						instance.setBounds( 0, 0, 16 - this.thickness, 16, 16, 16 );
						instance.prepareBounds( renderer );

						if( rbw != null )
						{
							rbw.isFacade = true;

							rbw.calculations = true;
							rbw.faces = EnumSet.noneOf( ForgeDirection.class );

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

							rbw.calculations = false;
							rbw.faces = this.calculateFaceOpenFaces( rbw.blockAccess, fc, x, y, z, this.side );

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
							rbw.opacity = 1.0f;
							rbw.faces = EnumSet.allOf( ForgeDirection.class );
						}

						instance.renderForPass( 0 );
						instance.setTexture( null );
						Tessellator.instance.setColorOpaque_F( 1, 1, 1 );
					}
				}
			}
			catch( Throwable t )
			{
				AELog.error( t );
			}
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper instance, RenderBlocks renderer )
	{
		if( this.facade != null )
		{
			IFacadeItem fi = (IFacadeItem) this.facade.getItem();

			try
			{
				if( fi != null )
				{
					ItemStack randomItem = fi.getTextureItem( this.facade );

					instance.setTexture( this.facade.getIconIndex() );
					instance.setBounds( 7, 7, 4, 9, 9, 14 );
					instance.renderInventoryBox( renderer );
					instance.setTexture( null );

					if( randomItem != null )
					{
						if( randomItem.getItem() instanceof ItemBlock )
						{
							ItemBlock ib = (ItemBlock) randomItem.getItem();
							Block blk = Block.getBlockFromItem( ib );

							try
							{
								int color = ib.getColorFromItemStack( randomItem, 0 );
								GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0F );
								instance.setInvColor( color );
							}
							catch( Throwable error )
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
			catch( Exception ignored )
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
		ItemStack is = this.getTexture();
		if( is == null )
		{
			return null;
		}
		return is.getItem();
	}

	@Override
	public int getItemDamage()
	{
		ItemStack is = this.getTexture();
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
	public void setThinFacades( boolean useThinFacades )
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

		ItemStack is = this.getTexture();
		Block blk = Block.getBlockFromItem( is.getItem() );

		return !blk.isOpaqueCube();
	}

	@Nullable
	ItemStack getTexture()
	{
		final Item maybeFacade = this.facade.getItem();

		// AE Facade
		if( maybeFacade instanceof IFacadeItem )
		{
			IFacadeItem facade = (IFacadeItem) maybeFacade;

			return facade.getTextureItem( this.facade );
		}
		else if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
		{
			final IBuildCraftTransport bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );

			return bc.getTextureForFacade( this.facade );
		}

		return null;
	}

	private EnumSet<ForgeDirection> calculateFaceOpenFaces( IBlockAccess blockAccess, IFacadeContainer fc, int x, int y, int z, ForgeDirection side )
	{
		EnumSet<ForgeDirection> out = EnumSet.of( side, side.getOpposite() );
		IFacadePart facade = fc.getFacade( side );

		for( ForgeDirection it : ForgeDirection.VALID_DIRECTIONS )
		{
			if( !out.contains( it ) && this.hasAlphaDiff( blockAccess.getTileEntity( x + it.offsetX, y + it.offsetY, z + it.offsetZ ), side, facade ) )
			{
				out.add( it );
			}
		}

		if( out.contains( ForgeDirection.UP ) && ( side.offsetX != 0 || side.offsetZ != 0 ) )
		{
			IFacadePart fp = fc.getFacade( ForgeDirection.UP );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( ForgeDirection.UP );
			}
		}

		if( out.contains( ForgeDirection.DOWN ) && ( side.offsetX != 0 || side.offsetZ != 0 ) )
		{
			IFacadePart fp = fc.getFacade( ForgeDirection.DOWN );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( ForgeDirection.DOWN );
			}
		}

		if( out.contains( ForgeDirection.SOUTH ) && ( side.offsetX != 0 ) )
		{
			IFacadePart fp = fc.getFacade( ForgeDirection.SOUTH );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( ForgeDirection.SOUTH );
			}
		}

		if( out.contains( ForgeDirection.NORTH ) && ( side.offsetX != 0 ) )
		{
			IFacadePart fp = fc.getFacade( ForgeDirection.NORTH );
			if( fp != null && ( fp.isTransparent() == facade.isTransparent() ) )
			{
				out.remove( ForgeDirection.NORTH );
			}
		}

		/*
		 * if ( out.contains( ForgeDirection.EAST ) && (side.offsetZ != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.EAST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.EAST ); }
		 *
		 * if ( out.contains( ForgeDirection.WEST ) && (side.offsetZ != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.WEST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.WEST ); }
		 *
		 * if ( out.contains( ForgeDirection.NORTH ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.NORTH ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.NORTH ); }
		 *
		 * if ( out.contains( ForgeDirection.SOUTH ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.SOUTH ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.SOUTH ); }
		 *
		 * if ( out.contains( ForgeDirection.EAST ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.EAST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.EAST ); }
		 *
		 * if ( out.contains( ForgeDirection.WEST ) && (side.offsetY != 0) ) { IFacadePart fp = fc.getFacade(
		 * ForgeDirection.WEST ); if ( fp != null && (fp.isTransparent() == facade.isTransparent()) ) out.remove(
		 * ForgeDirection.WEST ); }
		 */
		return out;
	}

	@SideOnly( Side.CLIENT )
	private void renderSegmentBlockCurrentBounds( IPartRenderHelper instance, int x, int y, int z, RenderBlocks renderer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ )
	{
		double oldMinX = renderer.renderMinX;
		double oldMinY = renderer.renderMinY;
		double oldMinZ = renderer.renderMinZ;
		double oldMaxX = renderer.renderMaxX;
		double oldMaxY = renderer.renderMaxY;
		double oldMaxZ = renderer.renderMaxZ;

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

	private boolean hasAlphaDiff( TileEntity tileEntity, ForgeDirection side, IFacadePart facade )
	{
		if( tileEntity instanceof IPartHost )
		{
			IPartHost ph = (IPartHost) tileEntity;
			IFacadePart fp = ph.getFacadeContainer().getFacade( side );

			return fp == null || ( fp.isTransparent() != facade.isTransparent() );
		}

		return true;
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		this.getBoxes( bch, null );
	}
}
