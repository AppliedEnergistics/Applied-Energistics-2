package appeng.core.registries;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCacheProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MockitoSettings
class GridCacheRegistryTest {

    @Mock
    IGrid grid;

    GridCacheRegistry registry = new GridCacheRegistry();

    @Test
    void testEmptyRegistry() {
        var caches = registry.createCacheInstance(grid);
        assertThat(caches).isEmpty();
    }

    @Test
    void testGridCacheWithDefaultConstructor() {
        registry.registerGridCache(PublicInterface.class, GridCache1.class);

        var caches = registry.createCacheInstance(grid);
        assertThat(caches).containsOnlyKeys(PublicInterface.class);
        assertThat(caches.get(PublicInterface.class)).isInstanceOf(GridCache1.class);
    }

    @Test
    void testGridCacheWithGridDependency() {
        registry.registerGridCache(GridCache2.class, GridCache2.class);

        var caches = registry.createCacheInstance(grid);
        assertThat(caches).containsOnlyKeys(GridCache2.class);
        var actual = (GridCache2) caches.get(GridCache2.class);
        assertThat(actual.grid).isSameAs(grid);
    }

    @Test
    void testGridCachesWithDependencies() {
        registry.registerGridCache(PublicInterface.class, GridCache1.class);
        registry.registerGridCache(GridCache2.class, GridCache2.class);
        registry.registerGridCache(GridCache3.class, GridCache3.class);
        registry.registerGridCache(GridCache4.class, GridCache4.class);

        var caches = registry.createCacheInstance(grid);
        assertThat(caches).containsOnlyKeys(
                PublicInterface.class,
                GridCache2.class,
                GridCache3.class,
                GridCache4.class
        );

        var cache3 = (GridCache3) caches.get(GridCache3.class);
        assertThat(cache3.cache1).isSameAs(caches.get(PublicInterface.class));
        assertThat(cache3.cache2).isSameAs(caches.get(GridCache2.class));
        var cache4 = (GridCache4) caches.get(GridCache4.class);
        assertThat(cache4.cache3).isSameAs(cache3);
        assertThat(cache4.grid).isSameAs(grid);
    }

    @Test
    void testCantRegisterCacheBeforeItsDependencies() {
        registry.registerGridCache(GridCache2.class, GridCache2.class);
        assertThatThrownBy(() -> registry.registerGridCache(GridCache3.class, GridCache3.class))
                .hasMessageContaining("Missing dependency")
                .hasMessageContaining("PublicInterface");
    }

    @Test
    void testMustHavePublicConstructor() {
        assertThatThrownBy(() -> registry.registerGridCache(NoCtorClass.class, NoCtorClass.class))
                .hasMessageContaining("Grid cache implementation class")
                .hasMessageContaining("has 0 public constructors. It needs exactly 1");
    }

    @Test
    void testNoAmbiguousConstructorAllowed() {
        assertThatThrownBy(() -> registry.registerGridCache(AmbiguousConstructorClass.class, AmbiguousConstructorClass.class))
                .hasMessageContaining("Grid cache implementation class")
                .hasMessageContaining("has 2 public constructors. It needs exactly 1");
    }

    interface PublicInterface {
    }

    public static class GridCache1 implements PublicInterface, IGridCacheProvider {
    }

    public static class GridCache2 implements IGridCacheProvider {
        public IGrid grid;

        public GridCache2(IGrid grid) {
            this.grid = grid;
        }
    }

    public static class GridCache3 implements IGridCacheProvider {
        public PublicInterface cache1;
        public GridCache2 cache2;

        public GridCache3(PublicInterface cache1, GridCache2 cache2) {
            this.cache1 = cache1;
            this.cache2 = cache2;
        }
    }

    public static class GridCache4 implements IGridCacheProvider {
        public IGrid grid;
        public GridCache3 cache3;

        public GridCache4(IGrid grid, GridCache3 cache3) {
            this.grid = grid;
            this.cache3 = cache3;
        }
    }

    // No public constructor
    static class NoCtorClass implements IGridCacheProvider {
    }

    static class AmbiguousConstructorClass implements IGridCacheProvider {
        public AmbiguousConstructorClass(GridCache1 gc1) {
        }

        public AmbiguousConstructorClass(GridCache2 gc2) {
        }
    }

}