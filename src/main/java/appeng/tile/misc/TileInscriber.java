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

package appeng.tile.misc;


import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.ITileDefinition;
import appeng.api.features.IInscriberRecipe;
import appeng.api.features.IInscriberRecipeBuilder;
import appeng.api.features.InscriberProcessType;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.parts.automation.DefinitionUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.WrapperInventoryRange;
import appeng.util.item.AEItemStack;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class TileInscriber extends AENetworkPowerTile implements IGridTickable, IUpgradeableHost, IConfigManagerHost
{

	private static final int SLOT_TOP = 0;
	private static final int SLOT_BOTTOM = 1;
	private static final int SLOT_MIDDLE = 2;
	private static final int SLOT_OUT = 3;

	private final int maxProcessingTime = 100;
	private final int[] top = { SLOT_TOP };
	private final int[] bottom = { SLOT_BOTTOM };
	private final int[] sides = { SLOT_MIDDLE, SLOT_OUT };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 4 );
	private final IConfigManager settings;
	private final UpgradeInventory upgrades;
	private int processingTime = 0;
	// cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the normal routine.
	private boolean smash;
	private int finalStep;
	private long clientStart;
	private final IItemHandler topItemHandler = new ItemHandler( 0, 0 );
	private final IItemHandler bottomItemHandler = new ItemHandler( 1, 0 );
	private final IItemHandler sideItemHandler = new ItemHandler( 2, 3 );

	@Reflected
	public TileInscriber()
	{
		this.getProxy().setValidSides( EnumSet.noneOf( EnumFacing.class ) );
		this.setInternalMaxPower( 1500 );
		this.getProxy().setIdlePowerUsage( 0 );
		this.settings = new ConfigManager( this );

		final ITileDefinition inscriberDefinition = AEApi.instance().definitions().blocks().inscriber();
		this.upgrades = new DefinitionUpgradeInventory( inscriberDefinition, this, this.getUpgradeSlots() );
	}

	private int getUpgradeSlots()
	{
		return 3;
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.COVERED;
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileInscriber( final NBTTagCompound data )
	{
		this.inv.writeToNBT( data, "inscriberInv" );
		this.upgrades.writeToNBT( data, "upgrades" );
		this.settings.writeToNBT( data );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileInscriber( final NBTTagCompound data )
	{
		this.inv.readFromNBT( data, "inscriberInv" );
		this.upgrades.readFromNBT( data, "upgrades" );
		this.settings.readFromNBT( data );
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileInscriber( final ByteBuf data ) throws IOException
	{
		final int slot = data.readByte();

		final boolean oldSmash = this.isSmash();
		final boolean newSmash = ( slot & 64 ) == 64;

		if( oldSmash != newSmash && newSmash )
		{
			this.setSmash( true );
			this.setClientStart( System.currentTimeMillis() );
		}

		for( int num = 0; num < this.inv.getSizeInventory(); num++ )
		{
			if( ( slot & ( 1 << num ) ) > 0 )
			{
				this.inv.setInventorySlotContents( num, AEItemStack.loadItemStackFromPacket( data ).getItemStack() );
			}
			else
			{
				this.inv.setInventorySlotContents( num, null );
			}
		}

		return false;
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileInscriber( final ByteBuf data ) throws IOException
	{
		int slot = this.isSmash() ? 64 : 0;

		for( int num = 0; num < this.inv.getSizeInventory(); num++ )
		{
			if( this.inv.getStackInSlot( num ) != null )
			{
				slot |= ( 1 << num );
			}
		}

		data.writeByte( slot );
		for( int num = 0; num < this.inv.getSizeInventory(); num++ )
		{
			if( ( slot & ( 1 << num ) ) > 0 )
			{
				final AEItemStack st = AEItemStack.create( this.inv.getStackInSlot( num ) );
				st.writeToPacket( data );
			}
		}
	}

	@Override
	public void setOrientation( final EnumFacing inForward, final EnumFacing inUp )
	{
		super.setOrientation( inForward, inUp );
		this.getProxy().setValidSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
		this.setPowerSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
	}

	@Override
	public void getDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{
		super.getDrops( w, pos, drops );

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
	public boolean requiresTESR()
	{
		return true;
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
		if( this.isSmash() )
		{
			return false;
		}

		if( i == SLOT_TOP || i == SLOT_BOTTOM )
		{
			if( AEApi.instance().definitions().materials().namePress().isSameAs( itemstack ) )
			{
				return true;
			}

			for( final ItemStack optionals : AEApi.instance().registries().inscriber().getOptionals() )
			{
				if( Platform.itemComparisons().isSameItem( optionals, itemstack ) )
				{
					return true;
				}
			}
		}

		return i == SLOT_MIDDLE;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		try
		{
			if( mc != InvOperation.markDirty )
			{
				if( slot != SLOT_OUT )
				{
					this.setProcessingTime( 0 );
				}

				if( !this.isSmash() )
				{
					this.markForUpdate();
				}

				this.getProxy().getTick().wakeDevice( this.getProxy().getNode() );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final EnumFacing side )
	{
		if( this.isSmash() )
		{
			return false;
		}

		return slotIndex == SLOT_TOP || slotIndex == SLOT_BOTTOM || slotIndex == SLOT_OUT;
	}

	@Override
	public int[] getAccessibleSlotsBySide( final EnumFacing d )
	{
		if( d == EnumFacing.UP )
		{
			return this.top;
		}

		if( d == EnumFacing.DOWN )
		{
			return this.bottom;
		}

		return this.sides;
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.Inscriber.getMin(), TickRates.Inscriber.getMax(), !this.hasWork(), false );
	}

	private boolean hasWork()
	{
		if( this.getTask() != null )
		{
			return true;
		}

		this.setProcessingTime( 0 );
		return this.isSmash();
	}

	@Nullable
	public IInscriberRecipe getTask()
	{
		final ItemStack plateA = this.getStackInSlot( 0 );
		final ItemStack plateB = this.getStackInSlot( 1 );
		ItemStack renamedItem = this.getStackInSlot( 2 );

		if( plateA != null && plateA.stackSize > 1 )
		{
			return null;
		}

		if( plateB != null && plateB.stackSize > 1 )
		{
			return null;
		}

		if( renamedItem != null && renamedItem.stackSize > 1 )
		{
			return null;
		}

		final IComparableDefinition namePress = AEApi.instance().definitions().materials().namePress();
		final boolean isNameA = namePress.isSameAs( plateA );
		final boolean isNameB = namePress.isSameAs( plateB );

		if( ( isNameA || isNameB ) && ( isNameA || plateA == null ) && ( isNameB || plateB == null ) )
		{
			if( renamedItem != null )
			{
				String name = "";

				if( plateA != null )
				{
					final NBTTagCompound tag = Platform.openNbtData( plateA );
					name += tag.getString( "InscribeName" );
				}

				if( plateB != null )
				{
					final NBTTagCompound tag = Platform.openNbtData( plateB );
					if( name.length() > 0 )
					{
						name += " ";
					}
					name += tag.getString( "InscribeName" );
				}

				final ItemStack startingItem = renamedItem.copy();
				renamedItem = renamedItem.copy();
				final NBTTagCompound tag = Platform.openNbtData( renamedItem );

				final NBTTagCompound display = tag.getCompoundTag( "display" );
				tag.setTag( "display", display );

				if( name.length() > 0 )
				{
					display.setString( "Name", name );
				}
				else
				{
					display.removeTag( "Name" );
				}

				final List<ItemStack> inputs = Lists.newArrayList( startingItem );
				final InscriberProcessType type = InscriberProcessType.INSCRIBE;

				final IInscriberRecipeBuilder builder = AEApi.instance().registries().inscriber().builder();
				return builder.withInputs( inputs )
						.withOutput( renamedItem )
						.withTopOptional( plateA )
						.withBottomOptional( plateB )
						.withProcessType( type )
						.build();
			}
		}

		for( final IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
		{

			final boolean matchA = ( plateA == null && !recipe.getTopOptional().isPresent() ) || ( Platform.itemComparisons().isSameItem( plateA,
					recipe.getTopOptional().orElse( null ) ) ) && // and...
					( plateB == null && !recipe.getBottomOptional().isPresent() ) | ( Platform.itemComparisons().isSameItem( plateB,
							recipe.getBottomOptional().orElse( null ) ) );

			final boolean matchB = ( plateB == null && !recipe.getTopOptional().isPresent() ) || ( Platform.itemComparisons().isSameItem( plateB,
					recipe.getTopOptional().orElse( null ) ) ) && // and...
					( plateA == null && !recipe.getBottomOptional().isPresent() ) | ( Platform.itemComparisons().isSameItem( plateA,
							recipe.getBottomOptional().orElse( null ) ) );

			if( matchA || matchB )
			{
				for( final ItemStack option : recipe.getInputs() )
				{
					if( Platform.itemComparisons().isSameItem( option, this.getStackInSlot( 2 ) ) )
					{
						return recipe;
					}
				}
			}
		}
		return null;
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		if( this.isSmash() )
		{
			this.finalStep++;
			if( this.finalStep == 8 )
			{
				final IInscriberRecipe out = this.getTask();
				if( out != null )
				{
					final ItemStack outputCopy = out.getOutput().copy();
					final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this.inv, SLOT_OUT, 1, true ), EnumFacing.UP );

					if( ad.addItems( outputCopy ) == null )
					{
						this.setProcessingTime( 0 );
						if( out.getProcessType() == InscriberProcessType.PRESS )
						{
							this.setInventorySlotContents( SLOT_TOP, null );
							this.setInventorySlotContents( SLOT_BOTTOM, null );
						}
						this.setInventorySlotContents( SLOT_MIDDLE, null );
					}
				}

				this.markDirty();
			}
			else if( this.finalStep == 16 )
			{
				this.finalStep = 0;
				this.setSmash( false );
				this.markForUpdate();
			}
		}
		else
		{
			try
			{
				final IEnergyGrid eg = this.getProxy().getEnergy();
				IEnergySource src = this;

				// Base 1, increase by 1 for each card
				final int speedFactor = 1 + this.upgrades.getInstalledUpgrades( Upgrades.SPEED );
				final int powerConsumption = 10 * speedFactor;
				final double powerThreshold = powerConsumption - 0.01;
				double powerReq = this.extractAEPower( powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG );

				if( powerReq <= powerThreshold )
				{
					src = eg;
					powerReq = eg.extractAEPower( powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG );
				}

				if( powerReq > powerThreshold )
				{
					src.extractAEPower( powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG );

					if( this.getProcessingTime() == 0 )
					{
						this.setProcessingTime( this.getProcessingTime() + speedFactor );
					}
					else
					{
						this.setProcessingTime( this.getProcessingTime() + ticksSinceLastCall * speedFactor );
					}
				}
			}
			catch( final GridAccessException e )
			{
				// :P
			}

			if( this.getProcessingTime() > this.getMaxProcessingTime() )
			{
				this.setProcessingTime( this.getMaxProcessingTime() );
				final IInscriberRecipe out = this.getTask();
				if( out != null )
				{
					final ItemStack outputCopy = out.getOutput().copy();
					final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this.inv, SLOT_OUT, 1, true ), EnumFacing.UP );
					if( ad.simulateAdd( outputCopy ) == null )
					{
						this.setSmash( true );
						this.finalStep = 0;
						this.markForUpdate();
					}
				}
			}
		}

		return this.hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.settings;
	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		if( name.equals( "inv" ) )
		{
			return this.inv;
		}

		if( name.equals( "upgrades" ) )
		{
			return this.upgrades;
		}

		return null;
	}

	@Override
	public boolean hasCapability( Capability<?> capability, EnumFacing facing )
	{
		if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return true;
		}

		return super.hasCapability( capability, facing );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getCapability( Capability<T> capability, @Nullable EnumFacing facing )
	{
		if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			if( facing == getUp() )
			{
				return (T) topItemHandler;
			}
			else if( facing == getUp().getOpposite() )
			{
				return (T) bottomItemHandler;
			}
			else
			{
				return (T) sideItemHandler;
			}
		}

		return super.getCapability( capability, facing );
	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		return this.upgrades.getInstalledUpgrades( u );
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{
	}

	public long getClientStart()
	{
		return this.clientStart;
	}

	private void setClientStart( final long clientStart )
	{
		this.clientStart = clientStart;
	}

	public boolean isSmash()
	{
		return this.smash;
	}

	public void setSmash( final boolean smash )
	{
		this.smash = smash;
	}

	public int getMaxProcessingTime()
	{
		return this.maxProcessingTime;
	}

	public int getProcessingTime()
	{
		return this.processingTime;
	}

	private void setProcessingTime( final int processingTime )
	{
		this.processingTime = processingTime;
	}

	/**
	 * This is an item handler that exposes the inscribers inventory while providing simulation capabilities that do not
	 * reset the progress if there's already an item in a slot. Previously, the progress of the inscriber was reset when
	 * another mod attempetd insertion of items when there were already items in the slot.
	 */
	private class ItemHandler implements IItemHandler
	{

		private final int insertSlot;

		private final int extractSlot;

		private ItemHandler( int insertSlot, int extractSlot )
		{
			this.insertSlot = insertSlot;
			this.extractSlot = extractSlot;
		}

		@Override
		public int getSlots()
		{
			return insertSlot != extractSlot ? 2 : 1;
		}

		@Override
		public ItemStack getStackInSlot( int slot )
		{
			if( slot == 0 )
			{
				return inv.getStackInSlot( insertSlot );
			}
			else if( insertSlot != extractSlot && slot == 1 )
			{
				return inv.getStackInSlot( extractSlot );
			}
			return null;
		}

		@Override
		public ItemStack insertItem( int slot, ItemStack stack, boolean simulate )
		{
			if( slot != 0 || stack == null )
			{
				return stack;
			}

			// If there's already an item stack in the slot, we don't allow insertion and don't do any other checks
			if( inv.getStackInSlot( insertSlot ) != null )
			{
				return stack;
			}

			AdaptorIInventory adapter = new AdaptorIInventory( new WrapperInventoryRange( TileInscriber.this, insertSlot, 1, true ) );

			if( simulate )
			{
				return adapter.simulateAdd( stack );
			}
			else
			{
				return adapter.addItems( stack );
			}
		}

		@Override
		public ItemStack extractItem( int slot, int amount, boolean simulate )
		{
			final int validExtractSlot = ( insertSlot == extractSlot ) ? 0 : 1;

			if( slot != validExtractSlot || amount == 0 )
			{
				return null;
			}

			AdaptorIInventory adapter = new AdaptorIInventory( new WrapperInventoryRange( TileInscriber.this, extractSlot, 1, true ) );

			if( simulate )
			{
				return adapter.simulateRemove( amount, null, null );
			}
			else
			{
				return adapter.removeItems( amount, null, null );
			}
		}

	}

}
