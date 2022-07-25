package appeng.integration.modules.wthit;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.WailaConstants;
import mcp.mobius.waila.api.component.ItemComponent;

import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;

/**
 * Delegation provider for tiles through {@link mcp.mobius.waila.api.IBlockComponentProvider}
 */
public final class BlockEntityDataProvider<T extends BlockEntity>
        implements IBlockComponentProvider, IServerDataProvider<T> {
    /**
     * Contains all providers
     */
    private final InGameTooltipProvider<? super T> provider;

    private final Class<T> objectClass;

    public BlockEntityDataProvider(InGameTooltipProvider<? super T> provider, Class<T> objectClass) {
        this.provider = provider;
        this.objectClass = objectClass;
    }

    @Override
    public @Nullable ITooltipComponent getIcon(IBlockAccessor accessor, IPluginConfig config) {
        var context = getContext(accessor);
        var obj = objectClass.cast(accessor.getBlockEntity());
        var icon = provider.getIcon(obj, context);
        return icon != null ? new ItemComponent(icon) : null;
    }

    @Override
    public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        var context = getContext(accessor);
        var obj = objectClass.cast(accessor.getBlockEntity());
        var name = provider.getName(obj, context);
        if (name != null) {
            tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, name.copy().withStyle(style -> {
                // Don't overwrite a text color if one is present
                if (style.getColor() == null) {
                    return style.withColor(ChatFormatting.WHITE);
                } else {
                    return style;
                }
            }));
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        var context = getContext(accessor);
        var tooltipBuilder = new WthitTooltipBuilder(tooltip);
        var obj = objectClass.cast(accessor.getBlockEntity());
        provider.buildTooltip(obj, context, tooltipBuilder);
    }

    @Override
    public void appendTail(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        var context = getContext(accessor);
        var obj = objectClass.cast(accessor.getBlockEntity());

        var modName = provider.getModName(obj, context);
        if (modName != null) {
            // Only add the mod name if it's already there
            if (tooltip.getLine(WailaConstants.MOD_NAME_TAG) != null) {
                tooltip.setLine(WailaConstants.MOD_NAME_TAG, Component.literal(modName).withStyle(
                        ChatFormatting.BLUE, ChatFormatting.ITALIC));
            }
        }
    }

    private InGameTooltipContext getContext(IBlockAccessor accessor) {
        return new InGameTooltipContext(
                accessor.getServerData(),
                accessor.getHitResult().getLocation(),
                accessor.getPlayer());
    }

    @Override
    public void appendServerData(CompoundTag data, IServerAccessor<T> accessor, IPluginConfig config) {
        provider.provideServerData(accessor.getPlayer(), accessor.getTarget(), data);
    }
}
