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
import appeng.api.util.AEColor;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.IAESprite;


/**
 * A very simple part for emitting light.
 *
 * Opposed to the other subclass of {@link AbstractPartReporting}, it will only use the bright front texture.
 *
 * @author AlgorithmX2
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractPartPanel extends AbstractPartReporting
{
	private static final CableBusTextures FRONT_BRIGHT_ICON = CableBusTextures.PartMonitor_Bright;
	private static final CableBusTextures FRONT_DARK_ICON = CableBusTextures.PartMonitor_Colored;
	private static final CableBusTextures FRONT_COLORED_ICON = CableBusTextures.PartMonitor_Colored;

	public AbstractPartPanel( final ItemStack is )
	{
		super( is, false );
	}

	@Override
	public CableBusTextures getFrontBright()
	{
		return FRONT_BRIGHT_ICON;
	}

	@Override
	public CableBusTextures getFrontColored()
	{
		return FRONT_COLORED_ICON;
	}

	@Override
	public CableBusTextures getFrontDark()
	{
		return FRONT_DARK_ICON;
	}

	@Override
	public boolean isLightSource()
	{
		return true;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( final IPartRenderHelper rh, final ModelGenerator renderer )
	{
		rh.setBounds( 2, 2, 14, 14, 14, 16 );

		final IAESprite sideTexture = CableBusTextures.PartMonitorSides.getIcon();
		final IAESprite backTexture = CableBusTextures.PartMonitorBack.getIcon();

		rh.setTexture( sideTexture, sideTexture, backTexture, renderer.getIcon( this.is ), sideTexture, sideTexture );
		rh.renderInventoryBox( renderer );

		rh.setInvColor( this.getBrightnessColor() );
		rh.renderInventoryFace( this.getFrontBright().getIcon(), EnumFacing.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( final BlockPos pos, final IPartRenderHelper rh, final ModelGenerator renderer )
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

		renderer.setColorOpaque_I( this.getBrightnessColor() );
		rh.renderFace( pos, this.getFrontBright().getIcon(), EnumFacing.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderBlock( pos, renderer );
	}

	/**
	 * How bright the color the panel should appear. Usually it depends on a {@link AEColor} variant.
	 * This does not affect the actual light level of the part.
	 *
	 * @return the brightness to be used.
	 */
	protected abstract int getBrightnessColor();

}
