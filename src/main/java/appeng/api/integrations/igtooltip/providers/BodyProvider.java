package appeng.api.integrations.igtooltip.providers;

import org.jetbrains.annotations.ApiStatus;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;

@ApiStatus.Experimental
@ApiStatus.OverrideOnly
@FunctionalInterface
public interface BodyProvider<T> {
    void buildTooltip(T object, TooltipContext context, TooltipBuilder tooltip);
}
