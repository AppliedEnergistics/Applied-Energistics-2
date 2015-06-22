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
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.block.misc.BlockSecurity;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.IAESprite;
import appeng.tile.misc.TileSecurity;


public class RendererSecurity extends BaseBlockRender<BlockSecurity, TileSecurity>
{

	public RendererSecurity()
	{
		super( false, 0 );
	}

	@Override
	public void renderInventory( BlockSecurity block, ItemStack is, ModelGenerator renderer, ItemRenderType type, Object[] obj )
	{
		renderer.overrideBlockTexture = ExtraBlockTextures.getMissing();
		this.renderInvBlock( EnumSet.of( AEPartLocation.SOUTH ), block, is, 0x000000, renderer );

		renderer.overrideBlockTexture = ExtraBlockTextures.MEChest.getIcon();
		this.renderInvBlock( EnumSet.of( AEPartLocation.UP ), block, is, this.adjustBrightness( AEColor.Transparent.whiteVariant, 0.7 ), renderer );

		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( BlockSecurity imb, IBlockAccess world, BlockPos pos, ModelGenerator renderer )
	{
		TileSecurity sp = imb.getTileEntity( world, pos );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		EnumFacing up = sp.getUp();

		this.preRenderInWorld( imb, world, pos, renderer );

		boolean result = renderer.renderStandardBlock( imb, pos );

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

		renderer.overrideBlockTexture = null;
		this.postRenderInWorld( renderer );

		return result;
	}
}
