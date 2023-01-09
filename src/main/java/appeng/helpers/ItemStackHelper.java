package appeng.helpers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Methods to help with the added "stackSize" NBT tag to get around "Count" being written and read as a byte.
 */
public class ItemStackHelper {
    public static ItemStack stackFromNBT(NBTTagCompound itemNBT) {
        ItemStack is = new ItemStack(itemNBT);
        if (itemNBT.hasKey("stackSize")) {
            is.setCount(itemNBT.getInteger("stackSize"));
        }
        return is;
    }

    public static void stackWriteToNBT(ItemStack is, NBTTagCompound itemNBT) {
        is.writeToNBT(itemNBT);
        if (is.getCount() > Byte.MAX_VALUE) {
            itemNBT.setInteger("stackSize", is.getCount());
        }
    }

    public static NBTTagCompound stackToNBT(ItemStack is) {
        NBTTagCompound itemNBT = new NBTTagCompound();
        stackWriteToNBT(is, itemNBT);
        return itemNBT;
    }
}
