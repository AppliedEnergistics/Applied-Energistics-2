package appeng.helpers.externalstorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import appeng.api.stacks.AEKeyType;
import appeng.util.IVariantConversion;

/**
 * Exposes a {@link GenericStackInv} as the platforms external item storage interface.
 */
public class GenericStackItemStorage extends GenericStackInvStorage<ItemVariant> {
    public GenericStackItemStorage(GenericStackInv inv) {
        super(IVariantConversion.ITEM, AEKeyType.items(), inv);
    }
}
