package appeng.libs.micromark.misc;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DangerousHtmlTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "<x>||&lt;x&gt;||should be safe by default for flow",
            "a<b>||<p>a&lt;b&gt;</p>||should be safe by default for text",
    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "<x>||<x>||should be unsafe w/ `allowDangerousHtml`",
    })
    public void testGeneratedHtmlUnsafe(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedDangerousHtml(markdown, expectedHtml);
    }
}
