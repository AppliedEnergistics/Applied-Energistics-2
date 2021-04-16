package appeng.client.gui;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import appeng.client.gui.style.ScreenStyle;

class ScreenStyleTest {

    @Test
    void testValidityOfAllScreenStyles() throws Exception {
        Path styles = Paths.get(getClass().getResource("/screens/upgradeable.json").toURI()).getParent();

        Files.walk(styles).filter(f -> f.endsWith(".json")).forEach(f -> {
            try (Reader r = Files.newBufferedReader(f)) {
                ScreenStyle style = ScreenStyle.GSON.fromJson(r, ScreenStyle.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

}
