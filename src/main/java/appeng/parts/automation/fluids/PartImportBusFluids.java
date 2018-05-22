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


import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartModel;
import appeng.util.item.AEFluidStack;


/**
 * @author BrockWS
 * @version rv6 - 30/04/2018
 * @since rv6 30/04/2018
 */
public class PartImportBusFluids extends PartSharedFluidBus
{
	public static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "part/import_bus_fluids_base" );
	@PartModels
	public static final IPartModel MODELS_OFF = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/import_bus_fluids_off" ) );
	@PartModels
	public static final IPartModel MODELS_ON = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/import_bus_fluids_on" ) );
	@PartModels
	public static final IPartModel MODELS_HAS_CHANNEL = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/import_bus_fluids_has_channel" ) );

	private final IActionSource source;

	public PartImportBusFluids( ItemStack is )
	{
		super( is );
		this.getConfigManager().registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		this.getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.getConfigManager().registerSetting( Settings.CRAFT_ONLY, YesNo.NO );
		this.getConfigManager().registerSetting( Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT );
		this.source = new MachineSource( this );
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return new TickingRequest( 5, 40, this.isSleeping(), false );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int TicksSinceLastCall )
	{
		return this.canDoBusWork() ? this.doBusWork() : TickRateModulation.IDLE;
	}

	@Override
	protected boolean canDoBusWork()
	{
		return this.getProxy().isActive();
	}

	@Override
	protected TickRateModulation doBusWork()
	{
		if( !this.canDoBusWork() )
		{
			return TickRateModulation.IDLE;
		}

		final TileEntity te = this.getConnectedTE();
		if( te != null && te.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.getSide().getFacing() ) )
		{
			try
			{
				final IFluidHandler fh = te.getCapability( CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.getSide().getFacing() );
				final IMEMonitor<IAEFluidStack> inv = this.getProxy().getStorage().getInventory( this.getChannel() );
				if( fh != null )
				{
					FluidStack fluidStack = fh.drain( this.calculateAmountToSend(), false );
					if( this.filterEnabled() && !this.isInFilter( fluidStack ) )
					{
						return TickRateModulation.SLOWER;
					}
					AEFluidStack aeFluidStack = AEFluidStack.fromFluidStack( fluidStack );
					if( aeFluidStack != null )
					{
						IAEFluidStack notInserted = inv.injectItems( aeFluidStack, Actionable.MODULATE, this.source );
						if( notInserted != null && notInserted.getStackSize() > 0 )
						{
							aeFluidStack.decStackSize( notInserted.getStackSize() );
						}
						fh.drain( aeFluidStack.getFluidStack(), true );
						return TickRateModulation.FASTER;
					}
				}
			}
			catch( GridAccessException e )
			{
				e.printStackTrace();
			}
		}

		return TickRateModulation.SLEEP;
	}

	private boolean isInFilter( FluidStack fluid )
	{
		for( int i = 0; i < this.getConfig().getSlots(); i++ )
		{
			IAEItemStack stack = this.getConfig().getAEStackInSlot( i );
			if( stack != null && stack.getDefinition() != null )
			{
				IFluidHandlerItem fh = stack.getDefinition().getCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null );
				if( fh == null )
				{
					throw new NullPointerException( "IFluidHandlerItem is null" );
				}
				FluidStack filtered = fh.drain( Integer.MAX_VALUE, false );
				if( filtered != null && filtered.isFluidEqual( fluid ) )
				{
					return true;
				}
			}
		}
		return false;
	}

	private boolean filterEnabled(){
		for( int i = 0; i < this.getConfig().getSlots(); i++ )
		{
			IAEItemStack stack = this.getConfig().getAEStackInSlot( i );
			if( stack != null)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public RedstoneMode getRSMode()
	{
		return (RedstoneMode) this.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED );
	}

	@Nonnull
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
}
