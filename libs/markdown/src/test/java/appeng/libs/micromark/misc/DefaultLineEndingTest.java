package appeng.libs.micromark.misc;

import appeng.libs.micromark.TestUtil;
import appeng.libs.micromark.html.CompileOptions;
import appeng.libs.micromark.html.ParseOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DefaultLineEndingTest {
    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "> a||<blockquote>^n<p>a</p>^n</blockquote>||should use `\\n` default",
            "> a^n||<blockquote>^n<p>a</p>^n</blockquote>^n||should infer the first line ending (1)",
            "> a\r||<blockquote>\r<p>a</p>\r</blockquote>\r||should infer the first line ending (2)",
            "> a\r^n||<blockquote>\r^n<p>a</p>\r^n</blockquote>\r^n||should infer the first line ending (3)",
    })
    public void testGeneratedHtml(String markdown, String expectedHtml, String message) {
        TestUtil.assertGeneratedHtml(markdown, expectedHtml);
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @CsvSource(delimiterString = "||", ignoreLeadingAndTrailingWhitespace = false, value = {
            "> a||<blockquote>\r<p>a</p>\r</blockquote>||should support the given line ending",
            "> a^n||<blockquote>\r<p>a</p>\r</blockquote>^n||should support the given line ending, even if line endings exist"
    })
    public void testGeneratedHtmlWithCarriageReturns(String markdown, String expectedHtml, String message) {
        var options = new CompileOptions();
        options.setDefaultLineEnding("\r");

        TestUtil.assertGeneratedHtml(markdown, expectedHtml, null, new ParseOptions(), options);
    }
}
