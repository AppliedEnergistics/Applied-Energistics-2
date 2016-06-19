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


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.AchievementList;


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

		if( par1ItemStack.getItem() == Item.getItemFromBlock( Blocks.CRAFTING_TABLE ) )
		{
			this.thePlayer.addStat( AchievementList.BUILD_WORK_BENCH, 1 );
		}

		if( par1ItemStack.getItem() instanceof ItemPickaxe )
		{
			this.thePlayer.addStat( AchievementList.BUILD_PICKAXE, 1 );
		}

		if( par1ItemStack.getItem() == Item.getItemFromBlock( Blocks.FURNACE ) )
		{
			this.thePlayer.addStat( AchievementList.BUILD_FURNACE, 1 );
		}

		if( par1ItemStack.getItem() instanceof ItemHoe )
		{
			this.thePlayer.addStat( AchievementList.BUILD_HOE, 1 );
		}

		if( par1ItemStack.getItem() == Items.BREAD )
		{
			this.thePlayer.addStat( AchievementList.MAKE_BREAD, 1 );
		}

		if( par1ItemStack.getItem() == Items.CAKE )
		{
			this.thePlayer.addStat( AchievementList.BAKE_CAKE, 1 );
		}

		if( par1ItemStack.getItem() instanceof ItemPickaxe && ( (ItemTool) par1ItemStack.getItem() ).getToolMaterial() != Item.ToolMaterial.WOOD )
		{
			this.thePlayer.addStat( AchievementList.BUILD_BETTER_PICKAXE, 1 );
		}

		if( par1ItemStack.getItem() instanceof ItemSword )
		{
			this.thePlayer.addStat( AchievementList.BUILD_SWORD, 1 );
		}

		if( par1ItemStack.getItem() == Item.getItemFromBlock( Blocks.ENCHANTING_TABLE ) )
		{
			this.thePlayer.addStat( AchievementList.ENCHANTMENTS, 1 );
		}

		if( par1ItemStack.getItem() == Item.getItemFromBlock( Blocks.BOOKSHELF ) )
		{
			this.thePlayer.addStat( AchievementList.BOOKCASE, 1 );
		}
	}

	@Override
	public void onPickupFromSlot( final EntityPlayer playerIn, final ItemStack stack )
	{
		net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent( playerIn, stack, this.craftMatrix );
		this.onCrafting( stack );
		net.minecraftforge.common.ForgeHooks.setCraftingPlayer( playerIn );
		final InventoryCrafting ic = new InventoryCrafting( this.getContainer(), 3, 3 );

		for( int x = 0; x < this.craftMatrix.getSizeInventory(); x++ )
		{
			ic.setInventorySlotContents( x, this.craftMatrix.getStackInSlot( x ) );
		}

		final ItemStack[] aitemstack = CraftingManager.getInstance().getRemainingItems( ic, playerIn.worldObj );

		for( int x = 0; x < this.craftMatrix.getSizeInventory(); x++ )
		{
			this.craftMatrix.setInventorySlotContents( x, ic.getStackInSlot( x ) );
		}

		net.minecraftforge.common.ForgeHooks.setCraftingPlayer( null );

		for( int i = 0; i < aitemstack.length; ++i )
		{
			final ItemStack itemstack1 = this.craftMatrix.getStackInSlot( i );
			final ItemStack itemstack2 = aitemstack[i];

			if( itemstack1 != null )
			{
				this.craftMatrix.decrStackSize( i, 1 );
			}

			if( itemstack2 != null )
			{
				if( this.craftMatrix.getStackInSlot( i ) == null )
				{
					this.craftMatrix.setInventorySlotContents( i, itemstack2 );
				}
				else if( !this.thePlayer.inventory.addItemStackToInventory( itemstack2 ) )
				{
					this.thePlayer.dropItem( itemstack2, false );
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
