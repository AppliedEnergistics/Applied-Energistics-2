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

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MissingIcon implements IIcon
{

	final boolean isBlock;

	public MissingIcon(Object forWhat) {
		isBlock = forWhat instanceof Block;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getMissing()
	{
		return ((TextureMap) Minecraft.getMinecraft().getTextureManager()
				.getTexture( isBlock ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture )).getAtlasSprite( "missingno" );
	}

	@Override
	public int getIconWidth()
	{
		return getMissing().getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return getMissing().getIconHeight();
	}

	@Override
	public float getMinU()
	{
		return getMissing().getMinU();
	}

	@Override
	public float getMaxU()
	{
		return getMissing().getMaxU();
	}

	@Override
	public float getInterpolatedU(double var1)
	{
		return getMissing().getInterpolatedU( var1 );
	}

	@Override
	public float getMinV()
	{
		return getMissing().getMinV();
	}

	@Override
	public float getMaxV()
	{
		return getMissing().getMaxV();
	}

	@Override
	public float getInterpolatedV(double var1)
	{
		return getMissing().getInterpolatedV( var1 );
	}

	@Override
	public String getIconName()
	{
		return getMissing().getIconName();
	}

}
