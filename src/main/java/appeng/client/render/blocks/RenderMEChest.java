/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.render.blocks;


import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.block.storage.BlockChest;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.FlippableIcon;
import appeng.client.texture.IAESprite;
import appeng.client.texture.OffsetIcon;
import appeng.tile.storage.TileChest;
import appeng.util.Platform;


public class RenderMEChest extends BaseBlockRender<BlockChest, TileChest>
{

	public RenderMEChest()
	{
		super( false, 0 );
	}

	@Override
	public void renderInventory( final BlockChest block, final ItemStack is, final ModelGenerator renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.setBrightness( 0 );
		renderer.overrideBlockTexture = ExtraBlockTextures.White.getIcon();
		this.renderInvBlock( EnumSet.of( AEPartLocation.SOUTH ), block, is, 0x000000, renderer );

		renderer.overrideBlockTexture = ExtraBlockTextures.MEChest.getIcon();
		this.renderInvBlock( EnumSet.of( AEPartLocation.UP ), block, is, this.adjustBrightness( AEColor.Transparent.whiteVariant, 0.7 ), renderer );

		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( final BlockChest imb, final IBlockAccess world, final BlockPos pos, final ModelGenerator renderer )
	{
		final TileChest sp = imb.getTileEntity( world, pos );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		if( sp == null )
		{
			return false;
		}

		final EnumFacing up = sp.getUp();
		final EnumFacing forward = sp.getForward();
		final EnumFacing west = Platform.crossProduct( forward, up );

		this.preRenderInWorld( imb, world, pos, renderer );

		final int stat = sp.getCellStatus( 0 );
		final boolean result = renderer.renderStandardBlock( imb, pos );

		this.selectFace( renderer, west, up, forward, 5, 16 - 5, 9, 12 );

		int offsetV = 8;
		if( stat == 0 )
		{
			offsetV = 3;
		}

		int b = world.getCombinedLight( pos.offset( forward ), 0 );
		renderer.setBrightness( b );
		renderer.setColorOpaque_I( 0xffffff );

		final int offsetU = -4;
		final FlippableIcon flippableIcon = new FlippableIcon( new OffsetIcon( ExtraBlockTextures.MEStorageCellTextures.getIcon(), offsetU, offsetV ) );
		if( forward == EnumFacing.EAST && ( up == EnumFacing.NORTH || up == EnumFacing.SOUTH ) )
		{
			flippableIcon.setFlip( true, false );
		}
		else if( forward == EnumFacing.NORTH && up == EnumFacing.EAST )
		{
			flippableIcon.setFlip( false, true );
		}
		else if( forward == EnumFacing.NORTH && up == EnumFacing.WEST )
		{
			flippableIcon.setFlip( true, false );
		}
		else if( forward == EnumFacing.DOWN && up == EnumFacing.EAST )
		{
			flippableIcon.setFlip( false, true );
		}
		else if( forward == EnumFacing.DOWN )
		{
			flippableIcon.setFlip( true, false );
		}

		/*
		 * 1.7.2
		 * else if ( forward == AEPartLocation.EAST && up == AEPartLocation.UP ) flippableIcon.setFlip( true, false );
		 * else if (
		 * forward == AEPartLocation.NORTH && up == AEPartLocation.UP ) flippableIcon.setFlip( true, false );
		 */

		this.renderFace( pos, imb, flippableIcon, renderer, forward );

		if( stat != 0 )
		{
			b = 0;
			if( sp.isPowered() )
			{
				b = 15 << 20 | 15 << 4;
			}

			renderer.setBrightness( b );
			if( stat == 1 )
			{
				renderer.setColorOpaque_I( 0x00ff00 );
			}
			if( stat == 2 )
			{
				renderer.setColorOpaque_I( 0xffaa00 );
			}
			if( stat == 3 )
			{
				renderer.setColorOpaque_I( 0xff0000 );
			}
			this.selectFace( renderer, west, up, forward, 9, 10, 11, 12 );
			this.renderFace( pos, imb, ExtraBlockTextures.White.getIcon(), renderer, forward );
		}

		b = world.getCombinedLight( pos.offset( up ), 0 );
		if( sp.isPowered() )
		{
			b = 15 << 20 | 15 << 4;
		}

		renderer.setBrightness( b );
		renderer.setColorOpaque_I( 0xffffff );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		final ICellHandler ch = AEApi.instance().registries().cell().getHandler( sp.getStorageType() );

		renderer.setColorOpaque_I( sp.getColor().whiteVariant );
		IAESprite ico = ch == null ? null : ch.getTopTexture_Light();
		this.renderFace( pos, imb, ico == null ? ExtraBlockTextures.MEChest.getIcon() : ico, renderer, up );

		if( ico != null )
		{
			renderer.setColorOpaque_I( sp.getColor().mediumVariant );
			ico = ch == null ? null : ch.getTopTexture_Medium();
			this.renderFace( pos, imb, ico == null ? ExtraBlockTextures.MEChest.getIcon() : ico, renderer, up );

			renderer.setColorOpaque_I( sp.getColor().blackVariant );
			ico = ch == null ? null : ch.getTopTexture_Dark();
			this.renderFace( pos, imb, ico == null ? ExtraBlockTextures.MEChest.getIcon() : ico, renderer, up );
		}

		renderer.overrideBlockTexture = null;
		this.postRenderInWorld( renderer );

		return result;
	}
}
