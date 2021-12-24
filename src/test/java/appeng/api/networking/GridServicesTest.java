/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.api.networking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import appeng.util.BootstrapMinecraft;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
@BootstrapMinecraft
class GridServicesTest {

    @Mock
    IGrid grid;

    private List<?> servicesBefore;

    @BeforeEach
    void clearRegistry() throws Exception {
        var field = GridServices.class.getDeclaredField("registry");
        field.setAccessible(true);
        List<?> list = (List<?>) field.get(null);
        servicesBefore = new ArrayList<>(list);
        list.clear();
    }

    @AfterEach
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void restoreRegistry() throws Exception {
        var field = GridServices.class.getDeclaredField("registry");
        field.setAccessible(true);
        var list = (List) field.get(null);
        list.clear();
        list.addAll(servicesBefore);
    }

    @Test
    void testEmptyRegistry() {
        var services = GridServices.createServices(grid);
        assertThat(services).isEmpty();
    }

    @Test
    void testGridServiceWithDefaultConstructor() {
        GridServices.register(PublicInterface.class, GridService1.class);

        var services = GridServices.createServices(grid);
        assertThat(services).containsOnlyKeys(PublicInterface.class);
        assertThat(services.get(PublicInterface.class)).isInstanceOf(GridService1.class);
    }

    @Test
    void testGridServiceWithGridDependency() {
        GridServices.register(GridService2.class, GridService2.class);

        var services = GridServices.createServices(grid);
        assertThat(services).containsOnlyKeys(GridService2.class);
        var actual = (GridService2) services.get(GridService2.class);
        assertThat(actual.grid).isSameAs(grid);
    }

    @Test
    void testGridServicesWithDependencies() {
        GridServices.register(PublicInterface.class, GridService1.class);
        GridServices.register(GridService2.class, GridService2.class);
        GridServices.register(GridService3.class, GridService3.class);
        GridServices.register(GridService4.class, GridService4.class);

        var services = GridServices.createServices(grid);
        assertThat(services).containsOnlyKeys(
                PublicInterface.class,
                GridService2.class,
                GridService3.class,
                GridService4.class);

        var service3 = (GridService3) services.get(GridService3.class);
        assertThat(service3.service1).isSameAs(services.get(PublicInterface.class));
        assertThat(service3.service2).isSameAs(services.get(GridService2.class));
        var service4 = (GridService4) services.get(GridService4.class);
        assertThat(service4.service3).isSameAs(service3);
        assertThat(service4.grid).isSameAs(grid);
    }

    @Test
    void testCantRegisterServiceBeforeItsDependencies() {
        GridServices.register(GridService2.class, GridService2.class);
        assertThatThrownBy(() -> GridServices.register(GridService3.class, GridService3.class))
                .hasMessageContaining("Missing dependency")
                .hasMessageContaining("PublicInterface");
    }

    @Test
    void testMustHavePublicConstructor() {
        assertThatThrownBy(() -> GridServices.register(NoCtorClass.class, NoCtorClass.class))
                .hasMessageContaining("Grid service implementation class")
                .hasMessageContaining("has 0 public constructors. It needs exactly 1");
    }

    @Test
    void testNoAmbiguousConstructorAllowed() {
        assertThatThrownBy(
                () -> GridServices.register(AmbiguousConstructorClass.class, AmbiguousConstructorClass.class))
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
