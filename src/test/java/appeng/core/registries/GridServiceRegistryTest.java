package appeng.core.registries;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridServiceProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MockitoSettings
class GridServiceRegistryTest {

    @Mock
    IGrid grid;

    GridServiceRegistry registry = new GridServiceRegistry();

    @Test
    void testEmptyRegistry() {
        var services = registry.createGridServices(grid);
        assertThat(services).isEmpty();
    }

    @Test
    void testGridServiceWithDefaultConstructor() {
        registry.register(PublicInterface.class, GridService1.class);

        var services = registry.createGridServices(grid);
        assertThat(services).containsOnlyKeys(PublicInterface.class);
        assertThat(services.get(PublicInterface.class)).isInstanceOf(GridService1.class);
    }

    @Test
    void testGridServiceWithGridDependency() {
        registry.register(GridService2.class, GridService2.class);

        var services = registry.createGridServices(grid);
        assertThat(services).containsOnlyKeys(GridService2.class);
        var actual = (GridService2) services.get(GridService2.class);
        assertThat(actual.grid).isSameAs(grid);
    }

    @Test
    void testGridServicesWithDependencies() {
        registry.register(PublicInterface.class, GridService1.class);
        registry.register(GridService2.class, GridService2.class);
        registry.register(GridService3.class, GridService3.class);
        registry.register(GridService4.class, GridService4.class);

        var services = registry.createGridServices(grid);
        assertThat(services).containsOnlyKeys(
                PublicInterface.class,
                GridService2.class,
                GridService3.class,
                GridService4.class
        );

        var service3 = (GridService3) services.get(GridService3.class);
        assertThat(service3.service1).isSameAs(services.get(PublicInterface.class));
        assertThat(service3.service2).isSameAs(services.get(GridService2.class));
        var service4 = (GridService4) services.get(GridService4.class);
        assertThat(service4.service3).isSameAs(service3);
        assertThat(service4.grid).isSameAs(grid);
    }

    @Test
    void testCantRegisterServiceBeforeItsDependencies() {
        registry.register(GridService2.class, GridService2.class);
        assertThatThrownBy(() -> registry.register(GridService3.class, GridService3.class))
                .hasMessageContaining("Missing dependency")
                .hasMessageContaining("PublicInterface");
    }

    @Test
    void testMustHavePublicConstructor() {
        assertThatThrownBy(() -> registry.register(NoCtorClass.class, NoCtorClass.class))
                .hasMessageContaining("Grid service implementation class")
                .hasMessageContaining("has 0 public constructors. It needs exactly 1");
    }

    @Test
    void testNoAmbiguousConstructorAllowed() {
        assertThatThrownBy(() -> registry.register(AmbiguousConstructorClass.class, AmbiguousConstructorClass.class))
                .hasMessageContaining("Grid service implementation class")
                .hasMessageContaining("has 2 public constructors. It needs exactly 1");
    }

    interface PublicInterface {
    }

    public static class GridService1 implements PublicInterface, IGridServiceProvider {
    }

    public static class GridService2 implements IGridServiceProvider {
        public IGrid grid;

        public GridService2(IGrid grid) {
            this.grid = grid;
        }
    }

    public static class GridService3 implements IGridServiceProvider {
        public PublicInterface service1;
        public GridService2 service2;

        public GridService3(PublicInterface service1, GridService2 service2) {
            this.service1 = service1;
            this.service2 = service2;
        }
    }

    public static class GridService4 implements IGridServiceProvider {
        public IGrid grid;
        public GridService3 service3;

        public GridService4(IGrid grid, GridService3 service3) {
            this.grid = grid;
            this.service3 = service3;
        }
    }

    // No public constructor
    static class NoCtorClass implements IGridServiceProvider {
    }

    static class AmbiguousConstructorClass implements IGridServiceProvider {
        public AmbiguousConstructorClass(GridService1 gc1) {
        }

        public AmbiguousConstructorClass(GridService2 gc2) {
        }
    }

}