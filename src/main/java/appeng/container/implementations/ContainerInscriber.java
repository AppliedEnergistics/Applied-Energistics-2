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

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.util.AEItemDefinition;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;

public class ContainerInscriber extends ContainerUpgradeable implements IProgressProvider
{

	final TileInscriber ti;

	final Slot top;
	final Slot middle;
	final Slot bottom;

	@GuiSync(2)
	public int maxProcessingTime = -1;

	@GuiSync(3)
	public int processingTime = -1;

	public ContainerInscriber(InventoryPlayer ip, TileInscriber te)
	{
		super( ip, te );
		this.ti = te;

		this.addSlotToContainer( this.top = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, this.ti, 0, 45, 16, this.invPlayer ) );
		this.addSlotToContainer( this.bottom = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, this.ti, 1, 45, 62, this.invPlayer ) );
		this.addSlotToContainer( this.middle = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_INPUT, this.ti, 2, 63, 39, this.invPlayer ) );

		this.addSlotToContainer( new SlotOutput( this.ti, 3, 113, 40, -1 ) );

		this.bindPlayerInventory( ip, 0, this.getHeight() - /* height of player inventory */82 );

	}

	@Override
	protected int getHeight()
	{
		return 176;
	}

	@Override
	public int availableUpgrades()
	{
		return 3;
	}

	@Override
	protected boolean supportCapacity()
	{
		return false;
	}

	@Override
	/**
	 * Overridden super.setupConfig to prevent setting up the fake slots
	 */
	protected void setupConfig()
	{
		this.setupUpgrades();
	}

	@Override
	public boolean isValidForSlot(Slot s, ItemStack is)
	{
		ItemStack PlateA = this.ti.getStackInSlot( 0 );
		ItemStack PlateB = this.ti.getStackInSlot( 1 );

		if ( s == this.middle )
		{
			for (ItemStack i : Inscribe.PLATES )
			{
				if ( Platform.isSameItemPrecise( i, is ) )
					return false;
			}

			boolean matches = false;
			boolean found = false;

			for (InscriberRecipe i : Inscribe.RECIPES )
			{
				boolean matchA = (PlateA == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateA, i.plateA )) && // and...
						(PlateB == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateB, i.plateB ));

				boolean matchB = (PlateB == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateB, i.plateA )) && // and...
						(PlateA == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateA, i.plateB ));

				if ( matchA || matchB )
				{
					matches = true;
					for (ItemStack option : i.imprintable)
					{
						if ( Platform.isSameItemPrecise( is, option ) )
							found = true;
					}

				}
			}

			if ( matches && !found )
				return false;
		}

		if ( (s == this.top && PlateB != null) || (s == this.bottom && PlateA != null) )
		{
			boolean isValid = false;
			ItemStack otherSlot = null;
			if ( s == this.top )
				otherSlot = this.bottom.getStack();
			else
				otherSlot = this.top.getStack();

			// name presses
			final AEItemDefinition namePress = AEApi.instance().definitions().materials().namePress();
			if ( namePress.sameAsStack( otherSlot ) )
			{
				return namePress.sameAsStack( is );
			}

			// everything else
			for (InscriberRecipe i : Inscribe.RECIPES )
			{
				if ( Platform.isSameItemPrecise( i.plateA, otherSlot ) )
				{
					isValid = Platform.isSameItemPrecise( is, i.plateB );
				}
				else if ( Platform.isSameItemPrecise( i.plateB, otherSlot ) )
				{
					isValid = Platform.isSameItemPrecise( is, i.plateA );
				}

				if ( isValid )
					break;
			}

			if ( !isValid )
				return false;
		}

		return true;
	}

	@Override
	public void detectAndSendChanges()
	{
		this.standardDetectAndSendChanges();

		if ( Platform.isServer() )
		{
			this.maxProcessingTime = this.ti.maxProcessingTime;
			this.processingTime = this.ti.processingTime;
		}
	}

	@Override
	public int getCurrentProgress()
	{
		return this.processingTime;
	}

	@Override
	public int getMaxProgress()
	{
		return this.maxProcessingTime;
	}
}
