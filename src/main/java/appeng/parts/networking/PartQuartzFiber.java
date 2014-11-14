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

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

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

public class PartQuartzFiber extends AEBasePart implements IEnergyGridProvider
{

	final AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", proxy.getMachineRepresentation(), true );

	public PartQuartzFiber(ItemStack is) {
		super( PartQuartzFiber.class, is );
		proxy.setIdlePowerUsage( 0 );
		proxy.setFlags( GridFlags.CANNOT_CARRY );
		outerProxy.setIdlePowerUsage( 0 );
		outerProxy.setFlags( GridFlags.CANNOT_CARRY );
	}

	@Override
	public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side)
	{
		super.onPlacement( player, held, side );
		outerProxy.setOwner( player );
	}

	@Override
	public void setPartHostInfo(ForgeDirection side, IPartHost host, TileEntity tile)
	{
		super.setPartHostInfo( side, host, tile );
		outerProxy.setValidSides( EnumSet.of( side ) );
	}

	@Override
	public void readFromNBT(NBTTagCompound extra)
	{
		super.readFromNBT( extra );
		outerProxy.readFromNBT( extra );
	}

	@Override
	public void writeToNBT(NBTTagCompound extra)
	{
		super.writeToNBT( extra );
		outerProxy.writeToNBT( extra );
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		outerProxy.onReady();
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		outerProxy.invalidate();
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return outerProxy.getNode();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.GLASS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		IIcon myIcon = is.getIconIndex();
		rh.setTexture( myIcon );
		rh.setBounds( 6, 6, 10, 10, 10, 16 );
		rh.renderBlock( x, y, z, renderer );
		rh.setTexture( null );
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		bch.addBox( 6, 6, 10, 10, 10, 16 );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		GL11.glTranslated( -0.2, -0.3, 0.0 );

		rh.setTexture( is.getIconIndex() );
		rh.setBounds( 6.0f, 6.0f, 5.0f, 10.0f, 10.0f, 11.0f );
		rh.renderInventoryBox( renderer );
		rh.setTexture( null );
	}

	@Override
	public double extractAEPower(double amt, Actionable mode, Set<IEnergyGrid> seen)
	{
		double acquiredPower = 0;

		try
		{
			IEnergyGrid eg = proxy.getEnergy();
			acquiredPower += eg.extractAEPower( amt - acquiredPower, mode, seen );
		}
		catch (GridAccessException e)
		{
			// :P
		}

		try
		{
			IEnergyGrid eg = outerProxy.getEnergy();
			acquiredPower += eg.extractAEPower( amt - acquiredPower, mode, seen );
		}
		catch (GridAccessException e)
		{
			// :P
		}

		return acquiredPower;
	}

	@Override
	public double injectAEPower(double amt, Actionable mode, Set<IEnergyGrid> seen)
	{

		try
		{
			IEnergyGrid eg = proxy.getEnergy();
			if ( !seen.contains( eg ) )
				return eg.injectAEPower( amt, mode, seen );
		}
		catch (GridAccessException e)
		{
			// :P
		}

		try
		{
			IEnergyGrid eg = outerProxy.getEnergy();
			if ( !seen.contains( eg ) )
				return eg.injectAEPower( amt, mode, seen );
		}
		catch (GridAccessException e)
		{
			// :P
		}

		return amt;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 16;
	}

	@Override
	public double getEnergyDemand(double amt, Set<IEnergyGrid> seen)
	{
		double demand = 0;

		try
		{
			IEnergyGrid eg = proxy.getEnergy();
			demand += eg.getEnergyDemand( amt - demand, seen );
		}
		catch (GridAccessException e)
		{
			// :P
		}

		try
		{
			IEnergyGrid eg = outerProxy.getEnergy();
			demand += eg.getEnergyDemand( amt - demand, seen );
		}
		catch (GridAccessException e)
		{
			// :P
		}

		return demand;
	}

}
