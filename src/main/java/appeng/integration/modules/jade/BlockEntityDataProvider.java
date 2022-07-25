package appeng.integration.modules.jade;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.api.integrations.waila.AEJadeIds;

/**
 * Delegation provider for tiles through {@link snownee.jade.api.IBlockComponentProvider}
 */
public final class BlockEntityDataProvider<T extends BlockEntity> implements IBlockComponentProvider {
    private final IElementHelper elementHelper;
    private final InGameTooltipProvider<? super T> provider;

    private final Class<T> objectClass;

    public BlockEntityDataProvider(IElementHelper elementHelper, InGameTooltipProvider<? super T> provider,
            Class<T> objectClass) {
        this.elementHelper = elementHelper;
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public ResourceLocation getUid() {
        return AEJadeIds.BLOCK_ENTITIES_PROVIDER;
    }

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        var context = getContext(accessor);
        var obj = objectClass.cast(accessor.getBlockEntity());
        var icon = provider.getIcon(obj, context);
        return icon != null ? elementHelper.item(icon) : null;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // Removes the built-in Forge-Energy progressbar
        tooltip.remove(Identifiers.FORGE_ENERGY);
        // Removes the built-in Forge fluid bars, because usually we have 6 tanks...
        tooltip.remove(Identifiers.FORGE_FLUID);
        tooltip.remove(Identifiers.FABRIC_FLUID);

        var context = new InGameTooltipContext(
                accessor.getServerData(),
                accessor.getHitResult().getLocation(),
                accessor.getPlayer());
        var tooltipBuilder = new JadeTooltipBuilder(tooltip);
        var obj = objectClass.cast(accessor.getBlockEntity());

        var name = provider.getName(obj, context);
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

        // Append the rest of the tooltip
        provider.buildTooltip(obj, context, tooltipBuilder);
    }

    private InGameTooltipContext getContext(BlockAccessor accessor) {
        return new InGameTooltipContext(
                accessor.getServerData(),
                accessor.getHitResult().getLocation(),
                accessor.getPlayer());
    }

}
