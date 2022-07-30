package appeng.api.integrations.igtooltip;

import static appeng.api.integrations.igtooltip.TooltipProvider.DEFAULT_PRIORITY;

import javax.annotation.concurrent.ThreadSafe;

import org.jetbrains.annotations.ApiStatus;

import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.integration.modules.igtooltip.parts.PartTooltipProviders;

/**
 * Add additional in-game tooltips for parts and integrate them automatically with any AE2 supported in-game tooltip mod
 * (Jade, TOP, WTHIT).
 */
@ApiStatus.Experimental
@ThreadSafe
public final class PartTooltips {

    private PartTooltips() {
    }

    public static <T> void addServerData(Class<T> baseClass, ServerDataProvider<? super T> provider) {
        addServerData(baseClass, provider, DEFAULT_PRIORITY);
    }

    public static <T> void addServerData(Class<T> baseClass, ServerDataProvider<? super T> provider, int priority) {
        PartTooltipProviders.addServerData(baseClass, provider, priority);
    }

    public static <T> void addBody(Class<T> baseClass, BodyProvider<? super T> provider) {
        addBody(baseClass, provider, DEFAULT_PRIORITY);
    }

    public static <T> void addBody(Class<T> baseClass, BodyProvider<? super T> provider, int priority) {
        PartTooltipProviders.addBody(baseClass, provider, priority);
    }

    public static <T> void addName(Class<T> baseClass, NameProvider<? super T> provider) {
        addName(baseClass, provider, DEFAULT_PRIORITY);
    }

    public static <T> void addName(Class<T> baseClass, NameProvider<? super T> provider, int priority) {
        PartTooltipProviders.addName(baseClass, provider, priority);
    }

    public static <T> void addIcon(Class<T> baseClass, IconProvider<? super T> provider) {
        addIcon(baseClass, provider, DEFAULT_PRIORITY);
    }

    public static <T> void addIcon(Class<T> baseClass, IconProvider<? super T> provider, int priority) {
        PartTooltipProviders.addIcon(baseClass, provider, priority);
    }
}
