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

package appeng.items.tools.quartz;


import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.items.AEBaseItem;
import appeng.items.contents.QuartzKnifeObj;
import appeng.util.Platform;
import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


public class ToolQuartzCuttingKnife extends AEBaseItem implements IGuiItem
{
	private final AEFeature type;

	public ToolQuartzCuttingKnife( final AEFeature type )
	{
		super( Optional.of( type.name() ) );

		this.type = type;
		this.setFeature( EnumSet.of( type, AEFeature.QuartzKnife ) );
		this.setMaxDamage( 50 );
		this.setMaxStackSize( 1 );
	}

	@Override
	public boolean onItemUse( final ItemStack is, final EntityPlayer p, final World w, final int x, final int y, final int z, final int s, final float hitX, final float hitY, final float hitZ )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_QUARTZ_KNIFE );
		}
		return true;
	}

	@Override
	public ItemStack onItemRightClick( final ItemStack it, final World w, final EntityPlayer p )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_QUARTZ_KNIFE );
		}
		p.swingItem();
		return it;
	}

	@Override
	public boolean doesContainerItemLeaveCraftingGrid( final ItemStack par1ItemStack )
	{
		return false;
	}

	@Override
	public boolean getIsRepairable( final ItemStack a, final ItemStack b )
	{
		return Platform.canRepair( this.type, a, b );
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	@Override
	public ItemStack getContainerItem( final ItemStack itemStack )
	{
		itemStack.setItemDamage( itemStack.getItemDamage() + 1 );
		return itemStack;
	}

	@Override
	public boolean hasContainerItem( final ItemStack stack )
	{
		return true;
	}

	@Override
	public IGuiItemObject getGuiObject( final ItemStack is, final World world, final int x, final int y, final int z )
	{
		return new QuartzKnifeObj( is );
	}
}
