package appeng.helpers.externalstorage;

import net.neoforged.neoforge.transfer.item.ItemResource;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.AEKeyType;
import appeng.helpers.ResourceConversion;

/**
 * Exposes a {@link GenericInternalInventory} as the platforms external item storage interface.
 */
public class GenericStackItemHandler extends GenericStackInvHandler<ItemResource> {
    public GenericStackItemHandler(GenericInternalInventory inv) {
        super(ResourceConversion.ITEM, AEKeyType.items(), inv);
    }
}
