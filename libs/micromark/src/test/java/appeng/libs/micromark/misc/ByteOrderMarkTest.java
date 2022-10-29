package appeng.libs.micromark.misc;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ByteOrderMarkTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "\uFEFF||||should ignore just a bom",
            "\uFEFF# hea\uFEFFding||<h1>hea\uFEFFding</h1>||should ignore a bom",
    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }
}
