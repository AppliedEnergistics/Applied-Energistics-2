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

package appeng.client.texture;


import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public final class MissingIcon implements IIcon
{

	final boolean isBlock;

	public MissingIcon( Object forWhat )
	{
		this.isBlock = forWhat instanceof Block;
	}

	@Override
	public final int getIconWidth()
	{
		return this.getMissing().getIconWidth();
	}

	@SideOnly( Side.CLIENT )
	public final IIcon getMissing()
	{
		return ( (TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture( this.isBlock ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture ) ).getAtlasSprite( "missingno" );
	}

	@Override
	public final int getIconHeight()
	{
		return this.getMissing().getIconHeight();
	}

	@Override
	public final float getMinU()
	{
		return this.getMissing().getMinU();
	}

	@Override
	public final float getMaxU()
	{
		return this.getMissing().getMaxU();
	}

	@Override
	public final float getInterpolatedU( double var1 )
	{
		return this.getMissing().getInterpolatedU( var1 );
	}

	@Override
	public final float getMinV()
	{
		return this.getMissing().getMinV();
	}

	@Override
	public final float getMaxV()
	{
		return this.getMissing().getMaxV();
	}

	@Override
	public final float getInterpolatedV( double var1 )
	{
		return this.getMissing().getInterpolatedV( var1 );
	}

	@Override
	public final String getIconName()
	{
		return this.getMissing().getIconName();
	}
}
