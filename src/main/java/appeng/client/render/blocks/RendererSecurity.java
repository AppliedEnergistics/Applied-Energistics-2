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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IAESprite;
import appeng.api.util.ModelGenerator;
import appeng.block.misc.BlockSecurity;
import appeng.client.ItemRenderType;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.misc.TileSecurity;


public class RendererSecurity extends BaseBlockRender<BlockSecurity, TileSecurity>
{

	public RendererSecurity()
	{
		super( false, 0 );
	}

	@Override
	public void renderInventory( final BlockSecurity block, final ItemStack is, final ModelGenerator renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.setOverrideBlockTexture( ExtraBlockTextures.getMissing() );
		this.renderInvBlock( EnumSet.of( AEPartLocation.SOUTH ), block, is, 0x000000, renderer );

		renderer.setOverrideBlockTexture( ExtraBlockTextures.MEChest.getIcon() );
		this.renderInvBlock( EnumSet.of( AEPartLocation.UP ), block, is, this.adjustBrightness( AEColor.Transparent.whiteVariant, 0.7 ), renderer );

		renderer.setOverrideBlockTexture( null );
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( final BlockSecurity imb, final IBlockAccess world, final BlockPos pos, final ModelGenerator renderer )
	{
		final TileSecurity sp = imb.getTileEntity( world, pos );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		final EnumFacing up = sp.getUp();

		this.preRenderInWorld( imb, world, pos, renderer );

		final boolean result = renderer.renderStandardBlock( imb, pos );

		int b = world.getCombinedLight( pos.offset( up ), 0 );
		if( sp.isActive() )
		{
			b = 15 << 20 | 15 << 4;
		}

		renderer.setBrightness( b );
		renderer.setColorOpaque_I( 0xffffff );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		renderer.setColorOpaque_I( sp.getColor().whiteVariant );
		IAESprite ico = sp.isActive() ? ExtraBlockTextures.BlockMESecurityOn_Light.getIcon() : ExtraBlockTextures.MEChest.getIcon();
		this.renderFace( pos, imb, ico, renderer, up );
		if( sp.isActive() )
		{
			renderer.setColorOpaque_I( sp.getColor().mediumVariant );
			ico = sp.isActive() ? ExtraBlockTextures.BlockMESecurityOn_Medium.getIcon() : ExtraBlockTextures.MEChest.getIcon();
			this.renderFace( pos, imb, ico, renderer, up );

			renderer.setColorOpaque_I( sp.getColor().blackVariant );
			ico = sp.isActive() ? ExtraBlockTextures.BlockMESecurityOn_Dark.getIcon() : ExtraBlockTextures.MEChest.getIcon();
			this.renderFace( pos, imb, ico, renderer, up );
		}

		renderer.setOverrideBlockTexture( null );
		this.postRenderInWorld( renderer );

		return result;
	}
}
