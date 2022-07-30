package appeng.api.integrations.igtooltip.providers;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.api.integrations.igtooltip.TooltipContext;

/**
 * Provides the icon shown in the in-game tooltip.
 */
@ApiStatus.Experimental
@ApiStatus.OverrideOnly
@FunctionalInterface
public interface IconProvider<T> {
    /**
     * @return Null if this provider can't provide an icon for the object.
     */
    @Nullable
    ItemStack getIcon(T object, TooltipContext context);
}
