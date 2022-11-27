package appeng.libs.micromark.misc;

import appeng.libs.micromark.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class NullByteTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "asd\0asd||<p>asd�asd</p>||should replace `\\0` w/ a replacement characters (`�`)",
            "&#0;||<p>�</p>||should replace NUL in a character reference",
            // This doesn’t make sense in MD, as character escapes only work on ascii
            // punctuation, but it’s good to demonstrate the behavior.
            "\\0||<p>\\0</p>||should not support NUL in a character escape",
    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }
}
