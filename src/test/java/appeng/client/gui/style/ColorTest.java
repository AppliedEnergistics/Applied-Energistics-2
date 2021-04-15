package appeng.client.gui.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColorTest {
    @Test
    void testColorParsing() {
        assertEquals(new Color(255, 255, 255, 255), Color.parse("#ffffff"));
        assertEquals(new Color(255, 255, 255, 255), Color.parse("#ffffffff"));
        assertEquals(new Color(0, 0, 0, 255), Color.parse("#000000"));
        assertEquals(new Color(0, 0, 0, 0), Color.parse("#00000000"));
        assertEquals(new Color(0x11, 0x22, 0x33, 255), Color.parse("#112233"));
        assertEquals(new Color(0x22, 0x33, 0x44, 0x11), Color.parse("#11223344"));
        assertEquals(new Color(0x01, 0x02, 0x03, 255), Color.parse("#010203"));
        assertEquals(new Color(0x02, 0x03, 0x04, 0x01), Color.parse("#01020304"));
    }

    @Test
    void testInvalidColorParsing() {
        assertThrows(IllegalArgumentException.class, () -> Color.parse("#"));
        assertThrows(IllegalArgumentException.class, () -> Color.parse("#f"));
        assertThrows(IllegalArgumentException.class, () -> Color.parse("#x00000"));
        assertThrows(IllegalArgumentException.class, () -> Color.parse("#000000001"));
    }

    @Test
    void testToString() {
        assertEquals("#ffffffff", new Color(255, 255, 255, 255).toString());
        assertEquals("#ffffffff", new Color(255, 255, 255, 255).toString());
        assertEquals("#ff000000", new Color(0, 0, 0, 255).toString());
        assertEquals("#00000000", new Color(0, 0, 0, 0).toString());
        assertEquals("#ff112233", new Color(0x11, 0x22, 0x33, 255).toString());
        assertEquals("#11223344", new Color(0x22, 0x33, 0x44, 0x11).toString());
        assertEquals("#ff010203", new Color(0x01, 0x02, 0x03, 255).toString());
        assertEquals("#01020304", new Color(0x02, 0x03, 0x04, 0x01).toString());
    }

    @Test
    void testToARGB() {
        assertEquals(0xffffffff, new Color(255, 255, 255, 255).toARGB());
        assertEquals(0xffffffff, new Color(255, 255, 255, 255).toARGB());
        assertEquals(0xff000000, new Color(0, 0, 0, 255).toARGB());
        assertEquals(0x00000000, new Color(0, 0, 0, 0).toARGB());
        assertEquals(0xff112233, new Color(0x11, 0x22, 0x33, 255).toARGB());
        assertEquals(0x11223344, new Color(0x22, 0x33, 0x44, 0x11).toARGB());
        assertEquals(0xff010203, new Color(0x01, 0x02, 0x03, 255).toARGB());
        assertEquals(0x01020304, new Color(0x02, 0x03, 0x04, 0x01).toARGB());
    }
}