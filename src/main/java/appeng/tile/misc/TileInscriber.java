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
import java.util.ArrayList;
import java.util.EnumSet;

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.parts.automation.UpgradeInventory;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
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

public class TileInscriber extends AENetworkPowerTile implements IGridTickable, IUpgradeableHost, IConfigManagerHost
{

	final int[] top = new int[] { 0 };
	final int[] bottom = new int[] { 1 };
	final int[] sides = new int[] { 2, 3 };

	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 4 );

	public final int maxProcessingTime = 100;
	public int processingTime = 0;

	// cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the normal routine.
	public boolean smash;
	public int finalStep;

	public long clientStart;

	static final ItemStack inscriberStack = AEApi.instance().blocks().blockInscriber.stack( 1 );
	private final IConfigManager settings = new ConfigManager( this );
	private final UpgradeInventory upgrades = new UpgradeInventory( inscriberStack, this, this.getUpgradeSlots() );

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileInscriber(NBTTagCompound data)
	{
		this.inv.writeToNBT( data, "inscriberInv" );
		this.upgrades.writeToNBT( data, "upgrades" );
		this.settings.writeToNBT( data );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileInscriber(NBTTagCompound data)
	{
		this.inv.readFromNBT( data, "inscriberInv" );
		this.upgrades.readFromNBT( data, "upgrades" );
		this.settings.readFromNBT( data );
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileInscriber(ByteBuf data) throws IOException
	{
		int slot = data.readByte();

		boolean oldSmash = this.smash;
		boolean newSmash = (slot & 64) == 64;

		if ( oldSmash != newSmash && newSmash )
		{
			this.smash = true;
			this.clientStart = System.currentTimeMillis();
		}

		for (int num = 0; num < this.inv.getSizeInventory(); num++)
		{
			if ( (slot & (1 << num)) > 0 )
				this.inv.setInventorySlotContents( num, AEItemStack.loadItemStackFromPacket( data ).getItemStack() );
			else
				this.inv.setInventorySlotContents( num, null );
		}

		return false;
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileInscriber(ByteBuf data) throws IOException
	{
		int slot = this.smash ? 64 : 0;

		for (int num = 0; num < this.inv.getSizeInventory(); num++)
		{
			if ( this.inv.getStackInSlot( num ) != null )
				slot = slot | (1 << num);
		}

		data.writeByte( slot );
		for (int num = 0; num < this.inv.getSizeInventory(); num++)
		{
			if ( (slot & (1 << num)) > 0 )
			{
				AEItemStack st = AEItemStack.create( this.inv.getStackInSlot( num ) );
				st.writeToPacket( data );
			}
		}
	}

	@Override
	public boolean requiresTESR()
	{
		return true;
	}

	public TileInscriber()
	{
		this.gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		this.internalMaxPower = 1500;
		this.gridProxy.setIdlePowerUsage( 0 );
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		super.setOrientation( inForward, inUp );
		this.gridProxy.setValidSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
		this.setPowerSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection d)
	{
		if ( d == ForgeDirection.UP )
			return this.top;

		if ( d == ForgeDirection.DOWN )
			return this.bottom;

		return this.sides;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if ( this.smash )
			return false;

		if ( i == 0 || i == 1 )
		{
			if ( AEApi.instance().materials().materialNamePress.sameAsStack( itemstack ) )
				return true;

			for (ItemStack s : Inscribe.plates)
				if ( Platform.isSameItemPrecise( s, itemstack ) )
					return true;
		}

		if ( i == 2 )
		{
			return true;
			// for (ItemStack s : Inscribe.inputs)
			// if ( Platform.isSameItemPrecise( s, itemstack ) )
			// return true;
		}

		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		if ( this.smash )
			return false;

		return i == 0 || i == 1 || i == 3;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		try
		{
			if ( mc != InvOperation.markDirty )
			{
				if ( slot != 3 )
					this.processingTime = 0;

				if ( !this.smash )
					this.markForUpdate();

				this.gridProxy.getTick().wakeDevice( this.gridProxy.getNode() );
			}
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	public InscriberRecipe getTask()
	{
		ItemStack PlateA = this.getStackInSlot( 0 );
		ItemStack PlateB = this.getStackInSlot( 1 );
		ItemStack renamedItem = this.getStackInSlot( 2 );

		if ( PlateA != null && PlateA.stackSize > 1 )
			return null;

		if ( PlateB != null && PlateB.stackSize > 1 )
			return null;

		if ( renamedItem != null && renamedItem.stackSize > 1 )
			return null;

		boolean isNameA = AEApi.instance().materials().materialNamePress.sameAsStack( PlateA );
		boolean isNameB = AEApi.instance().materials().materialNamePress.sameAsStack( PlateB );

		if ( (isNameA || isNameB) && (isNameA || PlateA == null) && (isNameB || PlateB == null) )
		{
			if ( renamedItem != null )
			{
				String name = "";

				if ( PlateA != null )
				{
					NBTTagCompound tag = Platform.openNbtData( PlateA );
					name += tag.getString( "InscribeName" );
				}

				if ( PlateB != null )
				{
					NBTTagCompound tag = Platform.openNbtData( PlateB );
					if ( name.length() > 0 )
						name += " ";
					name += tag.getString( "InscribeName" );
				}

				ItemStack startingItem = renamedItem.copy();
				renamedItem = renamedItem.copy();
				NBTTagCompound tag = Platform.openNbtData( renamedItem );

				NBTTagCompound display = tag.getCompoundTag( "display" );
				tag.setTag( "display", display );

				if ( name.length() > 0 )
					display.setString( "Name", name );
				else
					display.removeTag( "Name" );

				return new InscriberRecipe( new ItemStack[] { startingItem }, PlateA, PlateB, renamedItem, false );
			}
		}

		for (InscriberRecipe i : Inscribe.recipes)
		{

			boolean matchA = (PlateA == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateA, i.plateA )) && // and...
					(PlateB == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateB, i.plateB ));

			boolean matchB = (PlateB == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateB, i.plateA )) && // and...
					(PlateA == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateA, i.plateB ));

			if ( matchA || matchB )
			{
				for (ItemStack option : i.imprintable)
				{
					if ( Platform.isSameItemPrecise( option, this.getStackInSlot( 2 ) ) )
						return i;
				}
			}

		}
		return null;
	}

	private boolean hasWork()
	{
		if ( this.getTask() != null )
			return true;

		this.processingTime = 0;
		return this.smash;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.Inscriber.min, TickRates.Inscriber.max, !this.hasWork(), false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if ( this.smash )
		{

			this.finalStep++;
			if ( this.finalStep == 8 )
			{

				InscriberRecipe out = this.getTask();
				if ( out != null )
				{
					ItemStack is = out.output.copy();
					InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this.inv, 3, 1, true ), ForgeDirection.UNKNOWN );

					if ( ad.addItems( is ) == null )
					{
						this.processingTime = 0;
						if ( out.usePlates )
						{
							this.setInventorySlotContents( 0, null );
							this.setInventorySlotContents( 1, null );
						}
						this.setInventorySlotContents( 2, null );
					}
				}

				this.markDirty();

			}
			else if ( this.finalStep == 16 )
			{
				this.finalStep = 0;
				this.smash = false;
				this.markForUpdate();
			}
		}
		else
		{
			IEnergyGrid eg;
			try
			{
				eg = this.gridProxy.getEnergy();
				IEnergySource src = this;

				// Base 1, increase by 1 for each card
				int speedFactor = 1 + this.upgrades.getInstalledUpgrades( Upgrades.SPEED );
				int powerConsumption = 10 * speedFactor;
				double powerThreshold = powerConsumption - 0.01;
				double powerReq = this.extractAEPower( powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG );

				if ( powerReq <= powerThreshold )
				{
					src = eg;
					powerReq = eg.extractAEPower( powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG );
				}

				if ( powerReq > powerThreshold )
				{
					src.extractAEPower( powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG );

					if ( this.processingTime == 0 )
						this.processingTime = this.processingTime + speedFactor;
					else
						this.processingTime += TicksSinceLastCall * speedFactor;
				}
			}
			catch (GridAccessException e)
			{
				// :P
			}

			if ( this.processingTime > this.maxProcessingTime )
			{
				this.processingTime = this.maxProcessingTime;
				InscriberRecipe out = this.getTask();
				if ( out != null )
				{
					ItemStack is = out.output.copy();
					InventoryAdaptor ad = InventoryAdaptor.getAdaptor( new WrapperInventoryRange( this.inv, 3, 1, true ), ForgeDirection.UNKNOWN );
					if ( ad.simulateAdd( is ) == null )
					{
						this.smash = true;
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
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "inv" ) )
			return this.inv;

		if ( name.equals( "upgrades" ) )
			return this.upgrades;

		return null;
	}

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return this.upgrades.getInstalledUpgrades( u );
	}

	protected int getUpgradeSlots()
	{
		return 3;
	}

	@Override
	public void getDrops(World w, int x, int y, int z, ArrayList<ItemStack> drops)
	{
		super.getDrops( w, x, y, z, drops );

		for (int h = 0; h < this.upgrades.getSizeInventory(); h++)
		{
			ItemStack is = this.upgrades.getStackInSlot( h );
			if ( is != null )
				drops.add( is );
		}
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{
	}
}
