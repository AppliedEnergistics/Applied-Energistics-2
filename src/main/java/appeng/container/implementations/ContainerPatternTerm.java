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

package appeng.container.implementations;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.definitions.IDefinitions;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.sync.packets.PacketPatternSlot;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ItemViewCell;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;


public class ContainerPatternTerm extends ContainerMEMonitorable implements IAEAppEngInventory, IOptionalSlotHost, IContainerCraftingPacket
{

	public final PartPatternTerminal ct;
	final AppEngInternalInventory cOut = new AppEngInternalInventory( null, 1 );
	final IInventory crafting;
	final SlotFakeCraftingMatrix[] craftingSlots = new SlotFakeCraftingMatrix[9];
	final OptionalSlotFake[] outputSlots = new OptionalSlotFake[3];
	final SlotPatternTerm craftSlot;
	final SlotRestrictedInput patternSlotIN;
	final SlotRestrictedInput patternSlotOUT;
	@GuiSync( 97 )
	public boolean craftingMode = true;

	public ContainerPatternTerm( InventoryPlayer ip, ITerminalHost monitorable )
	{
		super( ip, monitorable, false );
		this.ct = (PartPatternTerminal) monitorable;

		IInventory patternInv = this.ct.getInventoryByName( "pattern" );
		IInventory output = this.ct.getInventoryByName( "output" );
		this.crafting = this.ct.getInventoryByName( "crafting" );

		for( int y = 0; y < 3; y++ )
		{
			for( int x = 0; x < 3; x++ )
			{
				this.addSlotToContainer( this.craftingSlots[x + y * 3] = new SlotFakeCraftingMatrix( this.crafting, x + y * 3, 18 + x * 18, -76 + y * 18 ) );
			}
		}

		this.addSlotToContainer( this.craftSlot = new SlotPatternTerm( ip.player, this.mySrc, this.powerSrc, monitorable, this.crafting, patternInv, this.cOut, 110, -76 + 18, this, 2, this ) );
		this.craftSlot.IIcon = -1;

		for( int y = 0; y < 3; y++ )
		{
			this.addSlotToContainer( this.outputSlots[y] = new SlotPatternOutputs( output, this, y, 110, -76 + y * 18, 0, 0, 1 ) );
			this.outputSlots[y].renderDisabled = false;
			this.outputSlots[y].IIcon = -1;
		}

		this.addSlotToContainer( this.patternSlotIN = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.BLANK_PATTERN, patternInv, 0, 147, -72 - 9, this.invPlayer ) );
		this.addSlotToContainer( this.patternSlotOUT = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, patternInv, 1, 147, -72 + 34, this.invPlayer ) );

		this.patternSlotOUT.setStackLimit( 1 );

		this.bindPlayerInventory( ip, 0, 0 );
		this.updateOrderOfOutputSlots();
	}

	private void updateOrderOfOutputSlots()
	{
		if( !this.craftingMode )
		{
			this.craftSlot.xDisplayPosition = -9000;

			for( int y = 0; y < 3; y++ )
			{
				this.outputSlots[y].xDisplayPosition = this.outputSlots[y].defX;
			}
		}
		else
		{
			this.craftSlot.xDisplayPosition = this.craftSlot.defX;

			for( int y = 0; y < 3; y++ )
			{
				this.outputSlots[y].xDisplayPosition = -9000;
			}
		}
	}

	@Override
	public void putStackInSlot( int par1, ItemStack par2ItemStack )
	{
		super.putStackInSlot( par1, par2ItemStack );
		this.getAndUpdateOutput();
	}

	@Override
	public void putStacksInSlots( ItemStack[] par1ArrayOfItemStack )
	{
		super.putStacksInSlots( par1ArrayOfItemStack );
		this.getAndUpdateOutput();
	}

	public ItemStack getAndUpdateOutput()
	{
		InventoryCrafting ic = new InventoryCrafting( this, 3, 3 );
		for( int x = 0; x < ic.getSizeInventory(); x++ )
		{
			ic.setInventorySlotContents( x, this.crafting.getStackInSlot( x ) );
		}

		ItemStack is = CraftingManager.getInstance().findMatchingRecipe( ic, this.getPlayerInv().player.worldObj );
		this.cOut.setInventorySlotContents( 0, is );
		return is;
	}

	@Override
	public void saveChanges()
	{

	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{

	}

	public void encode()
	{
		ItemStack output = this.patternSlotOUT.getStack();

		ItemStack[] in = this.getInputs();
		ItemStack[] out = this.getOutputs();

		// if there is no input, this would be silly.
		if( in == null || out == null )
		{
			return;
		}

		// first check the output slots, should either be null, or a pattern
		if( output != null && !this.isPattern( output ) )
		{
			return;
		}// if nothing is there we should snag a new pattern.
		else if( output == null )
		{
			output = this.patternSlotIN.getStack();
			if( output == null || !this.isPattern( output ) )
			{
				return; // no blanks.
			}

			// remove one, and clear the input slot.
			output.stackSize--;
			if( output.stackSize == 0 )
			{
				this.patternSlotIN.putStack( null );
			}

			// add a new encoded pattern.
			for( ItemStack encodedPatternStack : AEApi.instance().definitions().items().encodedPattern().maybeStack( 1 ).asSet() )
			{
				output = encodedPatternStack;
				this.patternSlotOUT.putStack( output );
			}
		}

		// encode the slot.
		NBTTagCompound encodedValue = new NBTTagCompound();

		NBTTagList tagIn = new NBTTagList();
		NBTTagList tagOut = new NBTTagList();

		for( ItemStack i : in )
		{
			tagIn.appendTag( this.createItemTag( i ) );
		}

		for( ItemStack i : out )
		{
			tagOut.appendTag( this.createItemTag( i ) );
		}

		encodedValue.setTag( "in", tagIn );
		encodedValue.setTag( "out", tagOut );
		encodedValue.setBoolean( "crafting", this.craftingMode );

		output.setTagCompound( encodedValue );
	}

	private ItemStack[] getInputs()
	{
		ItemStack[] input = new ItemStack[9];
		boolean hasValue = false;

		for( int x = 0; x < this.craftingSlots.length; x++ )
		{
			input[x] = this.craftingSlots[x].getStack();
			if( input[x] != null )
			{
				hasValue = true;
			}
		}

		if( hasValue )
		{
			return input;
		}

		return null;
	}

	private ItemStack[] getOutputs()
	{
		if( this.craftingMode )
		{
			ItemStack out = this.getAndUpdateOutput();
			if( out != null && out.stackSize > 0 )
			{
				return new ItemStack[] { out };
			}
		}
		else
		{
			List<ItemStack> list = new ArrayList<ItemStack>( 3 );
			boolean hasValue = false;

			for( OptionalSlotFake outputSlot : this.outputSlots )
			{
				ItemStack out = outputSlot.getStack();
				if( out != null && out.stackSize > 0 )
				{
					list.add( out );
					hasValue = true;
				}
			}

			if( hasValue )
			{
				return list.toArray( new ItemStack[list.size()] );
			}
		}

		return null;
	}

	private boolean isPattern( ItemStack output )
	{
		if( output == null )
		{
			return false;
		}

		final IDefinitions definitions = AEApi.instance().definitions();

		boolean isPattern = definitions.items().encodedPattern().isSameAs( output );
		isPattern |= definitions.materials().blankPattern().isSameAs( output );

		return isPattern;
	}

	private NBTBase createItemTag( ItemStack i )
	{
		NBTTagCompound c = new NBTTagCompound();

		if( i != null )
		{
			i.writeToNBT( c );
		}

		return c;
	}

	@Override
	public boolean isSlotEnabled( int idx )
	{
		if( idx == 1 )
		{
			return Platform.isServer() ? !this.ct.isCraftingRecipe() : !this.craftingMode;
		}
		else if( idx == 2 )
		{
			return Platform.isServer() ? this.ct.isCraftingRecipe() : this.craftingMode;
		}
		else
		{
			return false;
		}
	}

	public void craftOrGetItem( PacketPatternSlot packetPatternSlot )
	{
		if( packetPatternSlot.slotItem != null && this.cellInv != null )
		{
			IAEItemStack out = packetPatternSlot.slotItem.copy();

			InventoryAdaptor inv = new AdaptorPlayerHand( this.getPlayerInv().player );
			InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor( this.getPlayerInv().player, ForgeDirection.UNKNOWN );
			if( packetPatternSlot.shift )
			{
				inv = playerInv;
			}

			if( inv.simulateAdd( out.getItemStack() ) != null )
			{
				return;
			}

			IAEItemStack extracted = Platform.poweredExtraction( this.powerSrc, this.cellInv, out, this.mySrc );
			EntityPlayer p = this.getPlayerInv().player;

			if( extracted != null )
			{
				inv.addItems( extracted.getItemStack() );
				if( p instanceof EntityPlayerMP )
				{
					this.updateHeld( (EntityPlayerMP) p );
				}
				this.detectAndSendChanges();
				return;
			}

			InventoryCrafting ic = new InventoryCrafting( new ContainerNull(), 3, 3 );
			InventoryCrafting real = new InventoryCrafting( new ContainerNull(), 3, 3 );
			for( int x = 0; x < 9; x++ )
			{
				ic.setInventorySlotContents( x, packetPatternSlot.pattern[x] == null ? null : packetPatternSlot.pattern[x].getItemStack() );
			}

			IRecipe r = Platform.findMatchingRecipe( ic, p.worldObj );

			if( r == null )
			{
				return;
			}

			IMEMonitor<IAEItemStack> storage = this.ct.getItemInventory();
			IItemList<IAEItemStack> all = storage.getStorageList();

			ItemStack is = r.getCraftingResult( ic );

			for( int x = 0; x < ic.getSizeInventory(); x++ )
			{
				if( ic.getStackInSlot( x ) != null )
				{
					ItemStack pulled = Platform.extractItemsByRecipe( this.powerSrc, this.mySrc, storage, p.worldObj, r, is, ic, ic.getStackInSlot( x ), x, all, Actionable.MODULATE, ItemViewCell.createFilter( this.getViewCells() ) );
					real.setInventorySlotContents( x, pulled );
				}
			}

			IRecipe rr = Platform.findMatchingRecipe( real, p.worldObj );

			if( rr == r && Platform.isSameItemPrecise( rr.getCraftingResult( real ), is ) )
			{
				SlotCrafting sc = new SlotCrafting( p, real, this.cOut, 0, 0, 0 );
				sc.onPickupFromSlot( p, is );

				for( int x = 0; x < real.getSizeInventory(); x++ )
				{
					ItemStack failed = playerInv.addItems( real.getStackInSlot( x ) );
					if( failed != null )
					{
						p.dropPlayerItemWithRandomChoice( failed, false );
					}
				}

				inv.addItems( is );
				if( p instanceof EntityPlayerMP )
				{
					this.updateHeld( (EntityPlayerMP) p );
				}
				this.detectAndSendChanges();
			}
			else
			{
				for( int x = 0; x < real.getSizeInventory(); x++ )
				{
					ItemStack failed = real.getStackInSlot( x );
					if( failed != null )
					{
						this.cellInv.injectItems( AEItemStack.create( failed ), Actionable.MODULATE, new MachineSource( this.ct ) );
					}
				}
			}
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		if( Platform.isServer() )
		{
			if( this.craftingMode != this.ct.isCraftingRecipe() )
			{
				this.craftingMode = this.ct.isCraftingRecipe();
				this.updateOrderOfOutputSlots();
			}
		}
	}

	@Override
	public void onUpdate( String field, Object oldValue, Object newValue )
	{
		super.onUpdate( field, oldValue, newValue );

		if( field.equals( "craftingMode" ) )
		{
			this.getAndUpdateOutput();
			this.updateOrderOfOutputSlots();
		}
	}

	@Override
	public void onSlotChange( Slot s )
	{
		if( s == this.patternSlotOUT && Platform.isServer() )
		{
			for( Object crafter : this.crafters )
			{
				ICrafting icrafting = (ICrafting) crafter;

				for( Object g : this.inventorySlots )
				{
					if( g instanceof OptionalSlotFake || g instanceof SlotFakeCraftingMatrix )
					{
						Slot sri = (Slot) g;
						icrafting.sendSlotContents( this, sri.slotNumber, sri.getStack() );
					}
				}
				( (EntityPlayerMP) icrafting ).isChangingQuantityOnly = false;
			}
			this.detectAndSendChanges();
		}
	}

	public void clear()
	{
		for( Slot s : this.craftingSlots )
		{
			s.putStack( null );
		}

		for( Slot s : this.outputSlots )
		{
			s.putStack( null );
		}

		this.detectAndSendChanges();
		this.getAndUpdateOutput();
	}

	@Override
	public IInventory getInventoryByName( String name )
	{
		if( name.equals( "player" ) )
		{
			return this.invPlayer;
		}
		return this.ct.getInventoryByName( name );
	}

	@Override
	public boolean useRealItems()
	{
		return false;
	}
}
