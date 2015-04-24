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

import com.google.common.base.Optional;

import appeng.core.features.AEFeature;
import appeng.core.features.FeatureNameExtractor;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.ItemFeatureHandler;


public abstract class AEBaseItem extends Item implements IAEFeature
{
	private final String fullName;
	private final Optional<String> subName;
	private IFeatureHandler feature;

	public AEBaseItem()
	{
		this( Optional.<String>absent() );
		this.setNoRepair();
	}

	public AEBaseItem( Optional<String> subName )
	{
		this.subName = subName;
		this.fullName = new FeatureNameExtractor( this.getClass(), subName ).get();
	}

	@Override
	public String toString()
	{
		return this.fullName;
	}

	@Override
	public IFeatureHandler handler()
	{
		return this.feature;
	}

	@Override
	public void postInit()
	{
		// override!
	}

	public void setFeature( EnumSet<AEFeature> f )
	{
		this.feature = new ItemFeatureHandler( f, this, this, this.subName );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public final void addInformation( ItemStack stack, EntityPlayer player, List lines, boolean displayMoreInfo )
	{
		this.addCheckedInformation( stack, player, lines, displayMoreInfo );
	}

	@Override
	public boolean isBookEnchantable( ItemStack itemstack1, ItemStack itemstack2 )
	{
		return false;
	}

	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayMoreInfo )
	{
		super.addInformation( stack, player, lines, displayMoreInfo );
	}
}
