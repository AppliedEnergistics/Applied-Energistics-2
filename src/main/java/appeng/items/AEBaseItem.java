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

package appeng.items;


import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;


public class AEBaseItem extends Item implements IAEFeature
{
	final String featureFullName;
	final String featureSubName;
	AEFeatureHandler feature;

	@Override
	public String toString()
	{
		return featureFullName;
	}

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	public void setFeature( EnumSet<AEFeature> f )
	{
		feature = new AEFeatureHandler( f, this, featureSubName );
	}

	public AEBaseItem( Class c )
	{
		this( c, null );
		canRepair = false;
	}

	public AEBaseItem( Class c, String subName )
	{
		featureSubName = subName;
		featureFullName = AEFeatureHandler.getName( c, subName );
	}

	@Override
	public boolean isBookEnchantable( ItemStack itemstack1, ItemStack itemstack2 )
	{
		return false;
	}

	@Override
	public void postInit()
	{
		// override!
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public final void addInformation( ItemStack stack, EntityPlayer player, List lines, boolean displayAdditionalInformation )
	{
		this.addCheckedInformation( stack, player, lines, displayAdditionalInformation );
	}

	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayAdditionalInformation )
	{
		super.addInformation( stack, player, lines, displayAdditionalInformation );
	}
}
