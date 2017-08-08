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

package appeng.debug;


import appeng.client.texture.MissingIcon;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import appeng.worldgen.MeteoritePlacer;
import appeng.worldgen.meteorite.StandardWorld;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;


public class ToolMeteoritePlacer extends AEBaseItem
{
	public ToolMeteoritePlacer()
	{
		this.setFeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
	}

	@Override
	public void registerIcons( final IIconRegister par1IconRegister )
	{
		this.itemIcon = new MissingIcon( this );
	}

	@Override
	public boolean onItemUseFirst( final ItemStack stack, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( ForgeEventFactory.onItemUseStart( player, stack, 1 ) <= 0 )
			return true;

		if( Platform.isClient() )
		{
			return false;
		}

		final MeteoritePlacer mp = new MeteoritePlacer();
		final boolean worked = mp.spawnMeteorite( new StandardWorld( world ), x, y, z );

		if( !worked )
		{
			player.addChatMessage( new ChatComponentText( "Un-suitable Location." ) );
		}

		return true;
	}
}
