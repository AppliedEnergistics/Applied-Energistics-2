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

package appeng.me.helpers;


import java.util.Collections;
import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkPowerIdleChange;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IOrientable;
import appeng.core.WorldSettings;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.me.cache.P2PCache;
import appeng.parts.networking.PartCable;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


public class AENetworkProxy implements IGridBlock
{

	final private IGridProxyable gp;
	final private boolean worldNode;
	final private String nbtName; // name
	public AEColor myColor = AEColor.Transparent;
	NBTTagCompound data = null; // input
	private ItemStack myRepInstance;
	private boolean isReady = false;
	private IGridNode node = null;
	private EnumSet<ForgeDirection> validSides;
	private EnumSet<GridFlags> flags = EnumSet.noneOf( GridFlags.class );
	private double idleDraw = 1.0;
	private EntityPlayer owner;

	public AENetworkProxy( IGridProxyable te, String nbtName, ItemStack visual, boolean inWorld )
	{
		this.gp = te;
		this.nbtName = nbtName;
		this.worldNode = inWorld;
		this.myRepInstance = visual;
		this.validSides = EnumSet.allOf( ForgeDirection.class );
	}

	public void setVisualRepresentation( ItemStack is )
	{
		this.myRepInstance = is;
	}

	public void writeToNBT( NBTTagCompound tag )
	{
		if( this.node != null )
			this.node.saveToNBT( this.nbtName, tag );
	}

	public void setValidSides( EnumSet<ForgeDirection> validSides )
	{
		this.validSides = validSides;
		if( this.node != null )
			this.node.updateState();
	}

	public void validate()
	{
		if( this.gp instanceof AEBaseTile )
			TickHandler.INSTANCE.addInit( (AEBaseTile) this.gp );
	}

	public void onChunkUnload()
	{
		this.isReady = false;
		this.invalidate();
	}

	public void invalidate()
	{
		this.isReady = false;
		if( this.node != null )
		{
			this.node.destroy();
			this.node = null;
		}
	}

	public void onReady()
	{
		this.isReady = true;

		// send orientation based directionality to the node.
		if( this.gp instanceof IOrientable )
		{
			IOrientable ori = (IOrientable) this.gp;
			if( ori.canBeRotated() )
				ori.setOrientation( ori.getForward(), ori.getUp() );
		}

		this.getNode();
	}

	public IGridNode getNode()
	{
		if( this.node == null && Platform.isServer() && this.isReady )
		{
			this.node = AEApi.instance().createGridNode( this );
			this.readFromNBT( this.data );
			this.node.updateState();
		}

		return this.node;
	}

	public void readFromNBT( NBTTagCompound tag )
	{
		this.data = tag;
		if( this.node != null && this.data != null )
		{
			this.node.loadFromNBT( this.nbtName, this.data );
			this.data = null;
		}
		else if( this.node != null && this.owner != null )
		{
			this.node.setPlayerID( WorldSettings.getInstance().getPlayerID( this.owner.getGameProfile() ) );
			this.owner = null;
		}
	}

	public IPathingGrid getPath() throws GridAccessException
	{
		IGrid grid = this.getGrid();
		if( grid == null )
			throw new GridAccessException();
		IPathingGrid pg = grid.getCache( IPathingGrid.class );
		if( pg == null )
			throw new GridAccessException();
		return pg;
	}

	/**
	 * short cut!
	 *
	 * @return grid of node
	 *
	 * @throws GridAccessException of node or grid is null
	 */
	public IGrid getGrid() throws GridAccessException
	{
		if( this.node == null )
			throw new GridAccessException();
		IGrid grid = this.node.getGrid();
		if( grid == null )
			throw new GridAccessException();
		return grid;
	}

	public ITickManager getTick() throws GridAccessException
	{
		IGrid grid = this.getGrid();
		if( grid == null )
			throw new GridAccessException();
		ITickManager pg = grid.getCache( ITickManager.class );
		if( pg == null )
			throw new GridAccessException();
		return pg;
	}

	public IStorageGrid getStorage() throws GridAccessException
	{
		IGrid grid = this.getGrid();
		if( grid == null )
			throw new GridAccessException();

		IStorageGrid pg = grid.getCache( IStorageGrid.class );

		if( pg == null )
			throw new GridAccessException();

		return pg;
	}

	public P2PCache getP2P() throws GridAccessException
	{
		IGrid grid = this.getGrid();
		if( grid == null )
			throw new GridAccessException();

		P2PCache pg = grid.getCache( P2PCache.class );

		if( pg == null )
			throw new GridAccessException();

		return pg;
	}

	public ISecurityGrid getSecurity() throws GridAccessException
	{
		IGrid grid = this.getGrid();
		if( grid == null )
			throw new GridAccessException();

		ISecurityGrid sg = grid.getCache( ISecurityGrid.class );

		if( sg == null )
			throw new GridAccessException();

		return sg;
	}

	public ICraftingGrid getCrafting() throws GridAccessException
	{
		IGrid grid = this.getGrid();
		if( grid == null )
			throw new GridAccessException();

		ICraftingGrid sg = grid.getCache( ICraftingGrid.class );

		if( sg == null )
			throw new GridAccessException();

		return sg;
	}

	@Override
	public double getIdlePowerUsage()
	{
		return this.idleDraw;
	}

	@Override
	public EnumSet<GridFlags> getFlags()
	{
		return this.flags;
	}

	@Override
	public boolean isWorldAccessible()
	{
		return this.worldNode;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return this.gp.getLocation();
	}

	@Override
	public AEColor getGridColor()
	{
		return this.myColor;
	}

	@Override
	public void onGridNotification( GridNotification notification )
	{
		if( this.gp instanceof PartCable )
			( (PartCable) this.gp ).markForUpdate();
	}

	@Override
	public void setNetworkStatus( IGrid grid, int channelsInUse )
	{

	}

	@Override
	public EnumSet<ForgeDirection> getConnectableSides()
	{
		return this.validSides;
	}

	@Override
	public IGridHost getMachine()
	{
		return this.gp;
	}

	@Override
	public void gridChanged()
	{
		this.gp.gridChanged();
	}

	@Override
	public ItemStack getMachineRepresentation()
	{
		return this.myRepInstance;
	}

	public void setFlags( GridFlags... requireChannel )
	{
		EnumSet<GridFlags> flags = EnumSet.noneOf( GridFlags.class );

		Collections.addAll( flags, requireChannel );

		this.flags = flags;
	}

	public void setIdlePowerUsage( double idle )
	{
		this.idleDraw = idle;

		if( this.node != null )
		{
			try
			{
				IGrid g = this.getGrid();
				g.postEvent( new MENetworkPowerIdleChange( this.node ) );
			}
			catch( GridAccessException e )
			{
				// not ready for this yet..
			}
		}
	}

	public boolean isReady()
	{
		return this.isReady;
	}

	public boolean isActive()
	{
		if( this.node == null )
			return false;

		return this.node.isActive();
	}

	public boolean isPowered()
	{
		try
		{
			return this.getEnergy().isNetworkPowered();
		}
		catch( GridAccessException e )
		{
			return false;
		}
	}

	public IEnergyGrid getEnergy() throws GridAccessException
	{
		IGrid grid = this.getGrid();
		if( grid == null )
			throw new GridAccessException();
		IEnergyGrid eg = grid.getCache( IEnergyGrid.class );
		if( eg == null )
			throw new GridAccessException();
		return eg;
	}

	public void setOwner( EntityPlayer player )
	{
		this.owner = player;
	}
}
