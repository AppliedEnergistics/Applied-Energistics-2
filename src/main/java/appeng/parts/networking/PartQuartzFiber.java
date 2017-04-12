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

package appeng.parts.networking;


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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import java.util.EnumSet;
import java.util.Set;


public class PartQuartzFiber extends AEBasePart implements IEnergyGridProvider
{

	private final AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", this.getProxy().getMachineRepresentation(), true );

	public PartQuartzFiber( final ItemStack is )
	{
		super( is );
		this.getProxy().setIdlePowerUsage( 0 );
		this.getProxy().setFlags( GridFlags.CANNOT_CARRY );
		this.outerProxy.setIdlePowerUsage( 0 );
		this.outerProxy.setFlags( GridFlags.CANNOT_CARRY );
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.GLASS;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 6, 6, 10, 10, 10, 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		GL11.glTranslated( -0.2, -0.3, 0.0 );

		rh.setTexture( this.getItemStack().getIconIndex() );
		rh.setBounds( 6.0f, 6.0f, 5.0f, 10.0f, 10.0f, 11.0f );
		rh.renderInventoryBox( renderer );
		rh.setTexture( null );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
	{
		final IIcon myIcon = this.getItemStack().getIconIndex();
		rh.setTexture( myIcon );
		rh.setBounds( 6, 6, 10, 10, 10, 16 );
		rh.renderBlock( x, y, z, renderer );
		rh.setTexture( null );
	}

	@Override
	public void readFromNBT( final NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.outerProxy.readFromNBT( extra );
	}

	@Override
	public void writeToNBT( final NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.outerProxy.writeToNBT( extra );
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		this.outerProxy.invalidate();
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		this.outerProxy.onReady();
	}

	@Override
	public void setPartHostInfo( final ForgeDirection side, final IPartHost host, final TileEntity tile )
	{
		super.setPartHostInfo( side, host, tile );
		this.outerProxy.setValidSides( EnumSet.of( side ) );
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return this.outerProxy.getNode();
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 16;
	}

	@Override
	public void onPlacement( final EntityPlayer player, final ItemStack held, final ForgeDirection side )
	{
		super.onPlacement( player, held, side );
		this.outerProxy.setOwner( player );
	}

	@Override
	public double extractAEPower( final double amt, final Actionable mode, final Set<IEnergyGrid> seen )
	{
		double acquiredPower = 0;

		try
		{
			final IEnergyGrid eg = this.getProxy().getEnergy();
			acquiredPower += eg.extractAEPower( amt - acquiredPower, mode, seen );
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		try
		{
			final IEnergyGrid eg = this.outerProxy.getEnergy();
			acquiredPower += eg.extractAEPower( amt - acquiredPower, mode, seen );
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return acquiredPower;
	}

	@Override
	public double injectAEPower( final double amt, final Actionable mode, final Set<IEnergyGrid> seen )
	{

		try
		{
			final IEnergyGrid eg = this.getProxy().getEnergy();
			if( !seen.contains( eg ) )
			{
				return eg.injectAEPower( amt, mode, seen );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		try
		{
			final IEnergyGrid eg = this.outerProxy.getEnergy();
			if( !seen.contains( eg ) )
			{
				return eg.injectAEPower( amt, mode, seen );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return amt;
	}

	@Override
	public double getEnergyDemand( final double amt, final Set<IEnergyGrid> seen )
	{
		double demand = 0;

		try
		{
			final IEnergyGrid eg = this.getProxy().getEnergy();
			demand += eg.getEnergyDemand( amt - demand, seen );
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		try
		{
			final IEnergyGrid eg = this.outerProxy.getEnergy();
			demand += eg.getEnergyDemand( amt - demand, seen );
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return demand;
	}
}
