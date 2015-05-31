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

package appeng.items.tools.quartz;


import java.util.EnumSet;

import com.google.common.base.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.items.AEBaseItem;
import appeng.items.contents.QuartzKnifeObj;
import appeng.util.Platform;


public final class ToolQuartzCuttingKnife extends AEBaseItem implements IGuiItem
{
	private final AEFeature type;

	public ToolQuartzCuttingKnife( AEFeature type )
	{
		super( Optional.of( type.name() ) );

		this.type = type;
		this.setFeature( EnumSet.of( type, AEFeature.QuartzKnife ) );
		this.setMaxDamage( 50 );
		this.setMaxStackSize( 1 );
	}

	@Override
	public final boolean onItemUse( ItemStack is, EntityPlayer p, World w, int x, int y, int z, int s, float hitX, float hitY, float hitZ )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_QUARTZ_KNIFE );
		}
		return true;
	}

	@Override
	public final ItemStack onItemRightClick( ItemStack it, World w, EntityPlayer p )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_QUARTZ_KNIFE );
		}
		p.swingItem();
		return it;
	}

	@Override
	public final boolean doesContainerItemLeaveCraftingGrid( ItemStack par1ItemStack )
	{
		return false;
	}

	@Override
	public final boolean getIsRepairable( ItemStack a, ItemStack b )
	{
		return Platform.canRepair( this.type, a, b );
	}

	@Override
	public final boolean isRepairable()
	{
		return false;
	}

	@Override
	public final ItemStack getContainerItem( ItemStack itemStack )
	{
		itemStack.setItemDamage( itemStack.getItemDamage() + 1 );
		return itemStack;
	}

	@Override
	public final boolean hasContainerItem( ItemStack stack )
	{
		return true;
	}

	@Override
	public final IGuiItemObject getGuiObject( ItemStack is, World world, int x, int y, int z )
	{
		return new QuartzKnifeObj( is );
	}
}
