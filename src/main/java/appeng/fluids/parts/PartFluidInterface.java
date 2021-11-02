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


import java.util.EnumSet;

import appeng.fluids.helper.IConfigurableFluidInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.parts.PartBasicState;
import appeng.parts.PartModel;
import appeng.util.Platform;


public class PartFluidInterface extends PartBasicState implements IGridTickable, IStorageMonitorable, IFluidInterfaceHost, IPriorityHost, IConfigurableFluidInventory
{
	public static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "part/fluid_interface_base" );

	@PartModels
	public static final PartModel MODELS_OFF = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/fluid_interface_off" ) );

	@PartModels
	public static final PartModel MODELS_ON = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/fluid_interface_on" ) );

	@PartModels
	public static final PartModel MODELS_HAS_CHANNEL = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/fluid_interface_has_channel" ) );

	private final DualityFluidInterface duality = new DualityFluidInterface( this.getProxy(), this );

	@Reflected
	public PartFluidInterface( final ItemStack is )
	{
		super( is );
	}

	@Override
	public DualityFluidInterface getDualityFluidInterface()
	{
		return this.duality;
	}

	@Override
	@MENetworkEventSubscribe
	public void chanRender( final MENetworkChannelsChanged c )
	{
		this.duality.notifyNeighbors();
	}

	@Override
	@MENetworkEventSubscribe
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.duality.notifyNeighbors();
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 2, 2, 14, 14, 14, 16 );
		bch.addBox( 5, 5, 12, 11, 11, 14 );
	}

	@Override
	public void gridChanged()
	{
		this.duality.gridChanged();
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.duality.readFromNBT( data );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.duality.writeToNBT( data );
	}

	@Override
	public float getCableConnectionLength( AECableType cable )
	{
		return 4;
	}

	@Override
	public boolean onPartActivate( final EntityPlayer p, final EnumHand hand, final Vec3d pos )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( p, this.getTileEntity(), this.getSide(), GuiBridge.GUI_FLUID_INTERFACE );
		}

		return true;
	}

	@Override
	public <T extends IAEStack<T>> IMEMonitor<T> getInventory( IStorageChannel<T> channel )
	{
		return this.duality.getInventory( channel );
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return this.duality.getTickingRequest( node );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		return this.duality.tickingRequest( node, ticksSinceLastCall );
	}

	@Override
	public EnumSet<EnumFacing> getTargets()
	{
		return EnumSet.of( this.getSide().getFacing() );
	}

	@Override
	public TileEntity getTileEntity()
	{
		return super.getHost().getTile();
	}

	@Override
	public IPartModel getStaticModels()
	{
		if( this.isActive() && this.isPowered() )
		{
			return MODELS_HAS_CHANNEL;
		}
		else if( this.isPowered() )
		{
			return MODELS_ON;
		}
		else
		{
			return MODELS_OFF;
		}
	}

	@Override
	public int getPriority()
	{
		return this.duality.getPriority();
	}

	@Override
	public void setPriority( final int newValue )
	{
		this.duality.setPriority( newValue );
	}

	@Override
	public boolean hasCapability( Capability<?> capabilityClass )
	{
		return this.duality.hasCapability( capabilityClass, this.getSide().getFacing() );
	}

	@Override
	public <T> T getCapability( Capability<T> capabilityClass )
	{
		return this.duality.getCapability( capabilityClass, this.getSide().getFacing() );
	}

	@Override
	public int getInstalledUpgrades( Upgrades u )
	{
		return this.duality.getInstalledUpgrades( u );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.duality.getConfigManager();
	}

	@Override
	public IItemHandler getInventoryByName( String name )
	{
		return this.duality.getInventoryByName( name );
	}

	@Override
	public IFluidHandler getFluidInventoryByName( final String name) {
		return this.duality.getFluidInventoryByName(name);
	}

	@Override
	public ItemStack getItemStackRepresentation()
	{
		return AEApi.instance().definitions().parts().fluidIface().maybeStack( 1 ).orElse( ItemStack.EMPTY );
	}

	@Override
	public GuiBridge getGuiBridge()
	{
		return GuiBridge.GUI_FLUID_INTERFACE;
	}
}
