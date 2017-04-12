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

package appeng.items.misc;


import appeng.api.util.AEColor;
import appeng.client.render.items.PaintBallRender;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;

import java.util.EnumSet;
import java.util.List;


public class ItemPaintBall extends AEBaseItem
{

	private static final int DAMAGE_THRESHOLD = 20;

	public ItemPaintBall()
	{
		this.setFeature( EnumSet.of( AEFeature.PaintBalls ) );
		this.setHasSubtypes( true );

		if( Platform.isClient() )
		{
			MinecraftForgeClient.registerItemRenderer( this, new PaintBallRender() );
		}
	}

	@Override
	public String getItemStackDisplayName( final ItemStack is )
	{
		return super.getItemStackDisplayName( is ) + " - " + this.getExtraName( is );
	}

	private String getExtraName( final ItemStack is )
	{
		return ( is.getItemDamage() >= DAMAGE_THRESHOLD ? GuiText.Lumen.getLocal() + ' ' : "" ) + this.getColor( is );
	}

	public AEColor getColor( final ItemStack is )
	{
		int dmg = is.getItemDamage();
		if( dmg >= DAMAGE_THRESHOLD )
		{
			dmg -= DAMAGE_THRESHOLD;
		}

		if( dmg >= AEColor.values().length )
		{
			return AEColor.Transparent;
		}

		return AEColor.values()[dmg];
	}

	@Override
	protected void getCheckedSubItems( final Item sameItem, final CreativeTabs creativeTab, final List<ItemStack> itemStacks )
	{
		for( final AEColor c : AEColor.values() )
		{
			if( c != AEColor.Transparent )
			{
				itemStacks.add( new ItemStack( this, 1, c.ordinal() ) );
			}
		}

		for( final AEColor c : AEColor.values() )
		{
			if( c != AEColor.Transparent )
			{
				itemStacks.add( new ItemStack( this, 1, DAMAGE_THRESHOLD + c.ordinal() ) );
			}
		}
	}

	public boolean isLumen( final ItemStack is )
	{
		final int dmg = is.getItemDamage();
		return dmg >= DAMAGE_THRESHOLD;
	}
}
