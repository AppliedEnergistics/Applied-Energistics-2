package appeng.integration.modules.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

import appeng.api.integrations.igtooltip.providers.NameProvider;

class NameProviderAdapter<T> extends BaseProvider implements IBlockComponentProvider {
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
            tooltip.remove(Identifiers.CORE_OBJECT_NAME);
            tooltip.add(0, name.copy().withStyle(style -> {
                // Don't overwrite a text color if one is present
                if (style.getColor() == null) {
                    return style.withColor(ChatFormatting.WHITE);
                } else {
                    return style;
                }
            }), Identifiers.CORE_OBJECT_NAME);
        }
    }
}
