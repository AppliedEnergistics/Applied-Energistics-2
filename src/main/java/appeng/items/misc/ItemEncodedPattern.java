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

package appeng.items.misc;


import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.items.ItemEncodedPatternRenderer;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.helpers.PatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


public class ItemEncodedPattern extends AEBaseItem implements ICraftingPatternItem
{
	// rather simple client side caching.
	private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<ItemStack, ItemStack>();

	public ItemEncodedPattern()
	{
		this.setFeature( EnumSet.of( AEFeature.Patterns ) );
		this.setMaxStackSize( 1 );
		if( Platform.isClient() )
		{
			MinecraftForgeClient.registerItemRenderer( this, new ItemEncodedPatternRenderer() );
		}
	}

	@Override
	public ItemStack onItemRightClick( final ItemStack stack, final World w, final EntityPlayer player )
	{
		this.clearPattern( stack, player );

		return stack;
	}

	@Override
	public boolean onItemUseFirst( final ItemStack stack, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( ForgeEventFactory.onItemUseStart( player, stack, 1 ) <= 0 )
			return true;

		return this.clearPattern( stack, player );
	}

	private boolean clearPattern( final ItemStack stack, final EntityPlayer player )
	{
		if( player.isSneaking() )
		{
			if( Platform.isClient() )
			{
				return false;
			}

			final InventoryPlayer inv = player.inventory;

			for( int s = 0; s < player.inventory.getSizeInventory(); s++ )
			{
				if( inv.getStackInSlot( s ) == stack )
				{
					for( final ItemStack blankPattern : AEApi.instance().definitions().materials().blankPattern().maybeStack( stack.stackSize ).asSet() )
					{
						inv.setInventorySlotContents( s, blankPattern );
					}

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		final ICraftingPatternDetails details = this.getPatternForItem( stack, player.worldObj );

		if( details == null )
		{
			lines.add( EnumChatFormatting.RED + GuiText.InvalidPattern.getLocal() );
			return;
		}

		final boolean isCrafting = details.isCraftable();
		final boolean substitute = details.canSubstitute();

		final IAEItemStack[] in = details.getCondensedInputs();
		final IAEItemStack[] out = details.getCondensedOutputs();

		final String label = ( isCrafting ? GuiText.Crafts.getLocal() : GuiText.Creates.getLocal() ) + ": ";
		final String and = ' ' + GuiText.And.getLocal() + ' ';
		final String with = GuiText.With.getLocal() + ": ";

		boolean first = true;
		for( final IAEItemStack anOut : out )
		{
			if( anOut == null )
			{
				continue;
			}

			lines.add( ( first ? label : and ) + anOut.getStackSize() + ' ' + Platform.getItemDisplayName( anOut ) );
			first = false;
		}

		first = true;
		for( final IAEItemStack anIn : in )
		{
			if( anIn == null )
			{
				continue;
			}

			lines.add( ( first ? with : and ) + anIn.getStackSize() + ' ' + Platform.getItemDisplayName( anIn ) );
			first = false;
		}

		final String substitutionLabel = GuiText.Substitute.getLocal() + " ";
		final String canSubstitute = substitute ? GuiText.Yes.getLocal() : GuiText.No.getLocal();

		lines.add( substitutionLabel + canSubstitute );
	}

	@Override
	public ICraftingPatternDetails getPatternForItem( final ItemStack is, final World w )
	{
		try
		{
			return new PatternHelper( is, w );
		}
		catch( final Throwable t )
		{
			return null;
		}
	}

	public ItemStack getOutput( final ItemStack item )
	{
		ItemStack out = SIMPLE_CACHE.get( item );
		if( out != null )
		{
			return out;
		}

		final World w = CommonHelper.proxy.getWorld();
		if( w == null )
		{
			return null;
		}

		final ICraftingPatternDetails details = this.getPatternForItem( item, w );

		if( details == null )
		{
			return null;
		}

		SIMPLE_CACHE.put( item, out = details.getCondensedOutputs()[0].getItemStack() );
		return out;
	}
}
