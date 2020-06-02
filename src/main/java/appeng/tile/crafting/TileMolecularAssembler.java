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

package appeng.tile.crafting;


import java.io.IOException;
import java.util.List;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.definitions.ITileDefinition;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerNull;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketAssemblerAnimation;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.GridAccessException;
import appeng.parts.automation.DefinitionUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.IAEItemFilter;
import appeng.util.item.AEItemStack;


public class TileMolecularAssembler extends AENetworkInvTile implements IUpgradeableHost, IConfigManagerHost, IGridTickable, ICraftingMachine, IPowerChannelState
{
	private final CraftingInventory craftingInv;
	private final AppEngInternalInventory gridInv = new AppEngInternalInventory( this, 9 + 1, 1 );
	private final AppEngInternalInventory patternInv = new AppEngInternalInventory( this, 1, 1 );
	private final IItemHandler gridInvExt = new WrapperFilteredItemHandler( this.gridInv, new CraftingGridFilter() );
	private final IItemHandler internalInv = new WrapperChainedItemHandler( this.gridInv, this.patternInv );
	private final IConfigManager settings;
	private final UpgradeInventory upgrades;
	private boolean isPowered = false;
	private AEPartLocation pushDirection = AEPartLocation.INTERNAL;
	private ItemStack myPattern = ItemStack.EMPTY;
	private ICraftingPatternDetails myPlan = null;
	private double progress = 0;
	private boolean isAwake = false;
	private boolean forcePlan = false;
	private boolean reboot = true;

	public TileMolecularAssembler()
	{
		final ITileDefinition assembler = Api.INSTANCE.definitions().blocks().molecularAssembler();

		this.settings = new ConfigManager( this );
		this.settings.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		this.getProxy().setIdlePowerUsage( 0.0 );
		this.upgrades = new DefinitionUpgradeInventory( assembler, this, this.getUpgradeSlots() );
		this.craftingInv = new CraftingInventory( new ContainerNull(), 3, 3 );

	}

	private int getUpgradeSlots()
	{
		return 5;
	}

	@Override
	public boolean pushPattern( final ICraftingPatternDetails patternDetails, final CraftingInventory table, final Direction where )
	{
		if( this.myPattern.isEmpty() )
		{
			boolean isEmpty = ItemHandlerUtil.isEmpty( this.gridInv ) && ItemHandlerUtil.isEmpty( this.patternInv );

			if( isEmpty && patternDetails.isCraftable() )
			{
				this.forcePlan = true;
				this.myPlan = patternDetails;
				this.pushDirection = AEPartLocation.fromFacing( where );

				for( int x = 0; x < table.getSizeInventory(); x++ )
				{
					this.gridInv.setStackInSlot( x, table.getStackInSlot( x ) );
				}

				this.updateSleepiness();
				this.saveChanges();
				return true;
			}
		}
		return false;
	}

	private void updateSleepiness()
	{
		final boolean wasEnabled = this.isAwake;
		this.isAwake = this.myPlan != null && this.hasMats() || this.canPush();
		if( wasEnabled != this.isAwake )
		{
			try
			{
				if( this.isAwake )
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
	}

	private boolean canPush()
	{
		return !this.gridInv.getStackInSlot( 9 ).isEmpty();
	}

	private boolean hasMats()
	{
		if( this.myPlan == null )
		{
			return false;
		}

		for( int x = 0; x < this.craftingInv.getSizeInventory(); x++ )
		{
			this.craftingInv.setInventorySlotContents( x, this.gridInv.getStackInSlot( x ) );
		}

		return !this.myPlan.getOutput( this.craftingInv, this.getWorld() ).isEmpty();
	}

	@Override
	public boolean acceptsPlans()
	{
		return ItemHandlerUtil.isEmpty( this.patternInv );
	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		return this.upgrades.getInstalledUpgrades( u );
	}

	@Override
	protected boolean readFromStream( final PacketBuffer data ) throws IOException
	{
		final boolean c = super.readFromStream( data );
		final boolean oldPower = this.isPowered;
		this.isPowered = data.readBoolean();
		return this.isPowered != oldPower || c;
	}

	@Override
	protected void writeToStream( final PacketBuffer data ) throws IOException
	{
		super.writeToStream( data );
		data.writeBoolean( this.isPowered );
	}

	@Override
	public CompoundNBT write(final CompoundNBT data )
	{
		super.write( data );
		if( this.forcePlan && this.myPlan != null )
		{
			final ItemStack pattern = this.myPlan.getPattern();
			if( !pattern.isEmpty() )
			{
				final CompoundNBT compound = new CompoundNBT();
				pattern.write(compound);
				data.setTag( "myPlan", compound );
				data.setInteger( "pushDirection", this.pushDirection.ordinal() );
			}
		}

		this.upgrades.writeToNBT( data, "upgrades" );
		this.settings.writeToNBT( data );
		return data;
	}

	@Override
	public void read(final CompoundNBT data )
	{
		super.read( data );
		if( data.contains("myPlan") )
		{
			final ItemStack myPat = new ItemStack( data.getCompoundTag( "myPlan" ) );

			if( !myPat.isEmpty() && myPat.getItem() instanceof ItemEncodedPattern )
			{
				final World w = this.getWorld();
				final ItemEncodedPattern iep = (ItemEncodedPattern) myPat.getItem();
				final ICraftingPatternDetails ph = iep.getPatternForItem( myPat, w );
				if( ph != null && ph.isCraftable() )
				{
					this.forcePlan = true;
					this.myPlan = ph;
					this.pushDirection = AEPartLocation.fromOrdinal( data.getInteger( "pushDirection" ) );
				}
			}
		}

		this.upgrades.readFromNBT( data, "upgrades" );
		this.settings.readFromNBT( data );
		this.recalculatePlan();
	}

	private void recalculatePlan()
	{
		this.reboot = true;

		if( this.forcePlan )
		{
			return;
		}

		final ItemStack is = this.patternInv.getStackInSlot( 0 );

		if( !is.isEmpty() && is.getItem() instanceof ItemEncodedPattern )
		{
			if( !ItemStack.areItemsEqual( is, this.myPattern ) )
			{
				final World w = this.getWorld();
				final ItemEncodedPattern iep = (ItemEncodedPattern) is.getItem();
				final ICraftingPatternDetails ph = iep.getPatternForItem( is, w );

				if( ph != null && ph.isCraftable() )
				{
					this.progress = 0;
					this.myPattern = is;
					this.myPlan = ph;
				}
			}
		}
		else
		{
			this.progress = 0;
			this.forcePlan = false;
			this.myPlan = null;
			this.myPattern = ItemStack.EMPTY;
			this.pushDirection = AEPartLocation.INTERNAL;
		}

		this.updateSleepiness();
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.COVERED;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.settings;
	}

	@Override
	public IItemHandler getInventoryByName( final String name )
	{
		if( name.equals( "upgrades" ) )
		{
			return this.upgrades;
		}

		if( name.equals( "mac" ) )
		{
			return this.internalInv;
		}

		return null;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{

	}

	@Override
	public IItemHandler getInternalInventory()
	{
		return this.internalInv;
	}

	@Override
	protected IItemHandler getItemHandlerForSide( Direction side )
	{
		return this.gridInvExt;
	}

	@Override
	public void onChangeInventory( final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( inv == this.gridInv || inv == this.patternInv )
		{
			this.recalculatePlan();
		}
	}

	public int getCraftingProgress()
	{
		return (int) this.progress;
	}

	@Override
	public void getDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{
		super.getDrops( w, pos, drops );

		for( int h = 0; h < this.upgrades.getSlots(); h++ )
		{
			final ItemStack is = this.upgrades.getStackInSlot( h );
			if( !is.isEmpty() )
			{
				drops.add( is );
			}
		}
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		this.recalculatePlan();
		this.updateSleepiness();
		return new TickingRequest( 1, 1, !this.isAwake, false );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, int ticksSinceLastCall )
	{
		if( !this.gridInv.getStackInSlot( 9 ).isEmpty() )
		{
			this.pushOut( this.gridInv.getStackInSlot( 9 ) );

			// did it eject?
			if( this.gridInv.getStackInSlot( 9 ).isEmpty() )
			{
				this.saveChanges();
			}

			this.ejectHeldItems();
			this.updateSleepiness();
			this.progress = 0;
			return this.isAwake ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
		}

		if( this.myPlan == null )
		{
			this.updateSleepiness();
			return TickRateModulation.SLEEP;
		}

		if( this.reboot )
		{
			ticksSinceLastCall = 1;
		}

		if( !this.isAwake )
		{
			return TickRateModulation.SLEEP;
		}

		this.reboot = false;
		int speed = 10;
		switch( this.upgrades.getInstalledUpgrades( Upgrades.SPEED ) )
		{
			case 0:
				this.progress += this.userPower( ticksSinceLastCall, speed = 10, 1.0 );
				break;
			case 1:
				this.progress += this.userPower( ticksSinceLastCall, speed = 13, 1.3 );
				break;
			case 2:
				this.progress += this.userPower( ticksSinceLastCall, speed = 17, 1.7 );
				break;
			case 3:
				this.progress += this.userPower( ticksSinceLastCall, speed = 20, 2.0 );
				break;
			case 4:
				this.progress += this.userPower( ticksSinceLastCall, speed = 25, 2.5 );
				break;
			case 5:
				this.progress += this.userPower( ticksSinceLastCall, speed = 50, 5.0 );
				break;
		}

		if( this.progress >= 100 )
		{
			for( int x = 0; x < this.craftingInv.getSizeInventory(); x++ )
			{
				this.craftingInv.setInventorySlotContents( x, this.gridInv.getStackInSlot( x ) );
			}

			this.progress = 0;
			final ItemStack output = this.myPlan.getOutput( this.craftingInv, this.getWorld() );
			if( !output.isEmpty() )
			{
				FMLCommonHandler.instance().firePlayerCraftingEvent( Platform.getPlayer( (WorldServer) this.getWorld() ), output, this.craftingInv );

				this.pushOut( output.copy() );

				for( int x = 0; x < this.craftingInv.getSizeInventory(); x++ )
				{
					this.gridInv.setStackInSlot( x, Platform.getContainerItem( this.craftingInv.getStackInSlot( x ) ) );
				}

				if( ItemHandlerUtil.isEmpty( this.patternInv ) )
				{
					this.forcePlan = false;
					this.myPlan = null;
					this.pushDirection = AEPartLocation.INTERNAL;
				}

				this.ejectHeldItems();

				try
				{
					final TargetPoint where = new TargetPoint( this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 32 );
					final IAEItemStack item = AEItemStack.fromItemStack( output );
					NetworkHandler.instance().sendToAllAround( new PacketAssemblerAnimation( this.pos, (byte) speed, item ), where );
				}
				catch( final IOException e )
				{
					// ;P
				}

				this.saveChanges();
				this.updateSleepiness();
				return this.isAwake ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
			}
		}

		return TickRateModulation.FASTER;
	}

	private void ejectHeldItems()
	{
		if( this.gridInv.getStackInSlot( 9 ).isEmpty() )
		{
			for( int x = 0; x < 9; x++ )
			{
				final ItemStack is = this.gridInv.getStackInSlot( x );
				if( !is.isEmpty() )
				{
					if( this.myPlan == null || !this.myPlan.isValidItemForSlot( x, is, this.world ) )
					{
						this.gridInv.setStackInSlot( 9, is );
						this.gridInv.setStackInSlot( x, ItemStack.EMPTY );
						this.saveChanges();
						return;
					}
				}
			}
		}
	}

	private int userPower( final int ticksPassed, final int bonusValue, final double acceleratorTax )
	{
		try
		{
			return (int) ( this.getProxy()
					.getEnergy()
					.extractAEPower( ticksPassed * bonusValue * acceleratorTax, Actionable.MODULATE,
							PowerMultiplier.CONFIG ) / acceleratorTax );
		}
		catch( final GridAccessException e )
		{
			return 0;
		}
	}

	private void pushOut( ItemStack output )
	{
		if( this.pushDirection == AEPartLocation.INTERNAL )
		{
			for( final Direction d : Direction.values() )
			{
				output = this.pushTo( output, d );
			}
		}
		else
		{
			output = this.pushTo( output, this.pushDirection.getFacing() );
		}

		if( output.isEmpty() && this.forcePlan )
		{
			this.forcePlan = false;
			this.recalculatePlan();
		}

		this.gridInv.setStackInSlot( 9, output );
	}

	private ItemStack pushTo( ItemStack output, final Direction d )
	{
		if( output.isEmpty() )
		{
			return output;
		}

		final TileEntity te = this.getWorld().getTileEntity( this.pos.offset( d ) );

		if( te == null )
		{
			return output;
		}

		final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( te, d.getOpposite() );

		if( adaptor == null )
		{
			return output;
		}

		final int size = output.getCount();
		output = adaptor.addItems( output );
		final int newSize = output.isEmpty() ? 0 : output.getCount();

		if( size != newSize )
		{
			this.saveChanges();
		}

		return output;
	}

	@MENetworkEventSubscribe
	public void onPowerEvent( final MENetworkPowerStatusChange p )
	{
		this.updatePowerState();
	}

	private void updatePowerState()
	{
		boolean newState = false;

		try
		{
			newState = this.getProxy().isActive() && this.getProxy().getEnergy().extractAEPower( 1, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > 0.0001;
		}
		catch( final GridAccessException ignored )
		{

		}

		if( newState != this.isPowered )
		{
			this.isPowered = newState;
			this.markForUpdate();
		}
	}

	@Override
	public boolean isPowered()
	{
		return this.isPowered;
	}

	@Override
	public boolean isActive()
	{
		return this.isPowered;
	}

	private class CraftingGridFilter implements IAEItemFilter
	{
		private boolean hasPattern()
		{
			return TileMolecularAssembler.this.myPlan != null && !ItemHandlerUtil.isEmpty( TileMolecularAssembler.this.patternInv );
		}

		@Override
		public boolean allowExtract( IItemHandler inv, int slot, int amount )
		{
			return slot == 9;
		}

		@Override
		public boolean allowInsert( IItemHandler inv, int slot, ItemStack stack )
		{
			if( slot >= 9 )
			{
				return false;
			}

			if( this.hasPattern() )
			{
				return TileMolecularAssembler.this.myPlan.isValidItemForSlot( slot, stack, TileMolecularAssembler.this.getWorld() );
			}
			return false;
		}
	}
}
