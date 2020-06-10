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

package appeng.items;


import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public abstract class AEBaseItem extends Item
{

	public AEBaseItem(Item.Properties properties)
	{
		super(properties);
	}

	public AEBaseItem()
	{
		this(new Properties().setNoRepair());
	}

	@Override
	public String toString()
	{
		String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
		return this.getClass().getSimpleName() + "[" + regName + "]";
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	@SuppressWarnings( "unchecked" )
	public final void addInformation( final ItemStack stack, final World world, final List lines, final ITooltipFlag advancedTooltips )
	{
		this.addCheckedInformation( stack, world, lines, advancedTooltips );
	}

	@Override
	public boolean isBookEnchantable( final ItemStack itemstack1, final ItemStack itemstack2 )
	{
		return false;
	}

	@OnlyIn( Dist.CLIENT )
	protected void addCheckedInformation( final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips )
	{
		super.addInformation( stack, world, lines, advancedTooltips );
	}

}
