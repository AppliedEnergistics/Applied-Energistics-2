package appeng.integration.modules.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;

import appeng.api.integrations.igtooltip.providers.NameProvider;

class NameProviderAdapter<T> extends BaseProvider implements IComponentProvider {
    private static final ResourceLocation CORE_OBJECT_NAME = new ResourceLocation("waila", "object_name");

    private final NameProvider<? super T> provider;

    private final Class<T> objectClass;

    public NameProviderAdapter(ResourceLocation id, int priority, NameProvider<? super T> provider,
            Class<T> objectClass) {
        super(id, priority);
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var object = objectClass.cast(accessor.getBlockEntity());
        var context = ContextHelper.getContext(accessor);

        var name = provider.getName(object, context);

        // Replace the object name
        if (name != null) {
            tooltip.remove(CORE_OBJECT_NAME);
            tooltip.add(0, name.copy().withStyle(style -> {
                // Don't overwrite a text color if one is present
                if (style.getColor() == null) {
                    return style.withColor(ChatFormatting.WHITE);
                } else {
                    return style;
                }
            }), CORE_OBJECT_NAME);
        }
    }
}
