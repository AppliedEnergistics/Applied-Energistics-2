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


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.ITileDefinition;
import appeng.api.features.IInscriberRecipe;
import appeng.api.features.InscriberProcessType;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.features.registries.entries.InscriberRecipe;
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
import appeng.util.inv.WrapperInventoryRange;
import appeng.util.item.AEItemStack;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class TileInscriber extends AENetworkPowerTile implements IGridTickable, IUpgradeableHost, IConfigManagerHost
{

	private final int maxProcessingTime = 100;
	private final int[] top = { 0 };
	private final int[] bottom = { 1 };
	private final int[] sides = { 2, 3 };
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 4 );
	private final IConfigManager settings;
	private final UpgradeInventory upgrades;
	private int processingTime = 0;
	// cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the normal routine.
	private boolean smash;
	private int finalStep;
	private long clientStart;

	@Reflected
	public TileInscriber()
	{
		this.getProxy().setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
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
	public AECableType getCableConnectionType( final ForgeDirection dir )
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
	public void setOrientation( final ForgeDirection inForward, final ForgeDirection inUp )
	{
		super.setOrientation( inForward, inUp );
		this.getProxy().setValidSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
		this.setPowerSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
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

		if( i == 0 || i == 1 )
		{
			if( AEApi.instance().definitions().materials().namePress().isSameAs( itemstack ) )
			{
				return true;
			}

			for( final ItemStack optionals : AEApi.instance().registries().inscriber().getOptionals() )
			{
				if( Platform.isSameItemPrecise( optionals, itemstack ) )
				{
					return true;
				}
			}
		}

		return i == 2;
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		try
		{
			if( mc != InvOperation.markDirty )
			{
				if( slot != 3 )
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
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final int side )
	{
		if( this.isSmash() )
		{
			return false;
		}

		return slotIndex == 0 || slotIndex == 1 || slotIndex == 3;
	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection d )
	{
		if( d == ForgeDirection.UP )
		{
			return this.top;
		}

		if( d == ForgeDirection.DOWN )
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
				final InscriberProcessType type = InscriberProcessType.Inscribe;

				return new InscriberRecipe( inputs, renamedItem, plateA, plateB, type );
			}
		}

		for( final IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
		{

			final boolean matchA = ( plateA == null && !recipe.getTopOptional().isPresent() ) || ( Platform.isSameItemPrecise( plateA, recipe.getTopOptional().orNull() ) ) && // and...
					( plateB == null && !recipe.getBottomOptional().isPresent() ) | ( Platform.isSameItemPrecise( plateB, recipe.getBottomOptional().orNull() ) );

			final boolean matchB = ( plateB == null && !recipe.getTopOptional().isPresent() ) || ( Platform.isSameItemPrecise( plateB, recipe.getTopOptional().orNull() ) ) && // and...
					( plateA == null && !recipe.getBottomOptional().isPresent() ) | ( Platform.isSameItemPrecise( plateA, recipe.getBottomOptional().orNull() ) );

			if( matchA || matchB )
			{
				for( final ItemStack option : recipe.getInputs() )
				{
					if( Platform.isSameItemPrecise( option, this.getStackInSlot( 2 ) ) )
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
					final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this.inv, 3, 1, true ), ForgeDirection.UNKNOWN );

					if( ad.addItems( outputCopy ) == null )
					{
						this.setProcessingTime( 0 );
						if( out.getProcessType() == InscriberProcessType.Press )
						{
							this.setInventorySlotContents( 0, null );
							this.setInventorySlotContents( 1, null );
						}
						this.setInventorySlotContents( 2, null );
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
					final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this.inv, 3, 1, true ), ForgeDirection.UNKNOWN );
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
}
