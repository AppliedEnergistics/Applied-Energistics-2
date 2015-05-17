/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.networking;


import java.util.EnumSet;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergyGridProvider;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.AEBasePart;


public final class PartQuartzFiber extends AEBasePart implements IEnergyGridProvider
{

	final AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", this.proxy.getMachineRepresentation(), true );

	public PartQuartzFiber( ItemStack is )
	{
		super( is );
		this.proxy.setIdlePowerUsage( 0 );
		this.proxy.setFlags( GridFlags.CANNOT_CARRY );
		this.outerProxy.setIdlePowerUsage( 0 );
		this.outerProxy.setFlags( GridFlags.CANNOT_CARRY );
	}

	@Override
	public final AECableType getCableConnectionType( ForgeDirection dir )
	{
		return AECableType.GLASS;
	}

	@Override
	public final void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 6, 6, 10, 10, 10, 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public final void renderInventory( IPartRenderHelper rh, RenderBlocks renderer )
	{
		GL11.glTranslated( -0.2, -0.3, 0.0 );

		rh.setTexture( this.is.getIconIndex() );
		rh.setBounds( 6.0f, 6.0f, 5.0f, 10.0f, 10.0f, 11.0f );
		rh.renderInventoryBox( renderer );
		rh.setTexture( null );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public final void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		IIcon myIcon = this.is.getIconIndex();
		rh.setTexture( myIcon );
		rh.setBounds( 6, 6, 10, 10, 10, 16 );
		rh.renderBlock( x, y, z, renderer );
		rh.setTexture( null );
	}

	@Override
	public final void readFromNBT( NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.outerProxy.readFromNBT( extra );
	}

	@Override
	public final void writeToNBT( NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.outerProxy.writeToNBT( extra );
	}

	@Override
	public final void removeFromWorld()
	{
		super.removeFromWorld();
		this.outerProxy.invalidate();
	}

	@Override
	public final void addToWorld()
	{
		super.addToWorld();
		this.outerProxy.onReady();
	}

	@Override
	public final void setPartHostInfo( ForgeDirection side, IPartHost host, TileEntity tile )
	{
		super.setPartHostInfo( side, host, tile );
		this.outerProxy.setValidSides( EnumSet.of( side ) );
	}

	@Override
	public final IGridNode getExternalFacingNode()
	{
		return this.outerProxy.getNode();
	}

	@Override
	public final int cableConnectionRenderTo()
	{
		return 16;
	}

	@Override
	public final void onPlacement( EntityPlayer player, ItemStack held, ForgeDirection side )
	{
		super.onPlacement( player, held, side );
		this.outerProxy.setOwner( player );
	}

	@Override
	public final double extractAEPower( double amt, Actionable mode, Set<IEnergyGrid> seen )
	{
		double acquiredPower = 0;

		try
		{
			IEnergyGrid eg = this.proxy.getEnergy();
			acquiredPower += eg.extractAEPower( amt - acquiredPower, mode, seen );
		}
		catch( GridAccessException e )
		{
			// :P
		}

		try
		{
			IEnergyGrid eg = this.outerProxy.getEnergy();
			acquiredPower += eg.extractAEPower( amt - acquiredPower, mode, seen );
		}
		catch( GridAccessException e )
		{
			// :P
		}

		return acquiredPower;
	}

	@Override
	public final double injectAEPower( double amt, Actionable mode, Set<IEnergyGrid> seen )
	{

		try
		{
			IEnergyGrid eg = this.proxy.getEnergy();
			if( !seen.contains( eg ) )
			{
				return eg.injectAEPower( amt, mode, seen );
			}
		}
		catch( GridAccessException e )
		{
			// :P
		}

		try
		{
			IEnergyGrid eg = this.outerProxy.getEnergy();
			if( !seen.contains( eg ) )
			{
				return eg.injectAEPower( amt, mode, seen );
			}
		}
		catch( GridAccessException e )
		{
			// :P
		}

		return amt;
	}

	@Override
	public final double getEnergyDemand( double amt, Set<IEnergyGrid> seen )
	{
		double demand = 0;

		try
		{
			IEnergyGrid eg = this.proxy.getEnergy();
			demand += eg.getEnergyDemand( amt - demand, seen );
		}
		catch( GridAccessException e )
		{
			// :P
		}

		try
		{
			IEnergyGrid eg = this.outerProxy.getEnergy();
			demand += eg.getEnergyDemand( amt - demand, seen );
		}
		catch( GridAccessException e )
		{
			// :P
		}

		return demand;
	}
}
