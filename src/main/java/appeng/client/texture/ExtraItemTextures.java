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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;


public enum ExtraItemTextures
{
	White( "White" ), ItemPaintBallShimmer( "ItemPaintBallShimmer" ),

	ToolColorApplicatorTip_Medium( "ToolColorApplicatorTip_Medium" ),

	ToolColorApplicatorTip_Dark( "ToolColorApplicatorTip_Dark" ),

	ToolColorApplicatorTip_Light( "ToolColorApplicatorTip_Light" );

	private final String name;
	private IIcon IIcon;

	ExtraItemTextures( final String name )
	{
		this.name = name;
	}

	public static ResourceLocation GuiTexture( final String string )
	{
		return new ResourceLocation( "appliedenergistics2", "textures/" + string );
	}

	@SideOnly( Side.CLIENT )
	public static IIcon getMissing()
	{
		return ( (TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture( TextureMap.locationItemsTexture ) ).getAtlasSprite( "missingno" );
	}

	public String getName()
	{
		return this.name;
	}

	public IIcon getIcon()
	{
		return this.IIcon;
	}

	public void registerIcon( final TextureMap map )
	{
		this.IIcon = map.registerIcon( "appliedenergistics2:" + this.name );
	}
}
