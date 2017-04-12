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

package appeng.client.texture;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;


public class MissingIcon implements IIcon
{

	private final boolean isBlock;

	public MissingIcon( final Object forWhat )
	{
		this.isBlock = forWhat instanceof Block;
	}

	@Override
	public int getIconWidth()
	{
		return this.getMissing().getIconWidth();
	}

	@SideOnly( Side.CLIENT )
	private IIcon getMissing()
	{
		return ( (TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture( this.isBlock ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture ) ).getAtlasSprite( "missingno" );
	}

	@Override
	public int getIconHeight()
	{
		return this.getMissing().getIconHeight();
	}

	@Override
	public float getMinU()
	{
		return this.getMissing().getMinU();
	}

	@Override
	public float getMaxU()
	{
		return this.getMissing().getMaxU();
	}

	@Override
	public float getInterpolatedU( final double var1 )
	{
		return this.getMissing().getInterpolatedU( var1 );
	}

	@Override
	public float getMinV()
	{
		return this.getMissing().getMinV();
	}

	@Override
	public float getMaxV()
	{
		return this.getMissing().getMaxV();
	}

	@Override
	public float getInterpolatedV( final double var1 )
	{
		return this.getMissing().getInterpolatedV( var1 );
	}

	@Override
	public String getIconName()
	{
		return this.getMissing().getIconName();
	}
}
