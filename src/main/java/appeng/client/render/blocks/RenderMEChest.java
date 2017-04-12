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


import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.api.util.AEColor;
import appeng.block.storage.BlockChest;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.FlippableIcon;
import appeng.client.texture.OffsetIcon;
import appeng.tile.storage.TileChest;
import appeng.util.Platform;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


public class RenderMEChest extends BaseBlockRender<BlockChest, TileChest>
{

	public RenderMEChest()
	{
		super( false, 0 );
	}

	@Override
	public void renderInventory( final BlockChest block, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		Tessellator.instance.setBrightness( 0 );

		renderer.overrideBlockTexture = ExtraBlockTextures.getMissing();
		this.renderInvBlock( EnumSet.of( ForgeDirection.SOUTH ), block, is, Tessellator.instance, 0x000000, renderer );

		renderer.overrideBlockTexture = ExtraBlockTextures.MEChest.getIcon();
		this.renderInvBlock( EnumSet.of( ForgeDirection.UP ), block, is, Tessellator.instance, this.adjustBrightness( AEColor.Transparent.whiteVariant, 0.7 ), renderer );

		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( final BlockChest imb, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		final TileChest sp = imb.getTileEntity( world, x, y, z );

		if( sp == null )
		{
			return false;
		}

		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		final ForgeDirection up = sp.getUp();
		final ForgeDirection forward = sp.getForward();
		final ForgeDirection west = Platform.crossProduct( forward, up );

		this.preRenderInWorld( imb, world, x, y, z, renderer );

		final int stat = sp.getCellStatus( 0 );
		final boolean result = renderer.renderStandardBlock( imb, x, y, z );

		this.selectFace( renderer, west, up, forward, 5, 16 - 5, 9, 12 );

		int offsetV = 8;
		if( stat == 0 )
		{
			offsetV = 3;
		}

		int b = world.getLightBrightnessForSkyBlocks( x + forward.offsetX, y + forward.offsetY, z + forward.offsetZ, 0 );
		Tessellator.instance.setBrightness( b );
		Tessellator.instance.setColorOpaque_I( 0xffffff );

		final int offsetU = -4;
		final FlippableIcon flippableIcon = new FlippableIcon( new OffsetIcon( ExtraBlockTextures.MEStorageCellTextures.getIcon(), offsetU, offsetV ) );

		if( forward == ForgeDirection.EAST && ( up == ForgeDirection.NORTH || up == ForgeDirection.SOUTH ) )
		{
			flippableIcon.setFlip( true, false );
		}
		else if( forward == ForgeDirection.NORTH && up == ForgeDirection.EAST )
		{
			flippableIcon.setFlip( false, true );
		}
		else if( forward == ForgeDirection.NORTH && up == ForgeDirection.WEST )
		{
			flippableIcon.setFlip( true, false );
		}
		else if( forward == ForgeDirection.DOWN && up == ForgeDirection.EAST )
		{
			flippableIcon.setFlip( false, true );
		}
		else if( forward == ForgeDirection.DOWN )
		{
			flippableIcon.setFlip( true, false );
		}

		this.renderFace( x, y, z, imb, flippableIcon, renderer, forward );

		if( stat != 0 )
		{
			b = 0;
			if( sp.isPowered() )
			{
				b = 15 << 20 | 15 << 4;
			}

			Tessellator.instance.setBrightness( b );
			if( stat == 1 )
			{
				Tessellator.instance.setColorOpaque_I( 0x00ff00 );
			}
			if( stat == 2 )
			{
				Tessellator.instance.setColorOpaque_I( 0xffaa00 );
			}
			if( stat == 3 )
			{
				Tessellator.instance.setColorOpaque_I( 0xff0000 );
			}
			this.selectFace( renderer, west, up, forward, 9, 10, 11, 12 );
			this.renderFace( x, y, z, imb, ExtraBlockTextures.White.getIcon(), renderer, forward );
		}

		b = world.getLightBrightnessForSkyBlocks( x + up.offsetX, y + up.offsetY, z + up.offsetZ, 0 );
		if( sp.isPowered() )
		{
			b = 15 << 20 | 15 << 4;
		}

		Tessellator.instance.setBrightness( b );
		Tessellator.instance.setColorOpaque_I( 0xffffff );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		final ICellHandler ch = AEApi.instance().registries().cell().getHandler( sp.getStorageType() );

		Tessellator.instance.setColorOpaque_I( sp.getColor().whiteVariant );
		IIcon ico = ch == null ? null : ch.getTopTexture_Light();
		this.renderFace( x, y, z, imb, ico == null ? ExtraBlockTextures.MEChest.getIcon() : ico, renderer, up );

		if( ico != null )
		{
			Tessellator.instance.setColorOpaque_I( sp.getColor().mediumVariant );
			ico = ch == null ? null : ch.getTopTexture_Medium();
			this.renderFace( x, y, z, imb, ico == null ? ExtraBlockTextures.MEChest.getIcon() : ico, renderer, up );

			Tessellator.instance.setColorOpaque_I( sp.getColor().blackVariant );
			ico = ch == null ? null : ch.getTopTexture_Dark();
			this.renderFace( x, y, z, imb, ico == null ? ExtraBlockTextures.MEChest.getIcon() : ico, renderer, up );
		}

		renderer.overrideBlockTexture = null;
		this.postRenderInWorld( renderer );

		return result;
	}
}
