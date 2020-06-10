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
 CompoundNBT item = new CompoundNBT();

 new ItemStack( Blocks.anvil ).writeToNBT( item );
 msg.setTag( "item", item );
 msg.setDouble( "weight", 32.0 );

 FMLInterModComms.sendMessage( "appliedenergistics2", "add-mattercannon-ammo", msg );

 */

package appeng.core.api.imc;


import appeng.core.AELog;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import appeng.api.AEApi;
import appeng.core.api.IIMCProcessor;
import net.minecraftforge.fml.InterModComms;


public class IMCMatterCannon implements IIMCProcessor
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
		final CompoundNBT item = msg.getCompound( "item" );

		final ItemStack ammo = ItemStack.read( item );
		final double weight = msg.getDouble( "weight" );

		if( ammo.isEmpty() )
		{
			throw new IllegalStateException( "invalid item in message " + m );
		}

		AEApi.instance().registries().matterCannon().registerAmmo( ammo, weight );
	}
}
