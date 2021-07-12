package appeng.api.networking;

import appeng.api.networking.events.GridEvent;

import java.util.function.BiConsumer;

public class GridCacheBuilder<T extends IGridCache> {

    public GridCacheBuilder(Class<T> cacheClass) {
    }

    /**
     * Declare a dependency on another grid cache. Once the grid is fully initialized, the given
     * function will be called with other grid cache as an argument. You can for example pass
     * a method reference to a setter on your grid cache to receive a reference to another grid cache.
     */
    public <D extends IGridCache> GridCacheBuilder<T> dependency(Class<D> gridCache, BiConsumer<T, D> consumer) {

        return this;
    }

    /**
     * Declare that your grid cache wants to be notified of a grid event.
     */
    public <E extends GridEvent> GridCacheBuilder<T> eventHandler(Class<E> gridEvent, BiConsumer<T, E> callback) {

        T gridCache = null;
        E evt = null;
        callback.accept(gridCache, evt);

        return this;
    }

}
