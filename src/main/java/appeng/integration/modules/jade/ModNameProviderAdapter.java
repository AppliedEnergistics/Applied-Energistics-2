package appeng.integration.modules.jade;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

import appeng.api.integrations.igtooltip.providers.ModNameProvider;

class ModNameProviderAdapter<T> extends BaseProvider implements IBlockComponentProvider {
    private final ModNameProvider<? super T> provider;

    private final Class<T> objectClass;

    public ModNameProviderAdapter(ResourceLocation id, ModNameProvider<? super T> provider, Class<T> objectClass) {
        super(id, Integer.MAX_VALUE);
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var object = objectClass.cast(accessor.getBlockEntity());
        var context = ContextHelper.getContext(accessor);

        var modName = provider.getModName(object, context);

        if (modName != null) {
            for (int i = 0; i < tooltip.size(); i++) {
                for (var align : IElement.Align.values()) {
                    List<IElement> line = tooltip.get(i, align);
                    for (int j = 0; j < line.size(); j++) {
                        IElement el = line.get(j);
                        if (Identifiers.CORE_MOD_NAME.equals(el.getTag())) {
                            line.set(j, tooltip.getElementHelper().text(
                                    Component.literal(
                                            String.format(config.getWailaConfig().getFormatting().getModName(),
                                                    modName))));
                        }
                    }
                }
            }
        }
    }
}
