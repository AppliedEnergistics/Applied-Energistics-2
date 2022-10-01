package appeng.integration.modules.jade;

import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.IJadeProvider;

public class BaseProvider implements IJadeProvider {
    private final ResourceLocation id;

    private final int priority;

    public BaseProvider(ResourceLocation id, int priority) {
        this.id = id;
        this.priority = priority;
    }

    @Override
    public final ResourceLocation getUid() {
        return id;
    }

    @Override
    public final int getDefaultPriority() {
        return priority;
    }
}
