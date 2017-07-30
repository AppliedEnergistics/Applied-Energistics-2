/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2017, tyra314, All rights reserved.
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

package appeng.helpers;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import appeng.util.Platform;


public class InvalidPatternHelper
{

	private final List<String> outputs = new ArrayList<>();
	private final List<String> inputs = new ArrayList<>();
	private final boolean isCrafting;
	private final boolean canSubstitute;

	public InvalidPatternHelper( final ItemStack is, final World w )
	{
		final NBTTagCompound encodedValue = is.getTagCompound();

		if( encodedValue == null )
		{
			throw new IllegalArgumentException( "No pattern here!" );
		}

		final NBTTagList inTag = encodedValue.getTagList( "in", 10 );
		final NBTTagList outTag = encodedValue.getTagList( "out", 10 );
		this.isCrafting = encodedValue.getBoolean( "crafting" );

		this.canSubstitute = this.isCrafting && encodedValue.getBoolean( "substitute" );

		for( int i = 0; i < outTag.tagCount(); i++ )
		{
			NBTTagCompound out = outTag.getCompoundTagAt( i );

			ItemStack stack = new ItemStack( out );
			if( stack.isEmpty() )
			{
				outputs.add( TextFormatting.RED + String.valueOf( out.getByte( "Count" ) ) + ' ' + out.getString( "id" ) + '@' + String.valueOf( Math.max( 0, out.getShort( "Damage" ) ) ) );
			}
			else
			{
				outputs.add( String.valueOf( stack.getCount() ) + ' ' + Platform.getItemDisplayName( stack ) );
			}
		}

		for( int i = 0; i < inTag.tagCount(); i++ )
		{
			NBTTagCompound in = inTag.getCompoundTagAt( i );

			// skip empty slots in the crafting grid
			if( in.hasNoTags() )
			{
				continue;
			}

			ItemStack stack = new ItemStack( in );
			if( stack.isEmpty() )
			{
				inputs.add( TextFormatting.RED + String.valueOf( in.getByte( "Count" ) ) + ' ' + in.getString( "id" ) + '@' + String.valueOf( Math.max( 0, in.getShort( "Damage" ) ) ) );
			}
			else
			{
				inputs.add( String.valueOf( stack.getCount() ) + ' ' + Platform.getItemDisplayName( stack ) );
			}
		}
	}

	public List<String> getOutputs()
	{
		return this.outputs;
	}

	public List<String> getInputs()
	{
		return this.inputs;
	}

	public boolean isCraftable()
	{
		return this.isCrafting;
	}

	public boolean canSubstitute()
	{
		return this.canSubstitute;
	}
}
