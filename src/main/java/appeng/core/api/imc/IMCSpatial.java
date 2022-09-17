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

 FMLInterModComms.sendMessage( "appliedenergistics2", "whitelist-spatial", "mymod.tileentities.MyTileEntity" );

 */

package appeng.core.api.imc;


import appeng.api.AEApi;
import appeng.core.AELog;
import appeng.core.api.IIMCProcessor;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;


public class IMCSpatial implements IIMCProcessor {

    @Override
    public void process(final IMCMessage m) {

        try {
            final Class classInstance = Class.forName(m.getStringValue());
            AEApi.instance().registries().movable().whiteListTileEntity(classInstance);
        } catch (final ClassNotFoundException e) {
            AELog.info("Bad Class Registered: " + m.getStringValue() + " by " + m.getSender());
        }
    }
}
