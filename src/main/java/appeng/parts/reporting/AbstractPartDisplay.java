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
import appeng.client.render.ModelGenerator;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.IAESprite;


/**
 * A more sophisticated part overlapping all 3 textures.
 *
 * Subclass this if you need want a new part and need all 3 textures.
 * For more concrete implementations, the direct abstract subclasses might be a better alternative.
 *
 * @author AlgorithmX2
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractPartDisplay extends AbstractPartReporting
{

	public AbstractPartDisplay( ItemStack is )
	{
		super( is, true );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, ModelGenerator renderer )
	{
		rh.setBounds( 2, 2, 14, 14, 14, 16 );

		final IAESprite sideTexture = CableBusTextures.PartMonitorSides.getIcon();
		final IAESprite backTexture = CableBusTextures.PartMonitorBack.getIcon();

		rh.setTexture( sideTexture, sideTexture, backTexture, renderer.getIcon( this.is ), sideTexture, sideTexture );
		rh.renderInventoryBox( renderer );

		rh.setInvColor( this.getColor().whiteVariant );
		rh.renderInventoryFace( this.getFrontBright().getIcon(), EnumFacing.SOUTH, renderer );

		rh.setInvColor( this.getColor().mediumVariant );
		rh.renderInventoryFace( this.getFrontDark().getIcon(), EnumFacing.SOUTH, renderer );

		rh.setInvColor( this.getColor().blackVariant );
		rh.renderInventoryFace( this.getFrontColored().getIcon(), EnumFacing.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( BlockPos pos, IPartRenderHelper rh, ModelGenerator renderer )
	{
		final IAESprite sideTexture = CableBusTextures.PartMonitorSides.getIcon();
		final IAESprite backTexture = CableBusTextures.PartMonitorBack.getIcon();

		rh.setTexture( sideTexture, sideTexture, backTexture, renderer.getIcon( this.is ), sideTexture, sideTexture );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( pos, renderer );

		if( this.getLightLevel() > 0 )
		{
			final int l = 13;
			renderer.setBrightness( l << 20 | l << 4 );
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = this.getSpin();

		renderer.setColorOpaque_I( this.getColor().whiteVariant );
		rh.renderFace( pos, this.getFrontBright().getIcon(), EnumFacing.SOUTH, renderer );

		renderer.setColorOpaque_I( this.getColor().mediumVariant );
		rh.renderFace( pos, this.getFrontDark().getIcon(), EnumFacing.SOUTH, renderer );

		renderer.setColorOpaque_I( this.getColor().blackVariant );
		rh.renderFace( pos, this.getFrontColored().getIcon(), EnumFacing.SOUTH, renderer );

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

		final IAESprite sideStatusTexture = CableBusTextures.PartMonitorSidesStatus.getIcon();

		rh.setTexture( sideStatusTexture, sideStatusTexture, backTexture, renderer.getIcon( this.is ), sideStatusTexture, sideStatusTexture );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderBlock( pos, renderer );

		final boolean hasChan = ( this.getClientFlags() & ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) ) == ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG );
		final boolean hasPower = ( this.getClientFlags() & PartPanel.POWERED_FLAG ) == PartPanel.POWERED_FLAG;

		if( hasChan )
		{
			final int l = 14;
			renderer.setBrightness( l << 20 | l << 4 );
			renderer.setColorOpaque_I( this.getColor().blackVariant );
		}
		else if( hasPower )
		{
			final int l = 9;
			renderer.setBrightness( l << 20 | l << 4 );
			renderer.setColorOpaque_I( this.getColor().whiteVariant );
		}
		else
		{
			renderer.setBrightness( 0 );
			renderer.setColorOpaque_I( 0x000000 );
		}

		final IAESprite sideStatusLightTexture = CableBusTextures.PartMonitorSidesStatusLights.getIcon();

		rh.renderFace( pos, sideStatusLightTexture, EnumFacing.EAST, renderer );
		rh.renderFace( pos, sideStatusLightTexture, EnumFacing.WEST, renderer );
		rh.renderFace( pos, sideStatusLightTexture, EnumFacing.UP, renderer );
		rh.renderFace( pos, sideStatusLightTexture, EnumFacing.DOWN, renderer );
	}

	@Override
	public boolean isLightSource()
	{
		return false;
	}

}
