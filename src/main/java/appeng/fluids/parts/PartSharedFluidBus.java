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

package appeng.fluids.parts;


import appeng.fluids.helper.IConfigurableFluidInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.util.AECableType;
import appeng.core.sync.GuiBridge;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
import appeng.me.GridAccessException;
import appeng.parts.automation.PartUpgradeable;
import appeng.util.Platform;
import net.minecraftforge.fluids.capability.IFluidHandler;


/**
 * @author BrockWS
 * @version rv6 - 30/04/2018
 * @since rv6 30/04/2018
 */
public abstract class PartSharedFluidBus extends PartUpgradeable implements IGridTickable, IConfigurableFluidInventory
{

	private final AEFluidInventory config = new AEFluidInventory( null, 9 );
	private boolean lastRedstone;

	public PartSharedFluidBus( ItemStack is )
	{
		super( is );
	}

	@Override
	public void upgradesChanged()
	{
		this.updateState();
	}

	@Override
	public void onNeighborChanged( IBlockAccess w, BlockPos pos, BlockPos neighbor )
	{
		this.updateState();
		if( this.lastRedstone != this.getHost().hasRedstone( this.getSide() ) )
		{
			this.lastRedstone = !this.lastRedstone;
			if( this.lastRedstone && this.getRSMode() == RedstoneMode.SIGNAL_PULSE )
			{
				this.doBusWork();
			}
		}
	}

	private void updateState()
	{
		try
		{
			if( !this.isSleeping() )
			{
				this.getProxy().getTick().wakeDevice( this.getProxy().getNode() );
			}
			else
			{
				this.getProxy().getTick().sleepDevice( this.getProxy().getNode() );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_BUS_FLUID );
		}

		return true;
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 6, 6, 11, 10, 10, 13 );
		bch.addBox( 5, 5, 13, 11, 11, 14 );
		bch.addBox( 4, 4, 14, 12, 12, 16 );
	}

	protected TileEntity getConnectedTE()
	{
		TileEntity self = this.getHost().getTile();
		return this.getTileEntity( self, self.getPos().offset( this.getSide().getFacing() ) );
	}

	private TileEntity getTileEntity( final TileEntity self, final BlockPos pos )
	{
		final World w = self.getWorld();

		if( w.getChunkProvider().getLoadedChunk( pos.getX() >> 4, pos.getZ() >> 4 ) != null )
		{
			return w.getTileEntity( pos );
		}

		return null;
	}

	protected int calculateAmountToSend()
	{
		double amount = this.getChannel().transferFactor();
		switch( this.getInstalledUpgrades( Upgrades.SPEED ) )
		{
			case 4:
				amount = amount * 1.5;
			case 3:
				amount = amount * 2;
			case 2:
				amount = amount * 4;
			case 1:
				amount = amount * 8;
			case 0:
			default:
				return MathHelper.floor( amount );
		}
	}

	@Override
	public void readFromNBT( NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.config.readFromNBT( extra, "config" );
	}

	@Override
	public void writeToNBT( NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.config.writeToNBT( extra, "config" );
	}

	public IAEFluidTank getConfig()
	{
		return this.config;
	}

	@Override
	public IFluidHandler getFluidInventoryByName( final String name) {
		if (name.equals("config")) {
			return this.config;
		}
		return null;
	}

	protected IFluidStorageChannel getChannel()
	{
		return AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class );
	}

	@Override
	public float getCableConnectionLength( AECableType cable )
	{
		return 5;
	}

	protected abstract TickRateModulation doBusWork();

	protected abstract boolean canDoBusWork();
}
