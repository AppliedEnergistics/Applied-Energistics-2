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

package appeng.parts.automation.fluids;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.util.AECableType;
import appeng.core.sync.GuiBridge;
import appeng.parts.automation.PartUpgradeable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;


/**
 * @author BrockWS
 * @version rv6 - 30/04/2018
 * @since rv6 30/04/2018
 */
public abstract class PartSharedFluidBus extends PartUpgradeable implements IGridTickable
{

	private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 9 );

	public PartSharedFluidBus( ItemStack is )
	{
		super( is );
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		if( !player.isSneaking() )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_BUS_FLUID );
			return true;
		}

		return false;
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

	@Override
	public IItemHandler getInventoryByName( final String name )
	{
		if( name.equals( "config" ) )
		{
			return this.getConfig();
		}

		return super.getInventoryByName( name );
	}

	protected int calculateAmountToSend()
	{
		switch( this.getInstalledUpgrades( Upgrades.SPEED ) )
		{
			default:
			case 0:
				return 1000;
			case 1:
				return 2000;
			case 2:
				return 4000;
			case 3:
				return 8000;
			case 4:
				return 16000;
		}
	}

	@Override
	public void readFromNBT( NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.getConfig().readFromNBT( extra, "config" );
	}

	@Override
	public void writeToNBT( NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.getConfig().writeToNBT( extra, "config" );
	}

	public AppEngInternalAEInventory getConfig()
	{
		return this.config;
	}

	protected IFluidStorageChannel getChannel(){
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
