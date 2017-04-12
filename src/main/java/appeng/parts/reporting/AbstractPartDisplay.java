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


import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;


/**
 * A more sophisticated part overlapping all 3 textures.
 * <p>
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

	public AbstractPartDisplay( final ItemStack is )
	{
		super( is, true );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		rh.setBounds( 2, 2, 14, 14, 14, 16 );

		final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
		final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

		rh.setTexture( sideTexture, sideTexture, backTexture, this.getItemStack().getIconIndex(), sideTexture, sideTexture );
		rh.renderInventoryBox( renderer );

		rh.setInvColor( this.getColor().whiteVariant );
		rh.renderInventoryFace( this.getFrontBright().getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setInvColor( this.getColor().mediumVariant );
		rh.renderInventoryFace( this.getFrontDark().getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setInvColor( this.getColor().blackVariant );
		rh.renderInventoryFace( this.getFrontColored().getIcon(), ForgeDirection.SOUTH, renderer );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		this.setRenderCache( rh.useSimplifiedRendering( x, y, z, this, this.getRenderCache() ) );

		final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
		final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

		rh.setTexture( sideTexture, sideTexture, backTexture, this.getItemStack().getIconIndex(), sideTexture, sideTexture );

		rh.setBounds( 2, 2, 14, 14, 14, 16 );
		rh.renderBlock( x, y, z, renderer );

		if( this.getLightLevel() > 0 )
		{
			final int l = 13;
			Tessellator.instance.setBrightness( l << 20 | l << 4 );
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = this.getSpin();

		Tessellator.instance.setColorOpaque_I( this.getColor().whiteVariant );
		rh.renderFace( x, y, z, this.getFrontBright().getIcon(), ForgeDirection.SOUTH, renderer );

		Tessellator.instance.setColorOpaque_I( this.getColor().mediumVariant );
		rh.renderFace( x, y, z, this.getFrontDark().getIcon(), ForgeDirection.SOUTH, renderer );

		Tessellator.instance.setColorOpaque_I( this.getColor().blackVariant );
		rh.renderFace( x, y, z, this.getFrontColored().getIcon(), ForgeDirection.SOUTH, renderer );

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

		final IIcon sideStatusTexture = CableBusTextures.PartMonitorSidesStatus.getIcon();

		rh.setTexture( sideStatusTexture, sideStatusTexture, backTexture, this.getItemStack().getIconIndex(), sideStatusTexture, sideStatusTexture );

		rh.setBounds( 4, 4, 13, 12, 12, 14 );
		rh.renderBlock( x, y, z, renderer );

		final boolean hasChan = ( this.getClientFlags() & ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG ) ) == ( PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG );
		final boolean hasPower = ( this.getClientFlags() & PartPanel.POWERED_FLAG ) == PartPanel.POWERED_FLAG;

		if( hasChan )
		{
			final int l = 14;
			Tessellator.instance.setBrightness( l << 20 | l << 4 );
			Tessellator.instance.setColorOpaque_I( this.getColor().blackVariant );
		}
		else if( hasPower )
		{
			final int l = 9;
			Tessellator.instance.setBrightness( l << 20 | l << 4 );
			Tessellator.instance.setColorOpaque_I( this.getColor().whiteVariant );
		}
		else
		{
			Tessellator.instance.setBrightness( 0 );
			Tessellator.instance.setColorOpaque_I( 0x000000 );
		}

		final IIcon sideStatusLightTexture = CableBusTextures.PartMonitorSidesStatusLights.getIcon();

		rh.renderFace( x, y, z, sideStatusLightTexture, ForgeDirection.EAST, renderer );
		rh.renderFace( x, y, z, sideStatusLightTexture, ForgeDirection.WEST, renderer );
		rh.renderFace( x, y, z, sideStatusLightTexture, ForgeDirection.UP, renderer );
		rh.renderFace( x, y, z, sideStatusLightTexture, ForgeDirection.DOWN, renderer );
	}

	@Override
	public boolean isLightSource()
	{
		return false;
	}

}
