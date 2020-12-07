package appeng.integration.modules.gregtech;

import appeng.util.inv.ItemSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class GTCEItemHandler extends GTCEInventoryAdaptor
{
    ArrayList<Integer> GTCElenses = new ArrayList<>(Arrays.asList(15085,15214,15092,15111,15113,15219,15218,15243,15244,15209,15117,15206,15216,15331,15212,15213,15154,15122,15157,15190,15247));
    ArrayList<Integer> GTCEmolds = new ArrayList<>(Arrays.asList(32301,32303,32304,32305,32306,32307,32308,32309,32313,32314,32315,32317,32350,32351,32352,32353,32354,32355,32356,32358,32359,32360,32361,32363,32364,32365,32366,32367,32368,32369,32370,32371,32372,32373));
    Item smallGearExtruderShape = Item.getByNameOrId("contenttweaker:smallgearextrudershape");
    Item creativePortableTankMold = Item.getByNameOrId("contenttweaker:creativeportabletankmold");

    protected final IItemHandler itemHandler;

    public GTCEItemHandler(IItemHandler itemHandler)
    {
        this.itemHandler = itemHandler;
    }

    boolean isBlockableItem(ItemStack stack)
    {
        if ( stack.getItem() == Item.getByNameOrId("gregtech:meta_item_1") ) {
            int metadata = stack.getItemDamage();
            if ( metadata == 32766 || GTCElenses.contains(metadata) || GTCEmolds.contains(metadata)) {
                return false;
            }
        }
        if ( stack.getItem() == smallGearExtruderShape ) return false;
        if ( stack.getItem() == creativePortableTankMold) return false;
        return true;
    }

    @Override
    public boolean canRemoveAllExceptCircuits()
    {
        int slots = this.itemHandler.getSlots();
        for ( int slot = 0; slot < slots; slot++ ) {
            ItemStack is = this.itemHandler.getStackInSlot(slot);
            if ( is.isEmpty() || !isBlockableItem(is) ) continue;

            return false;
        }
        return true;
    }

    @Override
    public Iterator<ItemSlot> iterator()
    {
        return null;
    }
}
