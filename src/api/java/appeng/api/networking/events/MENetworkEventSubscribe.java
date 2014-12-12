package appeng.api.networking.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;

/**
 * Usable on any {@link IGridHost}, or {@link IGridCache}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MENetworkEventSubscribe {

}
