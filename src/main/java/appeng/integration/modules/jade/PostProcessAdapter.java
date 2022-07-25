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

import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;

/**
 * Provider with highest possible priority to go last after all other providers.
 */
class PostProcessAdapter<T> implements IBlockComponentProvider {
    private final InGameTooltipProvider<? super T> provider;

    private final Class<T> objectClass;

    public PostProcessAdapter(InGameTooltipProvider<? super T> provider, Class<T> objectClass) {
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var obj = objectClass.cast(accessor.getBlockEntity());
        var context = new InGameTooltipContext(
                accessor.getServerData(),
                accessor.getHitResult().getLocation(),
                accessor.getPlayer());
        var modName = provider.getModName(obj, context);
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

    @Override
    public ResourceLocation getUid() {
        return Identifiers.CORE_MOD_NAME;
    }

    public int getDefaultPriority() {
        return Integer.MAX_VALUE;
    }
}
