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

package appeng.fluids.container;


import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketFluidTank;
import appeng.fluids.container.slots.SlotFakeFluid;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.util.Platform;


public class ContainerFluidInterface extends AEBaseContainer
{

	private final DualityFluidInterface myDuality;

	public ContainerFluidInterface( final InventoryPlayer ip, final IFluidInterfaceHost te )
	{
		super( ip, (TileEntity) ( te instanceof TileEntity ? te : null ), (IPart) ( te instanceof IPart ? te : null ) );

		this.myDuality = te.getDualityFluidInterface();

		for( int x = 0; x < DualityFluidInterface.NUMBER_OF_TANKS; x++ )
		{
			this.addSlotToContainer( new SlotFakeFluid( this.myDuality.getConfig(), x, 8 + 18 * x, 115 ) );
		}

		this.bindPlayerInventory( ip, 0, 231 - /* height of player inventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if( Platform.isServer() )
		{
			sendTankInfo();
		}

		super.detectAndSendChanges();
	}

	public void sendTankInfo()
	{
		final HashMap<Integer, NBTTagCompound> updateMap = new HashMap<>();
		if( this.myDuality.writeTankInfo( updateMap ) )
		{
			for( final IContainerListener listener : this.listeners )
			{
				NetworkHandler.instance().sendTo( new PacketFluidTank( updateMap ), (EntityPlayerMP) listener );
			}
		}
	}

	public void receiveTankInfo( final Map<Integer, NBTTagCompound> tankTags )
	{
		this.myDuality.readTankInfo( tankTags );
	}

	@Override
	public boolean isValidForSlot( Slot s, ItemStack i )
	{
		return s instanceof SlotFakeFluid ? i.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) : super.isValidForSlot( s, i );
	}
}
