package appeng.integration.modules.jade;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

import appeng.api.integrations.igtooltip.providers.BodyProvider;

/**
 * Delegation provider for tiles through {@link IBlockComponentProvider}
 */
class BodyProviderAdapter<T extends BlockEntity> extends BaseProvider implements IBlockComponentProvider {
    private final BodyProvider<? super T> provider;

    private final Class<T> objectClass;

    public BodyProviderAdapter(Identifier id, int priority, BodyProvider<? super T> provider,
            Class<T> objectClass) {
        super(id, priority);
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        tooltip.remove(JadeIds.UNIVERSAL_ENERGY_STORAGE_DETAILED);
        tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE_DETAILED);

        var context = ContextHelper.getContext(accessor);
        var tooltipBuilder = new JadeTooltipBuilder(tooltip);
        var obj = objectClass.cast(accessor.getBlockEntity());

        // Append the rest of the tooltip
        provider.buildTooltip(obj, context, tooltipBuilder);
    }
}
