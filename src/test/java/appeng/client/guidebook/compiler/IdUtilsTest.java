package appeng.client.guidebook.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IdUtilsTest {

    @CsvSource({
            "some_page,ae2,ae2:some_page",
            "ae2:some_page,ae2,ae2:some_page",
            "minecraft:some_page,ae2,minecraft:some_page",
    })
    @ParameterizedTest
    void testResolveId(String input, String defaultNamespace, String expected) {
        assertEquals(expected, IdUtils.resolveId(input, defaultNamespace).toString());
    }

}
