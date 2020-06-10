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

/* Example:

 CompoundNBT msg = new CompoundNBT();
 CompoundNBT in = new CompoundNBT();
 CompoundNBT out = new CompoundNBT();

 new ItemStack( Blocks.iron_ore ).writeToNBT( in );
 new ItemStack( Items.iron_ingot ).writeToNBT( out );
 msg.setTag( "in", in );
 msg.setTag( "out", out );
 msg.setInteger( "turns", 8 );

 FMLInterModComms.sendMessage( "appliedenergistics2", "add-grindable", msg );

 -- or --

 CompoundNBT msg = new CompoundNBT();
 CompoundNBT in = new CompoundNBT();
 CompoundNBT out = new CompoundNBT();
 CompoundNBT optional = new CompoundNBT();

 new ItemStack( Blocks.iron_ore ).writeToNBT( in );
 new ItemStack( Items.iron_ingot ).writeToNBT( out );
 new ItemStack( Blocks.gravel ).writeToNBT( optional );
 msg.setTag( "in", in );
 msg.setTag( "out", out );
 msg.setTag( "optional", optional );
 msg.setFloat( "chance", 0.5 );
 msg.setInteger( "turns", 8 );

 FMLInterModComms.sendMessage( "appliedenergistics2", "add-grindable", msg );

 */

package appeng.core.api.imc;


import appeng.core.AELog;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import appeng.api.AEApi;
import appeng.api.features.IGrinderRecipe;
import appeng.api.features.IGrinderRecipeBuilder;
import appeng.api.features.IGrinderRegistry;
import appeng.core.api.IIMCProcessor;
import net.minecraftforge.fml.InterModComms;


public class IMCGrinder implements IIMCProcessor
{
	@Override
	public void process( final InterModComms.IMCMessage m )
	{
		final Object messageArg = m.getMessageSupplier().get();

		if( !( messageArg instanceof CompoundNBT) )
		{
			AELog.warn( "Bad argument for %1$2 by Mod %2$s: expected instance of CompoundNBT got %3$s", this.getClass().getSimpleName(), m.getSenderModId(), messageArg.getClass().getSimpleName() );
			return;
		}

		final CompoundNBT msg = (CompoundNBT) messageArg;
		final CompoundNBT inTag = msg.getCompound( "in" );
		final CompoundNBT outTag = msg.getCompound( "out" );

		final ItemStack in = ItemStack.read( inTag );
		final ItemStack out = ItemStack.read( outTag );

		final int turns = msg.getInt( "turns" );

		if( in.isEmpty() )
		{
			throw new IllegalStateException( "invalid input" );
		}

		if( out.isEmpty() )
		{
			throw new IllegalStateException( "invalid output" );
		}

		if( msg.contains( "optional" ) )
		{
			final CompoundNBT optionalTag = msg.getCompound( "optional" );
			final ItemStack optional = ItemStack.read( optionalTag );

			if( optional.isEmpty() )
			{
				throw new IllegalStateException( "invalid optional" );
			}

			final float chance = msg.getFloat( "chance" );
			final IGrinderRegistry grinderRegistry = AEApi.instance().registries().grinder();
			final IGrinderRecipeBuilder builder = grinderRegistry.builder();
			final IGrinderRecipe grinderRecipe = builder.withInput( in )
					.withOutput( out )
					.withFirstOptional( optional, chance )
					.withTurns( turns )
					.build();

			grinderRegistry.addRecipe( grinderRecipe );
		}
		else
		{
			final IGrinderRegistry grinderRegistry = AEApi.instance().registries().grinder();
			final IGrinderRecipeBuilder builder = grinderRegistry.builder();
			final IGrinderRecipe grinderRecipe = builder.withInput( in )
					.withOutput( out )
					.withTurns( turns )
					.build();

			grinderRegistry.addRecipe( grinderRecipe );
		}
	}
}
