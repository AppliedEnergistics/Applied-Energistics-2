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


import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.helpers.InvalidPatternHelper;
import appeng.helpers.PatternHelper;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


public class ItemEncodedPattern extends AEBaseItem implements ICraftingPatternItem
{
	// rather simple client side caching.
	private static final Map<ItemStack, ItemStack> SIMPLE_CACHE = new WeakHashMap<>();

	public ItemEncodedPattern()
	{
		this.setMaxStackSize( 64 );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick( final World w, final EntityPlayer player, final EnumHand hand )
	{
		this.clearPattern( player.getHeldItem( hand ), player );

		return new ActionResult<>( EnumActionResult.SUCCESS, player.getHeldItem( hand ) );
	}

	@Override
	public EnumActionResult onItemUseFirst( final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand )
	{
		return this.clearPattern( player.getHeldItem( hand ), player ) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
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

			ItemStack is = AEApi.instance().definitions().materials().blankPattern().maybeStack( stack.getCount() ).orElse( ItemStack.EMPTY );
			if( !is.isEmpty() )
			{
				for( int s = 0; s < player.inventory.getSizeInventory(); s++ )
				{
					if( inv.getStackInSlot( s ) == stack )
					{
						inv.setInventorySlotContents( s, is );
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void addCheckedInformation( final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips )
	{
		final ICraftingPatternDetails details = this.getPatternForItem( stack, world );

		if( details == null )
		{
			if( !stack.hasTagCompound() )
			{
				return;
			}

			stack.setStackDisplayName( TextFormatting.RED + GuiText.InvalidPattern.getLocal() );

			InvalidPatternHelper invalid = new InvalidPatternHelper( stack );

			final String label = ( invalid.isCraftable() ? GuiText.Crafts.getLocal() : GuiText.Creates.getLocal() ) + ": ";
			final String and = ' ' + GuiText.And.getLocal() + ' ';
			final String with = GuiText.With.getLocal() + ": ";

			boolean first = true;
			for( final InvalidPatternHelper.PatternIngredient output : invalid.getOutputs() )
			{
				lines.add( ( first ? label : and ) + output.getFormattedToolTip() );
				first = false;
			}

			first = true;
			for( final InvalidPatternHelper.PatternIngredient input : invalid.getInputs() )
			{
				lines.add( ( first ? with : and ) + input.getFormattedToolTip() );
				first = false;
			}

			if( invalid.isCraftable() )
			{
				final String substitutionLabel = GuiText.Substitute.getLocal() + " ";
				final String canSubstitute = invalid.canSubstitute() ? GuiText.Yes.getLocal() : GuiText.No.getLocal();

				lines.add( substitutionLabel + canSubstitute );
			}

			return;
		}

		if( stack.hasDisplayName() )
		{
			stack.removeSubCompound( "display" );
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

		if( isCrafting )
		{
			final String substitutionLabel = GuiText.Substitute.getLocal() + " ";
			final String canSubstitute = substitute ? GuiText.Yes.getLocal() : GuiText.No.getLocal();

			lines.add( substitutionLabel + canSubstitute );
		}
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

		final World w = AppEng.proxy.getWorld();
		if( w == null )
		{
			return ItemStack.EMPTY;
		}

		final ICraftingPatternDetails details = this.getPatternForItem( item, w );

		out = details != null ? details.getOutputs()[0].createItemStack() : ItemStack.EMPTY;

		SIMPLE_CACHE.put( item, out );
		return out;
	}
}
