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

package appeng.parts.p2p;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import appeng.api.config.TunnelType;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartP2PRedstone extends PartP2PTunnel<PartP2PRedstone>
{

	@Override
	public TunnelType getTunnelType()
	{
		return TunnelType.REDSTONE;
	}

	public PartP2PRedstone(ItemStack is) {
		super( is );
	}

	int power;

	@Override
	public boolean canConnectRedstone()
	{
		return true;
	}

	@Override
	public int isProvidingStrongPower()
	{
		return output ? power : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return output ? power : 0;
	}

	@Override
	public void onTunnelNetworkChange()
	{
		setNetworkReady();
	}

	@MENetworkEventSubscribe
	public void changeStateA(MENetworkBootingStatusChange bs)
	{
		setNetworkReady();
	}

	@MENetworkEventSubscribe
	public void changeStateB(MENetworkChannelsChanged bs)
	{
		setNetworkReady();
	}

	@MENetworkEventSubscribe
	public void changeStateC(MENetworkPowerStatusChange bs)
	{
		setNetworkReady();
	}

	public void setNetworkReady()
	{
		if ( output )
		{
			PartP2PRedstone in = getInput();
			if ( in != null )
				putInput( in.power );
		}
	}

	boolean recursive = false;

	protected void putInput(Object o)
	{
		if ( recursive )
			return;

		recursive = true;
		if ( output && proxy.isActive() )
		{
			int newPower = (Integer) o;
			if ( power != newPower )
			{
				power = newPower;
				notifyNeighbors();
			}
		}
		recursive = false;

	}

	public void notifyNeighbors()
	{
		World worldObj = tile.getWorldObj();

		int xCoord = tile.xCoord;
		int yCoord = tile.yCoord;
		int zCoord = tile.zCoord;

		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord, zCoord );

		// and this cause sometimes it can go thought walls.
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord - 1, yCoord, zCoord );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord - 1, zCoord );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord, zCoord - 1 );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord, zCoord + 1 );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord, yCoord + 1, zCoord );
		Platform.notifyBlocksOfNeighbors( worldObj, xCoord + 1, yCoord, zCoord );
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT( tag );
		tag.setInteger( "power", power );
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT( tag );
		power = tag.getInteger( "power" );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.redstone_block.getBlockTextureFromSide( 0 );
	}

	public float getPowerDrainPerTick()
	{
		return 0.5f;
	}

	@Override
	public void onNeighborChanged()
	{
		if ( !output )
		{
			int x = tile.xCoord + side.offsetX;
			int y = tile.yCoord + side.offsetY;
			int z = tile.zCoord + side.offsetZ;

			Block b = tile.getWorldObj().getBlock( x, y, z );
			if ( b != null && !output )
			{
				int srcSide = side.ordinal();
				if ( b instanceof BlockRedstoneWire )
					srcSide = 1;
				power = b.isProvidingStrongPower( tile.getWorldObj(), x, y, z, srcSide );
				power = Math.max( power, b.isProvidingWeakPower( tile.getWorldObj(), x, y, z, srcSide ) );
				sendToOutput( power );
			}
			else
				sendToOutput( 0 );
		}
	}

	private void sendToOutput(int power)
	{
		try
		{
			for (PartP2PRedstone rs : getOutputs())
			{
				rs.putInput( power );
			}
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

}
