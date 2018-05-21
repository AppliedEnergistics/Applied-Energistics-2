/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.container.implementations;


import java.util.ArrayList;

import com.google.gson.Gson;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.parts.reporting.PartFluidTerminal;


/**
 * @author BrockWS
 * @version rv6 - 12/05/2018
 * @since rv6 12/05/2018
 */
public class ContainerFluidTerm extends AEBaseContainer
{

	@GuiSync( 0 )
	public String fluids = "";

	private PartFluidTerminal terminal;

	public ContainerFluidTerm( InventoryPlayer ip, ITerminalHost terminalHost )
	{
		super( ip, terminalHost instanceof TileEntity ? (TileEntity) terminalHost : null, null );
		this.terminal = (PartFluidTerminal) terminalHost;
	}

	@Override
	public void detectAndSendChanges()
	{
		IItemList<IAEFluidStack> list = this.terminal.getInventory( AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) ).getStorageList();
		if (!list.isEmpty()) {
			Gson gson = new Gson();
			ArrayList<String> fluids = new ArrayList<>(  );

			for (IAEFluidStack stack : list) {
				fluids.add( stack.getFluidStack().writeToNBT( new NBTTagCompound() ).toString() );
			}
			this.fluids = gson.toJson( fluids );
		}
		super.detectAndSendChanges();
	}
}
