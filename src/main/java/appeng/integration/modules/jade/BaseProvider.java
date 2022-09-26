package appeng.integration.modules.jade;

import net.minecraft.resources.ResourceLocation;

public class BaseProvider {
    private final ResourceLocation id;

    private final int priority;

    public BaseProvider(ResourceLocation id, int priority) {
        this.id = id;
        this.priority = priority;
    }

    public final ResourceLocation getUid() {
        return id;
    }

    public final int getDefaultPriority() {
        return priority;
    }
}
