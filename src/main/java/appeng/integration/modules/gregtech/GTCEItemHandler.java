package appeng.integration.modules.gregtech;

import appeng.util.inv.ItemSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;

public class GTCEItemHandler extends GTCEInventoryAdaptor {

    final static ItemStack integratedCircuit = new ItemStack((Item.getByNameOrId("gregtech:meta_item_1")), 1, 32766); // Gregtech Community Edition Integrated Circuit
    protected final IItemHandler itemHandler;


    public GTCEItemHandler( IItemHandler itemHandler )
    {
        this.itemHandler = itemHandler;
    }

    @Override
    public boolean canRemoveAllExceptCircuits()
    {
        int slots = this.itemHandler.getSlots();
        for( int slot = 0; slot < slots; slot++ )
        {
            ItemStack is = this.itemHandler.getStackInSlot( slot );
            if ( is.isEmpty() || is.isItemEqual(integratedCircuit)) continue;
            return false;
        }
        return true;
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return null;
    }
}
