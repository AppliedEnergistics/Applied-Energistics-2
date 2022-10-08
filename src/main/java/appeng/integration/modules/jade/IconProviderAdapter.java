package appeng.integration.modules.jade;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;

import appeng.api.integrations.igtooltip.providers.IconProvider;

class IconProviderAdapter<T extends BlockEntity> extends BaseProvider implements IComponentProvider {
    private final IElementHelper elementHelper;

    private final IconProvider<? super T> iconProvider;

    private final Class<T> objectClass;

    public IconProviderAdapter(ResourceLocation id, int priority, IElementHelper elementHelper,
            IconProvider<? super T> iconProvider, Class<T> objectClass) {
        super(id, priority);
        this.elementHelper = elementHelper;
        this.iconProvider = iconProvider;
        this.objectClass = objectClass;
    }

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        var object = objectClass.cast(accessor.getBlockEntity());
        var context = ContextHelper.getContext(accessor);

        var icon = iconProvider.getIcon(object, context);
        return icon != null ? elementHelper.item(icon) : null;
    }

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
    }
}
