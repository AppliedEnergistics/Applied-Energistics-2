package appeng.integration.modules.jade;

import net.minecraft.resources.Identifier;

import snownee.jade.api.IJadeProvider;

public class BaseProvider implements IJadeProvider {
    private final Identifier id;

    private final int priority;

    public BaseProvider(Identifier id, int priority) {
        this.id = id;
        this.priority = priority;
    }

    @Override
    public final Identifier getUid() {
        return id;
    }

    @Override
    public final int getDefaultPriority() {
        return priority;
    }
}
