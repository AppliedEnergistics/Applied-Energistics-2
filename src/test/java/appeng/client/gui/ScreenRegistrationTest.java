package appeng.client.gui;

import appeng.client.gui.style.StyleManager;
import net.minecraft.client.gui.ScreenManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@MockitoSettings
class ScreenRegistrationTest {

    @BeforeAll
    static void setUp() {
        try (MockedStatic<ScreenManager> registration = Mockito.mockStatic(ScreenManager.class)) {
            ScreenRegistration.register();
        }
    }

    /**
     * Tests that no styles referenced during screen registration are missing.
     */
    @Test
    void testMissingStyles() {
        List<String> missingStyles = ScreenRegistration.CONTAINER_STYLES.values().stream()
                .filter(f -> {
                    return getClass().getResourceAsStream("/assets/appliedenergistics2" + f) == null;
                })
                .collect(Collectors.toList());
        assertThat(missingStyles).isEmpty();
    }

    /**
     * Tests that all of the styles referenced can be deserialized.
     */
    @Test
    void testBrokenStyles() throws IOException {
        StyleManager.initialize(MockResourceManager.create());

        List<String> errors = new ArrayList<>();
        for (String path : ScreenRegistration.CONTAINER_STYLES.values()) {
            try {
                StyleManager.loadStyleDoc(path);
            } catch (Exception e) {
                errors.add(path + ": " + e);
            }
        }

        assertThat(errors).isEmpty();
    }
}