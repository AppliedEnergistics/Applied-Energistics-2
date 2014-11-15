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

FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-me", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-bc-power", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-ic2-power", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-redstone", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-fluid", new ItemStack( myBlockOrItem ) );
FMLInterModComms.sendMessage( "appliedenergistics2", "add-p2p-attunement-item", new ItemStack( myBlockOrItem ) );

 */
package appeng.core.api.imc;

import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.core.api.IIMCHandler;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCP2PAttunement implements IIMCHandler
{

	@Override
	public void post(IMCMessage m)
	{
		String key = m.key.substring( "add-p2p-attunement-".length() ).replace( '-', '_' ).toUpperCase();

		TunnelType type = TunnelType.valueOf( key );

		if ( type != null )
		{
			ItemStack is = m.getItemStackValue();
			if ( is != null )
				AEApi.instance().registries().p2pTunnel().addNewAttunement( is, type );
			else
				throw new RuntimeException( "invalid item" );
		}
		else
			throw new RuntimeException( "invalid type" );
	}

}
