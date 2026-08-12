package appeng.api.implementations.menus;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;

@MockitoSettings
class TerminalKeyFiltersTest {
    @Mock
    IGrid grid;

    @Mock
    AEKey key;

    private List<TerminalKeyFilter> filtersBefore;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void clearRegistry() throws Exception {
        var filters = getFilters();
        filtersBefore = List.copyOf(filters);
        filters.clear();
    }

    @AfterEach
    void restoreRegistry() throws Exception {
        var filters = getFilters();
        filters.clear();
        filters.addAll(filtersBefore);
    }

    @Test
    void combinesFiltersUsingLogicalAnd() {
        assertThat(TerminalKeyFilters.isVisible(grid, key)).isTrue();

        TerminalKeyFilters.register((grid, key) -> true);
        assertThat(TerminalKeyFilters.isVisible(grid, key)).isTrue();

        TerminalKeyFilters.register((grid, key) -> false);
        assertThat(TerminalKeyFilters.isVisible(grid, key)).isFalse();
    }

    @SuppressWarnings("unchecked")
    private static List<TerminalKeyFilter> getFilters() throws NoSuchFieldException, IllegalAccessException {
        Field field = TerminalKeyFilters.class.getDeclaredField("FILTERS");
        field.setAccessible(true);
        return (List<TerminalKeyFilter>) field.get(null);
    }
}
