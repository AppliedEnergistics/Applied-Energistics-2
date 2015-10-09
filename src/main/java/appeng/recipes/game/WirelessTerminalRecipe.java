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

package appeng.recipes.game;


import java.util.ArrayList;
import javax.annotation.Nullable;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.util.AEColor;


public class WirelessTerminalRecipe implements IRecipe
{

    private final Item terminal;
    private final ItemStack waterBucket;

    private static final ArrayList<String> dyes = new ArrayList<String>();
    static
    {
        dyes.add( "dyeBlack" );
        dyes.add( "dyeBlue" );
        dyes.add( "dyeBrown" );
        dyes.add( "dyeCyan" );
        dyes.add( "dyeGray" );
        dyes.add( "dyeGreen" );
        dyes.add( "dyeLightBlue" );
        dyes.add( "dyeLightGray" );
        dyes.add( "dyeLime" );
        dyes.add( "dyeMagenta" );
        dyes.add( "dyeOrange" );
        dyes.add( "dyePink" );
        dyes.add( "dyePurple" );
        dyes.add( "dyeRed" );
        dyes.add( "dyeWhite" );
        dyes.add( "dyeYellow" );
    }

    public WirelessTerminalRecipe()
    {
        final IDefinitions definitions = AEApi.instance().definitions();
        terminal = definitions.items().wirelessTerminal().item( AEColor.Black ); //pick a random color, items don't even have NBT data so it's ignored
        waterBucket = new ItemStack( Items.water_bucket, 1 );
    }

    @Override
    public boolean matches( final InventoryCrafting inv, final World w )
    {
        return this.getOutput( inv, false ) != null;
    }

    @Nullable
    private ItemStack getOutput( final IInventory inv, final boolean createTerminal )
    {
        ItemStack inputTerminal = null;
        String color = null;
        int numIngredients = 0;
        for( int i = 0; i < 9; i++ )
        {
            //If the slot is empty, ignore it
            if( inv.getStackInSlot( i ) != null )
            {
                numIngredients++;
                //if it contains a terminal, return that terminal
                if( inv.getStackInSlot( i ).getItem().equals( this.terminal ) )
                {
                    inputTerminal = inv.getStackInSlot( i );
                }
                else
                {
                    //check if the other item is a dye
                    color = getColorFromItem( inv.getStackInSlot( i ) );
                }
            }

        }

        //Check if both a terminal and a dye were found
        if( inputTerminal==null || color == null || numIngredients!=2 )
        {
            return null;
        }

        final ItemStack output = ItemStack.copyItemStack( inputTerminal );
        if( createTerminal )
        {
            final NBTTagCompound tag = output.getTagCompound();
            tag.setString( "color", color );
        }

        return output;
    }

    private String getColorFromItem( final ItemStack potentialColor )
    {
        //check if the "color" is water, in which case return fluix
        if( ItemStack.areItemStacksEqual( waterBucket, potentialColor ) )
        {
            return "fluix";
        }

        //check through the ore dictionary to see if the item is a dye
        for( final String dye : dyes )
        {
            final ArrayList<ItemStack> dyeItems = OreDictionary.getOres( dye );
            for( final ItemStack itm : dyeItems )
            {
                if( OreDictionary.itemMatches( potentialColor, itm, true ) )
                {
                    return dye.replace( "dye","" ); //remove starting "dye" to get color
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack getCraftingResult( final InventoryCrafting inv )
    {
        return this.getOutput( inv, true );
    }

    @Override
    public int getRecipeSize()
    {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return null;
    }
}
