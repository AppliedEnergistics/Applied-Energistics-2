package appeng.integration.modules.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;

import appeng.api.integrations.igtooltip.providers.ModNameProvider;

class ModNameProviderAdapter<T> extends BaseProvider implements IBlockComponentProvider {
    private final ModNameProvider<? super T> provider;

    private final Class<T> objectClass;

    public ModNameProviderAdapter(Identifier id, ModNameProvider<? super T> provider, Class<T> objectClass) {
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
            tooltip.replace(JadeIds.CORE_MOD_NAME, Component.literal(modName)
                    .withStyle(IWailaConfig.get().formatting().getItemModNameStyle()));
        }
    }
}
