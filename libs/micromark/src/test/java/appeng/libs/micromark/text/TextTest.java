package appeng.libs.micromark.text;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TextTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "hello $.;\"there||<p>hello $.;\"there</p>||should support ascii text",
            "Foo χρῆν||<p>Foo χρῆν</p>||should support unicode text",

            "Multiple     spaces||<p>Multiple     spaces</p>||should preserve internal spaces verbatim",

    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }


}
