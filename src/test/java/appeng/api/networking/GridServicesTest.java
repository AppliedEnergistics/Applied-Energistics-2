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

import appeng.util.BootstrapMinecraft;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MockitoSettings
@BootstrapMinecraft
class GridServicesTest {

    @Mock
    IGrid grid;

    private List<Object> servicesBefore;
    private List<Object> indicesBefore;

    @BeforeEach
    void clearRegistry() throws Exception {
        var list = getPrivateList("registry");
        servicesBefore = new ArrayList<>(list);
        list.clear();

        list = getPrivateList("interfaceIndices");
        indicesBefore = new ArrayList<>(list);
        list.clear();
    }

    @AfterEach
    void restoreRegistry() throws Exception {
        var list = getPrivateList("registry");
        list.clear();
        list.addAll(servicesBefore);

        list = getPrivateList("interfaceIndices");
        list.clear();
        list.addAll(indicesBefore);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> getPrivateList(String name) throws Exception {
        var field = GridServices.class.getDeclaredField(name);
        field.setAccessible(true);
        return (List<Object>) field.get(null);
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
        assertThat(services).allSatisfy(service -> assertThat(service).isInstanceOf(PublicInterface.class));
        assertThat(services[GridServicesInternal.getServiceIndex(PublicInterface.class)]).isInstanceOf(GridService1.class);
    }

    @Test
    void testGridServiceWithGridDependency() {
        GridServices.register(GridService2.class, GridService2.class);

        var services = GridServices.createServices(grid);
        assertThat(services).allSatisfy(service -> assertThat(service).isInstanceOf(GridService2.class));
        var actual = (GridService2) services[GridServicesInternal.getServiceIndex(GridService2.class)];
        assertThat(actual.grid).isSameAs(grid);
    }

    @Test
    void testGridServicesWithDependencies() {
        GridServices.register(PublicInterface.class, GridService1.class);
        GridServices.register(GridService2.class, GridService2.class);
        GridServices.register(GridService3.class, GridService3.class);
        GridServices.register(GridService4.class, GridService4.class);

        var services = GridServices.createServices(grid);

        var service3 = (GridService3) services[GridServicesInternal.getServiceIndex(GridService3.class)];
        assertThat(service3.service1).isSameAs(services[GridServicesInternal.getServiceIndex(PublicInterface.class)]);
        assertThat(service3.service2).isSameAs(services[GridServicesInternal.getServiceIndex(GridService2.class)]);
        var service4 = (GridService4) services[GridServicesInternal.getServiceIndex(GridService4.class)];
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
