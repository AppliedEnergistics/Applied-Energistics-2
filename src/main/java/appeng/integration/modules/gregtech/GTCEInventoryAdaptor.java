package appeng.integration.modules.gregtech;

import appeng.util.inv.ItemSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class GTCEInventoryAdaptor implements Iterable<ItemSlot>
{
    public static GTCEInventoryAdaptor getAdaptor(final TileEntity te, final EnumFacing d)
    {
        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d))
        {
            // Attempt getting an IItemHandler for the given side via caps
            IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d);
            if (itemHandler != null)
            {
                return new GTCEItemHandler(itemHandler);
            }
        }
        return null;
    }

    public abstract boolean canRemoveAllExceptCircuits();
}