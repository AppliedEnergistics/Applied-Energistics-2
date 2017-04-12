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


import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.api.features.IInscriberRecipe;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class ContainerInscriber extends ContainerUpgradeable implements IProgressProvider
{

	private final TileInscriber ti;

	private final Slot top;
	private final Slot middle;
	private final Slot bottom;

	@GuiSync( 2 )
	public int maxProcessingTime = -1;

	@GuiSync( 3 )
	public int processingTime = -1;

	public ContainerInscriber( final InventoryPlayer ip, final TileInscriber te )
	{
		super( ip, te );
		this.ti = te;

		this.addSlotToContainer( this.top = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, this.ti, 0, 45, 16, this.getInventoryPlayer() ) );
		this.addSlotToContainer( this.bottom = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, this.ti, 1, 45, 62, this.getInventoryPlayer() ) );
		this.addSlotToContainer( this.middle = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_INPUT, this.ti, 2, 63, 39, this.getInventoryPlayer() ) );

		this.addSlotToContainer( new SlotOutput( this.ti, 3, 113, 40, -1 ) );
	}

	@Override
	protected int getHeight()
	{
		return 176;
	}

	@Override
	/**
	 * Overridden super.setupConfig to prevent setting up the fake slots
	 */ protected void setupConfig()
	{
		this.setupUpgrades();
	}

	@Override
	protected boolean supportCapacity()
	{
		return false;
	}

	@Override
	public int availableUpgrades()
	{
		return 3;
	}

	@Override
	public void detectAndSendChanges()
	{
		this.standardDetectAndSendChanges();

		if( Platform.isServer() )
		{
			this.maxProcessingTime = this.ti.getMaxProcessingTime();
			this.processingTime = this.ti.getProcessingTime();
		}
	}

	@Override
	public boolean isValidForSlot( final Slot s, final ItemStack is )
	{
		final ItemStack top = this.ti.getStackInSlot( 0 );
		final ItemStack bot = this.ti.getStackInSlot( 1 );

		if( s == this.middle )
		{
			for( final ItemStack optional : AEApi.instance().registries().inscriber().getOptionals() )
			{
				if( Platform.isSameItemPrecise( optional, is ) )
				{
					return false;
				}
			}

			boolean matches = false;
			boolean found = false;

			for( final IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
			{
				final boolean matchA = ( top == null && !recipe.getTopOptional().isPresent() ) || ( Platform.isSameItemPrecise( top, recipe.getTopOptional().orNull() ) ) && // and...
						( bot == null && !recipe.getBottomOptional().isPresent() ) | ( Platform.isSameItemPrecise( bot, recipe.getBottomOptional().orNull() ) );

				final boolean matchB = ( bot == null && !recipe.getTopOptional().isPresent() ) || ( Platform.isSameItemPrecise( bot, recipe.getTopOptional().orNull() ) ) && // and...
						( top == null && !recipe.getBottomOptional().isPresent() ) | ( Platform.isSameItemPrecise( top, recipe.getBottomOptional().orNull() ) );

				if( matchA || matchB )
				{
					matches = true;
					for( final ItemStack option : recipe.getInputs() )
					{
						if( Platform.isSameItemPrecise( is, option ) )
						{
							found = true;
						}
					}
				}
			}

			if( matches && !found )
			{
				return false;
			}
		}

		if( ( s == this.top && bot != null ) || ( s == this.bottom && top != null ) )
		{
			ItemStack otherSlot = null;
			if( s == this.top )
			{
				otherSlot = this.bottom.getStack();
			}
			else
			{
				otherSlot = this.top.getStack();
			}

			// name presses
			final IItemDefinition namePress = AEApi.instance().definitions().materials().namePress();
			if( namePress.isSameAs( otherSlot ) )
			{
				return namePress.isSameAs( is );
			}

			// everything else
			boolean isValid = false;
			for( final IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes() )
			{
				if( Platform.isSameItemPrecise( recipe.getTopOptional().orNull(), otherSlot ) )
				{
					isValid = Platform.isSameItemPrecise( is, recipe.getBottomOptional().orNull() );
				}
				else if( Platform.isSameItemPrecise( recipe.getBottomOptional().orNull(), otherSlot ) )
				{
					isValid = Platform.isSameItemPrecise( is, recipe.getTopOptional().orNull() );
				}

				if( isValid )
				{
					break;
				}
			}

			if( !isValid )
			{
				return false;
			}
		}

		return true;
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
