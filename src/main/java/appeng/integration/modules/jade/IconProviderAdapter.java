package appeng.integration.modules.jade;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

import appeng.api.integrations.igtooltip.providers.IconProvider;

class IconProviderAdapter<T extends BlockEntity> extends BaseProvider implements IBlockComponentProvider {
    private final IconProvider<? super T> iconProvider;

    private final Class<T> objectClass;

    public IconProviderAdapter(Identifier id, int priority,
            IconProvider<? super T> iconProvider, Class<T> objectClass) {
        super(id, priority);
        this.iconProvider = iconProvider;
        this.objectClass = objectClass;
    }

    @Override
    public @Nullable Element getIcon(BlockAccessor accessor, IPluginConfig config, @Nullable Element currentIcon) {
        var object = objectClass.cast(accessor.getBlockEntity());
        var context = ContextHelper.getContext(accessor);

        var icon = iconProvider.getIcon(object, context);
        return icon != null ? JadeUI.item(icon) : null;
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
    }
}
