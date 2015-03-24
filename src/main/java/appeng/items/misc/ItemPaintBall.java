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

package appeng.items.misc;


import java.util.EnumSet;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;

import appeng.api.util.AEColor;
import appeng.client.render.items.PaintBallRender;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


public class ItemPaintBall extends AEBaseItem
{

	public ItemPaintBall()
	{
		super( ItemPaintBall.class );
		this.setFeature( EnumSet.of( AEFeature.PaintBalls ) );
		this.hasSubtypes = true;
		if( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( this, new PaintBallRender() );
	}

	@Override
	public String getItemStackDisplayName( ItemStack is )
	{
		return super.getItemStackDisplayName( is ) + " - " + this.getExtraName( is );
	}

	public String getExtraName( ItemStack is )
	{
		return ( is.getItemDamage() >= 20 ? GuiText.Lumen.getLocal() + ' ' : "" ) + this.getColor( is );
	}

	public AEColor getColor( ItemStack is )
	{
		int dmg = is.getItemDamage();
		if( dmg >= 20 )
			dmg -= 20;

		if( dmg >= AEColor.values().length )
			return AEColor.Transparent;

		return AEColor.values()[dmg];
	}

	@Override
	public void getSubItems( Item i, CreativeTabs ct, List l )
	{
		for( AEColor c : AEColor.values() )
			if( c != AEColor.Transparent )
				l.add( new ItemStack( this, 1, c.ordinal() ) );

		for( AEColor c : AEColor.values() )
			if( c != AEColor.Transparent )
				l.add( new ItemStack( this, 1, 20 + c.ordinal() ) );
	}

	public boolean isLumen( ItemStack is )
	{
		int dmg = is.getItemDamage();
		return dmg >= 20;
	}
}
