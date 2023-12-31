package appeng.integration.modules.curio;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

public class CurioModule {
    public static final String MODID = "curios";
    public static final ResourceLocation ID_ITEM_HANDLER = new ResourceLocation(MODID, "item_handler");
    public static final EntityCapability<IItemHandler, Void> ITEM_HANDLER = EntityCapability.createVoid(ID_ITEM_HANDLER,
            IItemHandler.class);
}
