package appeng.integration.modules.jade;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import appeng.api.integrations.igtooltip.providers.IconProvider;

class IconProviderAdapter<T extends BlockEntity> extends BaseProvider implements IBlockComponentProvider {
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
