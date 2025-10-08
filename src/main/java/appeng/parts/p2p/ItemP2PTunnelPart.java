package appeng.parts.p2p;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;

import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEKeyType;

public class ItemP2PTunnelPart extends ResourceHandlerP2PTunnelPart<ItemP2PTunnelPart, ItemResource> {
    public ItemP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, Capabilities.Item.BLOCK, ItemResource.EMPTY, AEKeyType.items());
    }
}
