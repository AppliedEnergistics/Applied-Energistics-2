package appeng.recipes.game;

import appeng.api.AEApi;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IDefinitions;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Created by Tom on 10/2/2015.
 */
public class WirelessTerminalRecipe implements IRecipe {

    private final IComparableDefinition terminal;
    private final ItemStack waterBucket;

    private static final ArrayList<String> dyes = new ArrayList<String>();
    static {
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
        terminal = definitions.items().wirelessTerminal();
        waterBucket = new ItemStack( Items.water_bucket, 1 );
    }

    @Override
    public boolean matches( InventoryCrafting inv, World w ) {
        return this.getOutput( inv, false ) != null;
    }

    @Nullable
    private ItemStack getOutput( final IInventory inv, final boolean createTerminal )
    {
        ItemStack inputTerminal = null;
        String color = null;
        for( int i = 0; i < 9; i++ )
        {
            //If the slot is empty, ignore it
            if( inv.getStackInSlot( i ) != null ) {
                //if it contains a terminal, return that terminal
                if( inputTerminal==null && this.terminal.isSameAs( inv.getStackInSlot( i ) ) )
                {
                    inputTerminal = inv.getStackInSlot( i );
                }
                //If no dye is found yet, then check if the item is a dye
                else if( color == null )
                {
                    //check if the other item is a dye
                    color = getColorFromItem( inv.getStackInSlot( i ) );

                    //Check if a color was found or not
                    if( color == null )
                    {
                        return null;
                    }
                }
                //if the item matches nothing fail
                else
                {
                    return null;
                }
            }

        }

        //Check if both a terminal and a dye were found
        if( inputTerminal==null || color == null )
        {
            return null;
        }

        ItemStack output = ItemStack.copyItemStack( inputTerminal );
        if( createTerminal ) {
            NBTTagCompound tag = output.getTagCompound();
            tag.setString( "color", color );
        }

        return output;
    }

    private String getColorFromItem(ItemStack potentialColor)
    {
        //check if the "color" is water, in which case return fluix
        if( ItemStack.areItemStacksEqual( waterBucket, potentialColor ) )
        {
            return "fluix";
        }

        //check through the ore dictionary to see if the item is a dye
        for( String dye : dyes )
        {
            ArrayList<ItemStack> dyeItems = OreDictionary.getOres( dye );
            for( ItemStack itm : dyeItems )
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
    public ItemStack getCraftingResult( InventoryCrafting inv ) {
        return this.getOutput( inv, true );
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }
}
