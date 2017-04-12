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


import appeng.api.AEApi;
import appeng.api.config.*;
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
import appeng.api.parts.ISimplifiedBundle;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerNull;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketAssemblerAnimation;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.GridAccessException;
import appeng.parts.automation.DefinitionUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.util.List;


public class TileMolecularAssembler extends AENetworkInvTile
		implements IUpgradeableHost, IConfigManagerHost, IGridTickable, ICraftingMachine, IPowerChannelState
{
	private static final int[] SIDES = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	private final InventoryCrafting craftingInv;
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 9 + 2 );
	private final IConfigManager settings;
	private final UpgradeInventory upgrades;
	private ISimplifiedBundle lightCache;
	private boolean isPowered = false;
	private ForgeDirection pushDirection = ForgeDirection.UNKNOWN;
	private ItemStack myPattern = null;
	private ICraftingPatternDetails myPlan = null;
	private double progress = 0;
	private boolean isAwake = false;
	private boolean forcePlan = false;
	private boolean reboot = true;

	public TileMolecularAssembler()
	{
		final ITileDefinition assembler = AEApi.instance().definitions().blocks().molecularAssembler();

		this.settings = new ConfigManager( this );
		this.settings.registerSetting( Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		this.inv.setMaxStackSize( 1 );
		this.getProxy().setIdlePowerUsage( 0.0 );
		this.upgrades = new DefinitionUpgradeInventory( assembler, this, this.getUpgradeSlots() );
		this.craftingInv = new InventoryCrafting( new ContainerNull(), 3, 3 );
	}

	private int getUpgradeSlots()
	{
		return 5;
	}

	@Override
	public boolean pushPattern( final ICraftingPatternDetails patternDetails, final InventoryCrafting table, final ForgeDirection where )
	{
		if( this.myPattern == null )
		{
			boolean isEmpty = true;
			for( int x = 0; x < this.inv.getSizeInventory(); x++ )
			{
				isEmpty = this.inv.getStackInSlot( x ) == null && isEmpty;
			}

			if( isEmpty && patternDetails.isCraftable() )
			{
				this.forcePlan = true;
				this.myPlan = patternDetails;
				this.pushDirection = where;

				for( int x = 0; x < table.getSizeInventory(); x++ )
				{
					this.inv.setInventorySlotContents( x, table.getStackInSlot( x ) );
				}

				this.updateSleepiness();
				this.markDirty();
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
		return this.inv.getStackInSlot( 9 ) != null;
	}

	private boolean hasMats()
	{
		if( this.myPlan == null )
		{
			return false;
		}

		for( int x = 0; x < this.craftingInv.getSizeInventory(); x++ )
		{
			this.craftingInv.setInventorySlotContents( x, this.inv.getStackInSlot( x ) );
		}

		return this.myPlan.getOutput( this.craftingInv, this.getWorldObj() ) != null;
	}

	@Override
	public boolean acceptsPlans()
	{
		return this.inv.getStackInSlot( 10 ) == null;
	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		return this.upgrades.getInstalledUpgrades( u );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileMolecularAssembler( final ByteBuf data )
	{
		final boolean oldPower = this.isPowered;
		this.isPowered = data.readBoolean();
		return this.isPowered != oldPower;
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileMolecularAssembler( final ByteBuf data )
	{
		data.writeBoolean( this.isPowered );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileMolecularAssembler( final NBTTagCompound data )
	{
		if( this.forcePlan && this.myPlan != null )
		{
			final ItemStack pattern = this.myPlan.getPattern();
			if( pattern != null )
			{
				final NBTTagCompound compound = new NBTTagCompound();
				pattern.writeToNBT( compound );
				data.setTag( "myPlan", compound );
				data.setInteger( "pushDirection", this.pushDirection.ordinal() );
			}
		}

		this.upgrades.writeToNBT( data, "upgrades" );
		this.inv.writeToNBT( data, "inv" );
		this.settings.writeToNBT( data );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileMolecularAssembler( final NBTTagCompound data )
	{
		if( data.hasKey( "myPlan" ) )
		{
			final ItemStack myPat = ItemStack.loadItemStackFromNBT( data.getCompoundTag( "myPlan" ) );

			if( myPat != null && myPat.getItem() instanceof ItemEncodedPattern )
			{
				final World w = this.getWorldObj();
				final ItemEncodedPattern iep = (ItemEncodedPattern) myPat.getItem();
				final ICraftingPatternDetails ph = iep.getPatternForItem( myPat, w );
				if( ph != null && ph.isCraftable() )
				{
					this.forcePlan = true;
					this.myPlan = ph;
					this.pushDirection = ForgeDirection.getOrientation( data.getInteger( "pushDirection" ) );
				}
			}
		}

		this.upgrades.readFromNBT( data, "upgrades" );
		this.inv.readFromNBT( data, "inv" );
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

		final ItemStack is = this.inv.getStackInSlot( 10 );

		if( is != null && is.getItem() instanceof ItemEncodedPattern )
		{
			if( !Platform.isSameItem( is, this.myPattern ) )
			{
				final World w = this.getWorldObj();
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
			this.myPattern = null;
			this.pushDirection = ForgeDirection.UNKNOWN;
		}

		this.updateSleepiness();
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
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
	public IInventory getInventoryByName( final String name )
	{
		if( name.equals( "upgrades" ) )
		{
			return this.upgrades;
		}

		if( name.equals( "mac" ) )
		{
			return this.inv;
		}

		return null;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{

	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		if( i >= 9 )
		{
			return false;
		}

		if( this.hasPattern() )
		{
			return this.myPlan.isValidItemForSlot( i, itemstack, this.getWorldObj() );
		}

		return false;
	}

	private boolean hasPattern()
	{
		return this.myPlan != null && this.inv.getStackInSlot( 10 ) != null;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( inv == this.inv )
		{
			this.recalculatePlan();
		}
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final int side )
	{
		return slotIndex == 9;
	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection whichSide )
	{
		return SIDES;
	}

	public int getCraftingProgress()
	{
		return (int) this.progress;
	}

	@Override
	public void getDrops( final World w, final int x, final int y, final int z, final List<ItemStack> drops )
	{
		super.getDrops( w, x, y, z, drops );

		for( int h = 0; h < this.upgrades.getSizeInventory(); h++ )
		{
			final ItemStack is = this.upgrades.getStackInSlot( h );
			if( is != null )
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
		if( this.inv.getStackInSlot( 9 ) != null )
		{
			this.pushOut( this.inv.getStackInSlot( 9 ) );

			// did it eject?
			if( this.inv.getStackInSlot( 9 ) == null )
			{
				this.markDirty();
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
				this.craftingInv.setInventorySlotContents( x, this.inv.getStackInSlot( x ) );
			}

			this.progress = 0;
			final ItemStack output = this.myPlan.getOutput( this.craftingInv, this.getWorldObj() );
			if( output != null )
			{
				FMLCommonHandler.instance().firePlayerCraftingEvent( Platform.getPlayer( (WorldServer) this.getWorldObj() ), output, this.craftingInv );

				this.pushOut( output.copy() );

				for( int x = 0; x < this.craftingInv.getSizeInventory(); x++ )
				{
					this.inv.setInventorySlotContents( x, Platform.getContainerItem( this.craftingInv.getStackInSlot( x ) ) );
				}

				if( this.inv.getStackInSlot( 10 ) == null )
				{
					this.forcePlan = false;
					this.myPlan = null;
					this.pushDirection = ForgeDirection.UNKNOWN;
				}

				this.ejectHeldItems();

				try
				{
					final TargetPoint where = new TargetPoint( this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 32 );
					final IAEItemStack item = AEItemStack.create( output );
					NetworkHandler.instance.sendToAllAround( new PacketAssemblerAnimation( this.xCoord, this.yCoord, this.zCoord, (byte) speed, item ), where );
				}
				catch( final IOException e )
				{
					// ;P
				}

				this.markDirty();
				this.updateSleepiness();
				return this.isAwake ? TickRateModulation.IDLE : TickRateModulation.SLEEP;
			}
		}

		return TickRateModulation.FASTER;
	}

	private void ejectHeldItems()
	{
		if( this.inv.getStackInSlot( 9 ) == null )
		{
			for( int x = 0; x < 9; x++ )
			{
				final ItemStack is = this.inv.getStackInSlot( x );
				if( is != null )
				{
					if( this.myPlan == null || !this.myPlan.isValidItemForSlot( x, is, this.worldObj ) )
					{
						this.inv.setInventorySlotContents( 9, is );
						this.inv.setInventorySlotContents( x, null );
						this.markDirty();
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
			return (int) ( this.getProxy().getEnergy().extractAEPower( ticksPassed * bonusValue * acceleratorTax, Actionable.MODULATE, PowerMultiplier.CONFIG ) / acceleratorTax );
		}
		catch( final GridAccessException e )
		{
			return 0;
		}
	}

	private void pushOut( ItemStack output )
	{
		if( this.pushDirection == ForgeDirection.UNKNOWN )
		{
			for( final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
			{
				output = this.pushTo( output, d );
			}
		}
		else
		{
			output = this.pushTo( output, this.pushDirection );
		}

		if( output == null && this.forcePlan )
		{
			this.forcePlan = false;
			this.recalculatePlan();
		}

		this.inv.setInventorySlotContents( 9, output );
	}

	private ItemStack pushTo( ItemStack output, final ForgeDirection d )
	{
		if( output == null )
		{
			return output;
		}

		final TileEntity te = this.getWorldObj().getTileEntity( this.xCoord + d.offsetX, this.yCoord + d.offsetY, this.zCoord + d.offsetZ );

		if( te == null )
		{
			return output;
		}

		final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( te, d.getOpposite() );

		if( adaptor == null )
		{
			return output;
		}

		final int size = output.stackSize;
		output = adaptor.addItems( output );
		final int newSize = output == null ? 0 : output.stackSize;

		if( size != newSize )
		{
			this.markDirty();
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

	public ISimplifiedBundle getLightCache()
	{
		return this.lightCache;
	}

	public void setLightCache( final ISimplifiedBundle lightCache )
	{
		this.lightCache = lightCache;
	}
}
