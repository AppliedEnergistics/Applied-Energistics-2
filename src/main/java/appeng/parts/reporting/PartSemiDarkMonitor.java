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

package appeng.parts.reporting;


import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.render.IRenderHelper;
import appeng.client.texture.CableBusTextures;


public class PartSemiDarkMonitor extends PartMonitor
{
	public PartSemiDarkMonitor( ItemStack is )
	{
		super( is, false );

		this.notLightSource = false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, IRenderHelper renderer )
	{
		rh.setBounds( 2, 2, 14, 14, 14, 16 );

		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), renderer.getIcon(is), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );
		rh.renderInventoryBox( renderer );

		int light = this.getColor().whiteVariant;
		int dark = this.getColor().mediumVariant;
		rh.setInvColor( ( ( ( ( ( light >> 16 ) & 0xff ) + ( ( dark >> 16 ) & 0xff ) ) / 2 ) << 16 ) | ( ( ( ( ( light >> 8 ) & 0xff ) + ( ( dark >> 8 ) & 0xff ) ) / 2 ) << 8 ) | ( ( ( ( light ) & 0xff ) + ( ( dark ) & 0xff ) ) / 2 ) );
		rh.renderInventoryFace( this.frontBright.getIcon(), EnumFacing.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( BlockPos pos, IPartRenderHelper rh, IRenderHelper renderer )
	{
		rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), renderer.getIcon(is), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( pos, renderer );

		if( this.getLightLevel() > 0 )
		{
			int l = 13;
			renderer.setBrightness( l << 20 | l << 4 );
		}

		int light = this.getColor().whiteVariant;
		int dark = this.getColor().mediumVariant;
		renderer.setColorOpaque( ( ( ( light >> 16 ) & 0xff ) + ( ( dark >> 16 ) & 0xff ) ) / 2, ( ( ( light >> 8 ) & 0xff ) + ( ( dark >> 8 ) & 0xff ) ) / 2, ( ( ( light ) & 0xff ) + ( ( dark ) & 0xff ) ) / 2 );
		rh.renderFace( pos, this.frontBright.getIcon(), EnumFacing.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderBlock( pos, renderer );
	}
}
