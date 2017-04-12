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

package appeng.container.slot;


import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;


public class AppEngCraftingSlot extends AppEngSlot
{

	/**
	 * The craft matrix inventory linked to this result slot.
	 */
	private final IInventory craftMatrix;

	/**
	 * The player that is using the GUI where this slot resides.
	 */
	private final EntityPlayer thePlayer;

	/**
	 * The number of items that have been crafted so far. Gets passed to ItemStack.onCrafting before being reset.
	 */
	private int amountCrafted;

	public AppEngCraftingSlot( final EntityPlayer par1EntityPlayer, final IInventory par2IInventory, final IInventory par3IInventory, final int par4, final int par5, final int par6 )
	{
		super( par3IInventory, par4, par5, par6 );
		this.thePlayer = par1EntityPlayer;
		this.craftMatrix = par2IInventory;
	}

	/**
	 * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
	 */
	@Override
	public boolean isItemValid( final ItemStack par1ItemStack )
	{
		return false;
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
	 * internal count then calls onCrafting(item).
	 */
	@Override
	protected void onCrafting( final ItemStack par1ItemStack, final int par2 )
	{
		this.amountCrafted += par2;
		this.onCrafting( par1ItemStack );
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
	 */
	@Override
	protected void onCrafting( final ItemStack par1ItemStack )
	{
		par1ItemStack.onCrafting( this.thePlayer.worldObj, this.thePlayer, this.amountCrafted );
		this.amountCrafted = 0;

		if( par1ItemStack.getItem() == Item.getItemFromBlock( Blocks.crafting_table ) )
		{
			this.thePlayer.addStat( AchievementList.buildWorkBench, 1 );
		}

		if( par1ItemStack.getItem() instanceof ItemPickaxe )
		{
			this.thePlayer.addStat( AchievementList.buildPickaxe, 1 );
		}

		if( par1ItemStack.getItem() == Item.getItemFromBlock( Blocks.furnace ) )
		{
			this.thePlayer.addStat( AchievementList.buildFurnace, 1 );
		}

		if( par1ItemStack.getItem() instanceof ItemHoe )
		{
			this.thePlayer.addStat( AchievementList.buildHoe, 1 );
		}

		if( par1ItemStack.getItem() == Items.bread )
		{
			this.thePlayer.addStat( AchievementList.makeBread, 1 );
		}

		if( par1ItemStack.getItem() == Items.cake )
		{
			this.thePlayer.addStat( AchievementList.bakeCake, 1 );
		}

		if( par1ItemStack.getItem() instanceof ItemPickaxe && ( (ItemTool) par1ItemStack.getItem() ).func_150913_i() != Item.ToolMaterial.WOOD )
		{
			this.thePlayer.addStat( AchievementList.buildBetterPickaxe, 1 );
		}

		if( par1ItemStack.getItem() instanceof ItemSword )
		{
			this.thePlayer.addStat( AchievementList.buildSword, 1 );
		}

		if( par1ItemStack.getItem() == Item.getItemFromBlock( Blocks.enchanting_table ) )
		{
			this.thePlayer.addStat( AchievementList.enchantments, 1 );
		}

		if( par1ItemStack.getItem() == Item.getItemFromBlock( Blocks.bookshelf ) )
		{
			this.thePlayer.addStat( AchievementList.bookcase, 1 );
		}
	}

	@Override
	public void onPickupFromSlot( final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack )
	{
		FMLCommonHandler.instance().firePlayerCraftingEvent( par1EntityPlayer, par2ItemStack, this.craftMatrix );
		this.onCrafting( par2ItemStack );

		for( int i = 0; i < this.craftMatrix.getSizeInventory(); ++i )
		{
			final ItemStack itemstack1 = this.craftMatrix.getStackInSlot( i );

			if( itemstack1 != null )
			{
				this.craftMatrix.decrStackSize( i, 1 );

				if( itemstack1.getItem().hasContainerItem( itemstack1 ) )
				{
					final ItemStack itemstack2 = itemstack1.getItem().getContainerItem( itemstack1 );

					if( itemstack2 != null && itemstack2.isItemStackDamageable() && itemstack2.getItemDamage() > itemstack2.getMaxDamage() )
					{
						MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( this.thePlayer, itemstack2 ) );
						continue;
					}

					if( !itemstack1.getItem().doesContainerItemLeaveCraftingGrid( itemstack1 ) || !this.thePlayer.inventory.addItemStackToInventory( itemstack2 ) )
					{
						if( this.craftMatrix.getStackInSlot( i ) == null )
						{
							this.craftMatrix.setInventorySlotContents( i, itemstack2 );
						}
						else
						{
							this.thePlayer.dropPlayerItemWithRandomChoice( itemstack2, false );
						}
					}
				}
			}
		}
	}

	/**
	 * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
	 * stack.
	 */
	@Override
	public ItemStack decrStackSize( final int par1 )
	{
		if( this.getHasStack() )
		{
			this.amountCrafted += Math.min( par1, this.getStack().stackSize );
		}

		return super.decrStackSize( par1 );
	}
}
