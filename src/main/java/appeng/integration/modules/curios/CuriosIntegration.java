package appeng.integration.modules.curios;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

public class CuriosIntegration {
    public static final EntityCapability<IItemHandler, Void> ITEM_HANDLER = EntityCapability.createVoid(
            Identifier.fromNamespaceAndPath("curios", "item_handler"),
            IItemHandler.class);
}
