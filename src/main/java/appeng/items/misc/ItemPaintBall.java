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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.api.util.AEColor;
import appeng.client.ClientHelper;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;


public class ItemPaintBall extends AEBaseItem
{

	public static final int DAMAGE_THRESHOLD = 20;

	public ItemPaintBall()
	{
		this.setFeature( EnumSet.of( AEFeature.PaintBalls ) );
		this.setHasSubtypes( true );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons( ClientHelper ir, String name )
	{
		final ModelResourceLocation sloc = ir.setIcon( this, name + "Shimmer" );
		final ModelResourceLocation loc = ir.setIcon( this, name );
		
		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( this, new ItemMeshDefinition(){
			
			@Override
			public ModelResourceLocation getModelLocation(
					ItemStack stack )
			{
				if ( isLumen(stack) )
					return sloc;
				
				return loc;
			}
		});
	}
	
	@Override
	public String getItemStackDisplayName( ItemStack is )
	{
		return super.getItemStackDisplayName( is ) + " - " + this.getExtraName( is );
	}

	public String getExtraName( ItemStack is )
	{
		return ( is.getItemDamage() >= DAMAGE_THRESHOLD ? GuiText.Lumen.getLocal() + ' ' : "" ) + this.getColor( is );
	}
	
	@Override
	public int getColorFromItemStack(
			ItemStack stack,
			int renderPass )
	{
		AEColor col = getColor(stack);
		
		int colorValue = stack.getItemDamage() >= 20 ? col.mediumVariant : col.mediumVariant;
		int r = ( colorValue >> 16 ) & 0xff;
		int g = ( colorValue >> 8 ) & 0xff;
		int b = ( colorValue ) & 0xff;
	
		int full = (int) ( 255 * 0.3 );
		float fail = 0.7f;
	
		if( stack.getItemDamage() >= 20 )
		{
			return  (int)( full + r * fail ) << 16 |  (int)( full + g * fail ) << 8 |  (int)( full + b * fail ) | 0xff << 24;
		}
		else
		{
			return r << 16 | g << 8 | b | 0xff << 24 ;
		}
	}

	public AEColor getColor( ItemStack is )
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
	public void getSubItems( Item i, CreativeTabs ct, List l )
	{
		for( AEColor c : AEColor.values() )
		{
			if( c != AEColor.Transparent )
			{
				l.add( new ItemStack( this, 1, c.ordinal() ) );
			}
		}

		for( AEColor c : AEColor.values() )
		{
			if( c != AEColor.Transparent )
			{
				l.add( new ItemStack( this, 1, DAMAGE_THRESHOLD + c.ordinal() ) );
			}
		}
	}

	public boolean isLumen( ItemStack is )
	{
		int dmg = is.getItemDamage();
		return dmg >= DAMAGE_THRESHOLD;
	}
}
