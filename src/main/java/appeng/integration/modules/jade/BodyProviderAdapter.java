package appeng.integration.modules.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;

import appeng.api.integrations.igtooltip.providers.BodyProvider;

/**
 * Delegation provider for tiles through {@link mcp.mobius.waila.api.IComponentProvider}
 */
class BodyProviderAdapter<T extends BlockEntity> extends BaseProvider implements IComponentProvider {
    private final BodyProvider<? super T> provider;

    private final Class<T> objectClass;

    public BodyProviderAdapter(ResourceLocation id, int priority, BodyProvider<? super T> provider,
            Class<T> objectClass) {
        super(id, priority);
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // Removes the built-in Forge-Energy progressbar
        tooltip.remove(new ResourceLocation("minecraft:fe"));
        // Removes the built-in Forge fluid bars, because usually we have 6 tanks...
        tooltip.remove(new ResourceLocation("minecraft:fluid"));

        var context = ContextHelper.getContext(accessor);
        var tooltipBuilder = new JadeTooltipBuilder(tooltip);
        var obj = objectClass.cast(accessor.getBlockEntity());

        // Append the rest of the tooltip
        provider.buildTooltip(obj, context, tooltipBuilder);
    }
}
